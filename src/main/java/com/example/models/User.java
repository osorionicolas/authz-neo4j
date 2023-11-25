package com.example.models;

import java.util.Set;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public record User (
    @Id String username,
    @Relationship(type = "assignee", direction = Relationship.Direction.OUTGOING) Set<Role> roles
){}  
