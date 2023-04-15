package com.github.himeraoo.library.dao;

import org.testcontainers.containers.MySQLContainer;

public class BaseDAOWithBDTest {
    protected static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql")
            .withDatabaseName("mydb")
            .withUsername("myuser")
            .withPassword("mypass")
            .withInitScript("db.sql");
}
