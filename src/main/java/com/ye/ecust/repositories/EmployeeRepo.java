package com.ye.ecust.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ye.ecust.domain.Employee;
import com.ye.ecust.domain.SuggestResult;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/**
 * Created by yesiyu on 2019/1/1.
 */


@RepositoryRestResource(collectionResourceRel = "employees", path = "employees")
public interface EmployeeRepo extends Neo4jRepository<Employee, Long> {

    @Query("MATCH (n) where n.name={name} RETURN n")
    Employee testFind(@Param("name") String name);

    @Query("MATCH (n:Employee)-[:HAS_DEGREE]-(:Degree {area:\"{area}\"}) WHERE (n)-[:HAS_SKILL]-(:Personal_Skill)<-[:REQUIRES]-(:Activity)-[:IN_AREA]->(:Competence_area {name:\'competence {competence}\'}) WITH n AS person MATCH (person)-[r:WORKS_AS]-() WHERE r.duration>1 RETURN person AS `Matching Candidate`")
    List<Employee> excludeEmployee(@Param("area") String area, @Param("competence") String competence);

    @Query("MATCH (b:Employee)-[r:RATES]->(m:Employee), (b)-[s:SIMILARITY]-(t:Team {name:'Team ' + {0} })\n" +
            "WITH m, s.similarity AS similarity, r.rating AS rating\n" +
            "ORDER BY m.name, similarity DESC\n" +
            "WITH m.name AS candidate, COLLECT(rating)[0..5] AS ratings\n" +
            "WITH candidate, REDUCE(s = 0, i IN ratings | s + i)*1.0 / LENGTH(ratings) AS reco\n" +
            "ORDER BY reco DESC\n" +
            "RETURN candidate AS Candidate, toFloat(reco) AS Recommendation")
    Iterable<Map<String, Float>> excludeEmployeeUpdate(@Param("teamNo") String teamNo); //todo list not matched with return value;

}
