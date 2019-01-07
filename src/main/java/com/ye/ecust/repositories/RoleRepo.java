package com.ye.ecust.repositories;

import com.ye.ecust.domain.Role;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by yesiyu on 2019/1/1.
 */
@RepositoryRestResource(collectionResourceRel = "roles", path = "roles")
public interface RoleRepo extends Neo4jRepository<Role, Long> {

}
