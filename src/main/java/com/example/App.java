package com.example;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import com.example.models.Role;
import com.example.models.User;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class App implements AutoCloseable
{
    private final SessionFactory factory;
    private final Session session;

    public App(String uri, String user, String password) {
        org.neo4j.driver.Driver driver = GraphDatabase.driver(uri, AuthTokens.none());
        Driver ogmDriver = new BoltDriver(driver);
        factory = new SessionFactory(ogmDriver, "com.example");
        session = factory.openSession();
    }

    @Override
    public void close() throws RuntimeException {
        factory.close();
    }

    public void createUserHasRole(List<User> users) {
        users.forEach(session::save);
    }

    public void isUserAuthorized(String username, String role) {
        Boolean result = session.queryForObject(Boolean.class, "RETURN EXISTS {" +
                "  MATCH (u:User)-[*]->(r:Role)" +
                "  WHERE u.username = $username" +
                "    AND r.name = $role" +
                "} as hasRole", Map.of("username", username, "role", role));
        String response = result ? String.format("User %s has role %s", username, role) : String.format("User %s does not have role %s", username, role);
        System.out.println(response);
    }

    public static void main(String... args) {
        try (var authz = new App("bolt://localhost:7687", "neo4j", "password")) {
            Role admin = new Role("admin-catalog", Set.of(new Role("view-product", Collections.emptySet()), new Role("edit-product", Collections.emptySet())));
            Role analyst = new Role("analyst-catalog", Set.of(new Role("view-product", Collections.emptySet())));
            User paula = new User("paula", Set.of(analyst));
            User richard = new User("richard", Set.of(admin));

            authz.createUserHasRole(List.of(paula, richard));
            authz.isUserAuthorized("paula", "analyst-catalog"); // User paula has role analyst-catalog
            authz.isUserAuthorized("paula", "edit-product"); // User paula does not have role edit-product
            authz.isUserAuthorized("richard", "admin-catalog"); // User richard has role admin-catalog
            authz.isUserAuthorized("richard", "edit-product"); // User richard has role edit-product
            authz.isUserAuthorized("richard", "view-product"); // User richard has role view-product
            authz.isUserAuthorized("richard", "fake-role"); // User richard does not have role fake-role
        }
    }
}
