package com.github.himeraoo.library.dao;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.himeraoo.library.util.TestUtils.getFullAuthor;
import static com.github.himeraoo.library.util.TestUtils.getFullBook;
import static com.github.himeraoo.library.util.TestUtils.getFullGenre;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseDAOTest {

    protected static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql")
            .withDatabaseName("mydb")
            .withUsername("myuser")
            .withPassword("mypass")
            .withInitScript("db.sql");

    static {
        mySQLContainer.start();
    }
}
