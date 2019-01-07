# Neo4j JobRecommendation
Neo4j + Java Spring Boot + SocialNetwork
 
想通过一个不同的方式展现论文，同时又能匹配论文的主题，制作一个网页博客的想法便油然而生，本篇论文主题为基于社交网络的应用系统，我将借用Neo4j官网上的一个数据库，[内部职位推荐数据库](https://neo4j.com/graphgist/competency-management-a-matter-of-filtering-and-recommendation-engines#ranking)，来展开整个WebService的构建，并探索图数据库在社交网络方面的独特用处。

# 基于图数据库的内部职位推荐引擎

本篇论文模拟实现公司组织内部使用的职位推荐模型，随着组织的扩大，寻找工作和人员的匹配变得愈加困难，该模型目标实现组织内部寻找某个职位或工作的需要。

## 模型简介

现有企业通常都有内部调整职位的需求，同时，做到岗位、技能匹配能大大提高公司内部效率，然而，现有企业面临以下几个主要挑战：
1. 寻找“理想员工”的任务通常仅仅交给HR或者组织中个别人，但是，他们一个人没法全面的认识员工技能，更没法全面认识企业中每一位员工。因此，在筛选过程中，经常产生偏见，因为一个**员工社交网络**通常较难完整识别。
2. 即使在有些组织中，将这个任务交给一个团队，但是团队决策通常较慢，同时团队中的不同意见最终也必须由人来解决，为了避免偏见，产生了新的偏见。
3. 组织内即使存在员工技能数据库，数据库也必须在固定间隔时间内，进行维护和整理，而这个过程在很多组织内是欠缺的。

为了解决以上挑战，以下展示了一个基于Neo4j图数据库的员工数据库prototype，同时体现了图数据库和CQL语言在处理**社交网络相关**问题时的优秀特性（对比关系型数据库）。

## 数据库构建

本数据库在构建时仅仅考虑最基础的节点和关系。节点主要分为以下7个：员工（Employee），角色（Role），技能（Personal_Skill），领域（Competence_area），学历（Degree），团队（Team），工作（Activity）。关系主要有员工-角色，员工-团队，员工-学历，员工-技能，工作-技能，工作-领域，角色-工作，相关节点和关系的属性及生成以上内容的样例代码如下：

``` sql
CREATE
(u1:Employee {name:'Employee 1'}),
(rol1:Role {name:'Role 1', dept:'dept 1', hierarchy:'employee', open_status:0}),
(skill1:Personal_Skill {name:'Personal Skill 1', set:'Skill Set 1'}),
(comp1:Competence_area {name:'Competence Area 1'}),
(deg1:Degree {name:'Degree 1', institution:'Uni 1', area:'area 1', grade:'grade A'}),
(t1:Team {name:'Team 1', team_size: 1}),
(act1:Activity {name:'Activity 1', complexity:4.0}),
(u1)-[:WORKS_AS {duration:2, location:'Location 1'}]->(rol1),
(u1)-[:IN_TEAM {current: 1}]->(t1),
(u1)-[:CAN_PERFORM {bin_threshold: 1}]->(act1),
(u1)-[:HAS_DEGREE]->(deg1),
(u1)-[:HAS_SKILL]->(skill1),
(u2)-[:ENDORSES {level:4.0}]->(skill1),
(act1)-[:REQUIRES]->(skill1),
(act1)-[:IN_AREA]->(comp1),
(rol1)-[:RELATED_ACTIVITY {level_weight: 4.0}]->(act1);
```

在Neo4j数据库中可视化结果如下（个别关系有可能有所差别，原因为后文的相关操作）：

![初始数据可视化](https://github.com/Andyyesiyu/Neo4jJobRecommendation/blob/master/image/initdata.jpg)

## 筛选员工的几种方法
以下将详细演示多种社交网络算法在本案例中的实现。

### 1. 排除法
利用排除法排除无关员工是HR部门常用的筛选方法，同时也符合人类理性思考的过程，另外排除一定节点后，也有利于程序性能的提高。

首先，对所有员工节点增加**exclude**属性，若**exclude**属性为1则代表该员工已经被排除，为0则代表未被排除。首先，针对工作时间进行排除，如筛选掉未工作满1年的人员；其次，针对目标工作所需要的学历进行筛选；另外，针对目标工作所制定的特殊技能、领域等一一排除。若是普通的关系型数据库，以上每一次排除都要做多次表连接（JOIN），不仅消耗性能，一旦员工数据量很大，每次耗时都很长。不利于数据的实时更新，但是Neo4j图数据库可以直接对关系进行搜索，因此大大降低搜索的复杂度和时间，两者差距可能达到100倍。针对特定需求，我们可以构建Cypher语句类似于下方代码：

``` sql
MATCH (n:Employee)-[:HAS_DEGREE]-(:Degree {area:"area 1"})
WHERE (n)-[:HAS_SKILL]-(:Personal_Skill)<-[:REQUIRES]-(:Activity)-[:IN_AREA]->(:Competence_area {name:'Competence Area 2'})
WITH n AS person
MATCH (person)-[r:WORKS_AS]-()
WHERE r.duration>1
RETURN person.name AS `Matching Candidate`
```

### 2. 协同评分法
针对以上方法，可以发现最后没有搜索到任何结果。这首先是因为我们构建的数据库数据量不大，但是也反映了排除法算法的局限性，过于局限于现有能力，使最后推荐的结果受限，因此我们可以采用一种基于距离的**社交网络**算法。

为了定量分析各个员工之间的关系，首先需要构建员工的属性矩阵，并计算各个员工之间的相似度，通过定量计算相似度，即使两个员工并没有直接联系，其关系也可以量化体现。该算法思路其实类似于**基于用户的推荐系统**，为了简化计算，我们这里采用余弦相似。但是，数据库中员工并没有直接联系，而需要通过**技能**进行联系，因此我们可以利用一下CQL建立员工之间的联系RATES。

``` sql
MATCH (u1:Employee)-[x:ENDORSES]->(:Personal_Skill)<-[:HAS_SKILL]-(u2:Employee)
WITH AVG(x.level) AS rating_score, u1, u2
CREATE UNIQUE (u1)-[:RATES {rating:rating_score}]->(u2)
```

以上步骤仅仅能表达员工与员工之间的联系，但是在一个Team存在多个员工时，仅仅计算员工之间的相似度并不足够，为了计算Team与员工的相似度，需要排除其他本组的员工，只计算一次本组员工，即本组员工之间的联系不重新计算。随后计算小组与各个其他员工的平均评分，以Team3为例，该步骤的样例代码如下：

``` sql
MATCH (u1:Employee)-[x:RATES]->(u2:Employee)
WHERE (u1)-[:IN_TEAM]-(:Team {name:'Team 3'}) AND NOT (u2)-[:IN_TEAM]-(:Team {name:'Team 3'})
WITH AVG(x.rating) AS team_score, u2
MATCH (t:Team {name:'Team 3'})
CREATE UNIQUE (t)-[:RATES {team_rating:team_score}]->(u2)
```

经过以上处理，我们已经得出了员工与员工和员工与Team的评分，那么，我们可以利用相似的算法，引入余弦相似度计算所有与Team3有关的相似度。

``` sql
MATCH (t:Team {name:'Team 3'})-[x:RATES]->(:Employee)<-[y:RATES]-(u2:Employee)
WHERE not (u2)-[:IN_TEAM]-(t)
WITH SUM(x.team_rating * y.rating) AS xyDotProduct,
 SQRT(REDUCE(xDot = 0.0, a IN COLLECT(x.team_rating) | xDot + a^2)) AS xLength,
 SQRT(REDUCE(yDot = 0.0, b IN COLLECT(y.rating) | yDot + b^2)) AS yLength,
 t, u2
MERGE (t)<-[s:SIMILARITY]-(u2)
SET s.similarity = xyDotProduct / (xLength * yLength)
```

对于在Team3中的成员，我们则直接设置为2，因为其他相似度肯定小于等于1，因此可以有效区分。为了得出最后考虑的员工结果即对应的评分，我们可以采用K近邻算法减少考虑的数量，进而提高算法效率，由于这里一共有5个员工，我们直接取K为5，采用的代码如下：
``` sql
MATCH (b:Employee)-[r:RATES]->(m:Employee), (b)-[s:SIMILARITY]-(t:Team {name:'Team 3'})
WITH m, s.similarity AS similarity, r.rating AS rating
ORDER BY m.name, similarity DESC
WITH m.name AS candidate, COLLECT(rating)[0..5] AS ratings
WITH candidate, REDUCE(s = 0, i IN ratings | s + i)*1.0 / LENGTH(ratings) AS reco
ORDER BY reco DESC
RETURN candidate AS Candidate, toFloat(reco) AS Recommendation
```
注释： COLLECT为数组，这里采用[0..5]代表取第0个到第4个元素。Reduce函数用来计算数组内各分数的和（**|**为管道函数，代表从第0个变量开始执行Reduce函数）。
在本数据库中得到的结果如下：

| Candidate   |      Recommendation      |  
|-------------|:-------------:|
| Employee 1 |  4 |
| Employee 3 |    3   |

### 3. 其他方法
其实，对于职位推荐，还有很多其他算法，利用其他标准进行选择。例如，以上介绍了**基于用户的过滤**算法的改进应用，而对于另外一种推荐系统模型，**基于物品的协同过滤**也可以在本例中应用。上例中关注于员工（Employee）与Team的关系，而我们只需要将员工改为领域（Competence_area）就可以实现基于物品的过滤算法在本例中的实现。

另外，在上例中我们采用了余弦相似度作为相似度算法，尽管在Neo4j中效率已经较高，但是一旦数据量较大，仍然需要进行很多计算。而另一种改进算法为Jaccard相似度，忽略潜在数值的大小，而仅仅用0，1加以区分，但是和余弦相似度一样，也能达到计算集合相似度的目的。

以上两种方法在Cypher语言中同样能够进行实现，详细实现就不在这里阐释了，可以从本文章开篇中的链接中进行查看。

## 职位推荐模型衍生：能力管理模型
其实，图数据库不仅仅能够在职位推荐方面发挥作用，随着企业信息化的进行，组织可能需要针对员工的能力、涉及领域进行更多的工作，从而提高员工与工作的适配度，大大提高企业内部工作的效率。例如，我们可以引入每个工作所需要的**能力集**节点（现模型只有个人能力），首先在能力集中加入现有工作必须的能力，然后通过搜索员工的所有个人能力的关系，对于存在强关系的能力也放入能力集中，作为能力集中间接需要的能力。该步操作在关系型数据库中可能十分负责，不仅存在表连接，还需要进行表的自连接操作，算法时间复杂度和空间复杂度成倍增长，甚至可能超过现有系统所能承受的范围。而图数据库基于关系的搜索则不存在这种情况，可以加入系统的轮循Job中，定期刷新。

在计算出每个工作的能力集后，可以对于Team中的成员进行能力-能力集的匹配度计算，对于匹配度不高的Team或员工进行有针对的培训，使得企业内培训机制真正起到提高工作效率的作用。

## Demo演示




## Spring Boot搭建介绍

### 1. 项目结构
项目结构为IDEA标准生成的Spring Boot项目结构，为了Demo演示，只选取数据库中部分的节点和关系进行读取，详细项目结构如下图所示：

![后端项目结构](https://github.com/Andyyesiyu/Neo4jJobRecommendation/blob/master/src/main/resources/public/image/springstruc.jpg)

### 2. Domain设计
Domain部分较为简单，对于节点各属性进行初始化，并加上适当的annotation即可。对于节点中的关系也如法炮制。选取部分代码如下：
``` java
@NodeEntity
public class Employee {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    @JsonIgnoreProperties("employee")
    @Relationship(type = "WORKS_AS", direction = Relationship.OUTGOING)
    private List<WorkingExperience> workingExperiences;

    public Employee(String name, List<WorkingExperience> workingExperiences) {
        this.name = name;
        this.workingExperiences = workingExperiences;
    }
}
```

### 3. Repository设计
Repo部分需要包含对数据库的CQl操作，我在这里仅仅实现三个CQl语句，其他方法实现上完全一样。另外对于Repo而言，只要加上@RepositoryRestResource这一annotation便能自动实现基本的查询功能，并能通过制定的URL返回对应的JSON文件。另外，注意CQL返回的数据结构需要与java中的数据类型匹配，详情见下方代码的注释，选取代码如下：
```java
@RepositoryRestResource(collectionResourceRel = "employees", path = "employees")
public interface EmployeeRepo extends Neo4jRepository<Employee, Long> {
    @Query("MATCH (b:Employee)-[r:RATES]->(m:Employee), (b)-[s:SIMILARITY]-(t:Team {name:'Team ' + {0} })\n" +
            "WITH m, s.similarity AS similarity, r.rating AS rating\n" +
            "ORDER BY m.name, similarity DESC\n" +
            "WITH m.name AS candidate, COLLECT(rating)[0..5] AS ratings\n" +
            "WITH candidate, REDUCE(s = 0, i IN ratings | s + i)*1.0 / LENGTH(ratings) AS reco\n" +
            "ORDER BY reco DESC\n" +
            "RETURN candidate AS Candidate, toFloat(reco) AS Recommendation")
    Iterable<Map<String, Float>> excludeEmployeeUpdate(@Param("teamNo") String teamNo); //注意这里返回对象的格式，由于可能存在多个推荐人员，因此需要使用Iterable进行封装
}
```
自动产生的response如下所示：
``` json
{
    "_embedded": {
        "employees": [
            {
                "name": "Employee 1",
                "workingExperiences": [
                    {
                        "duration": 2,
                        "location": "Location 1",
                        "_links": {
                            "role": {
                                "href": "http://127.0.0.1:8080/roles/7"
                            },
                            "employee": {
                                "href": "http://127.0.0.1:8080/employees/0"
                            }
                        }
                    }
                ],
                "_links": {
                    "self": {
                        "href": "http://127.0.0.1:8080/employees/0"
                    },
                    "employee": {
                        "href": "http://127.0.0.1:8080/employees/0"
                    }
                }
            },...]}
}
```
通过exclude方法返回的JSON格式如下所示：
``` json
[
    {
        "Candidate": "Employee 1",
        "Recommendation": 4
    },
    {
        "Candidate": "Employee 3",
        "Recommendation": 3
    }
]
```

### 4. Service设计
Service层只要对请求进行一些简单处理，并调用Repo层进行运算即可，部分代码如下：
``` java
@Service
public class JobService {
    private final static Logger LOG = LoggerFactory.getLogger(JobService.class);

    private EmployeeRepo employeeRepo;
    private RoleRepo roleRepo;

    public JobService(EmployeeRepo employeeRepo,RoleRepo roleRepo) {
        this.employeeRepo = employeeRepo;
        this.roleRepo = roleRepo;
    }

    public Iterable<Map<String, Float>>excludeEmployeeUpdate(String teamNo){
        System.out.print(employeeRepo.excludeEmployeeUpdate(teamNo));
        return employeeRepo.excludeEmployeeUpdate(teamNo);
    }
}
```

### 5. Controller设计
Controller层负责Map Request，但是，由于Spring Boot还需要实现html网站的跳转，因此这里构建一个htmlController专门负责网页间跳转，部分代码如下所示：
``` java
@RestController
@RequestMapping("/")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @RequestMapping(value="/exclude")
    public List<Employee> excludeEmployee(@RequestParam String area, @RequestParam String competence){
        return jobService.excludeEmployee(area, competence);
    }

    @RequestMapping(value="/excludeUpdate")
    public Iterable<Map<String, Float>>excludeEmployeeUpdate(@RequestParam String teamNo){
        return jobService.excludeEmployeeUpdate(teamNo);
    }
}

@Controller
@RequestMapping("/")
public class htmlController {
    @RequestMapping("2019/01/05/Hello-Hexo/")
    public String FirstPage(){
        return "redirect:/2019/01/05/Hello-Hexo/index.html"; // 若要实现网页间跳转，必须使用redirect，且annotation必须为Controller
    }
}
```

## 前端页面介绍

对于前端页面，由于国内Neo4j中文资源较少，因此我希望能够做一个把此次项目详细介绍的前端页面，覆盖以上及以下的所有论文内容。首先，可以使用Hexo搭建一个博客模版平台，随后在博客生成端页面中进行一定的定制化处理，从而解决静态模版的一些局限性，例如于后段交互等。前段网站架构如下所示：

![前端项目结构](https://github.com/Andyyesiyu/Neo4jJobRecommendation/blob/master/src/main/resources/public/image/frontendstruc.jpg)

界面截图如下所示：
![文字显示](https://github.com/Andyyesiyu/Neo4jJobRecommendation/blob/master/src/main/resources/public/image/front1.jpg)

![代码显示](https://github.com/Andyyesiyu/Neo4jJobRecommendation/blob/master/src/main/resources/public/image/front2.jpg)

![demo展示1](https://github.com/Andyyesiyu/Neo4jJobRecommendation/blob/master/src/main/resources/public/image/front3.jpg)

![demo展示2](https://github.com/Andyyesiyu/Neo4jJobRecommendation/blob/master/src/main/resources/public/image/front4.jpg)

### 前后端连接

我采用协同评分法作为本次demo的方法，在搜索框中输入目标招人的Team号码，并点击开始匹配就能够将数据库搜索的结果和评分显示在下方的表格中。其中Ajax部分代码如下所示：
``` javascript
<script type="text/javascript">
$(
  function () {
    console.log("init");
    function search(evt) {
      var query = $("#search").find("input[name=search]").val();
      $.get("/excludeUpdate?teamNo="+ encodeURIComponent(query), function (data) {
        var t = $("table#results tbody").empty();
        if (!data) return;
        data.forEach(function (result) {
          $(`<tr><td>`+result.Candidate+`<td style="text-align:center">`+result.Recommendation+`</td></tr>`).appendTo(t);
        })
      })
      return false;
    }
    $("#search").submit(search);
  }
)
</script>
```

``` html
<div>
    <form role="search" class="navbar-form" id="search">
        <div style="zoom: 1; padding: 12px; position: relative;
              display: inline-block;">
            <input type="text" value="3" placeholder="请输入您想要匹配的Team号" class="form-control" name="search">
            <button class="btn btn-default" type="submit">开始匹配</button>
        </div>
    </form>
    <table id="results">
        <thead>
            <tr>
                <th>Candidate</th>
                <th style="text-align:center">Recommendation</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
</div>
```

## 结语
本项目对基于图数据库的内部职位搜索引擎进行了探索和初步实现。另外对于WebService进行了实践性的认识，包括以Java Spring Boot为框架的后端，以及以Hexo为框架进行定制化的前端。获益良多。本次项目所有代码可以在我的Github上找到。

### 项目未来规划
1. 将整个项目部署至云端，其中涉及云端Linux Java和Neo4j部署
2. 将涉及的图数据库职位匹配算法进一步完善，并比较各种方法的优劣
3. 代码有关的文档整理

### 附录: 参考资料
1. Neo4j Graphgist https://neo4j.com/graphgist/competency-management-a-matter-of-filtering-and-recommendation-engines
2. Neo4j Spring Manual https://docs.spring.io/spring-data/neo4j/docs/5.0.x/reference/html
3. W3C CQL https://www.w3cschool.cn/neo4j/neo4j_directional_relationships.html
4. HEXO https://hexo.io/docs/writing
5. Jianshu https://www.jianshu.com/p/3423fa97d185
6. CSS Reference 
7. Pandoc https://pandoc.org/
