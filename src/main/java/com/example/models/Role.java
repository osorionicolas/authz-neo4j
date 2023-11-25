package com.example.models;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NodeEntity
public record Role (
        @Id String name,
        @Relationship(type = "parent", direction = Relationship.Direction.OUTGOING) Set<Role> children
) {}
