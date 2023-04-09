package com.github.himeraoo.library.jdbc;

import com.github.himeraoo.library.exception.SessionManagerException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SessionManagerJDBC implements SessionManager {
    private static final int TIMEOUT_IN_SECONDS = 10;
    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

    public SessionManagerJDBC(String urlProp, String usernameProp, String passwordProp, String driver) {

        this.url = urlProp;
        this.username = usernameProp;
        this.password = passwordProp;

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        initBD();
    }

    @Override
    public void beginSession() {
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public void startTransaction() {
        checkConnection();
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public void finishTransaction() {
        checkConnection();
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public void commitSession() {
        checkConnection();
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public void rollbackSession() {
        checkConnection();
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public void close() {
        checkConnection();
        try {
            connection.close();
        } catch (SQLException e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public Connection getCurrentSession() {
        checkConnection();
        return connection;
    }

    private void checkConnection() {
        try {
            if (connection == null || !connection.isValid(TIMEOUT_IN_SECONDS)) {
                throw new SessionManagerException("Connection is invalid");
            }
        } catch (SQLException ex) {
            throw new SessionManagerException(ex);
        }
    }

    private void initBD() {
        beginSession();

        try {
            try (PreparedStatement statement = getCurrentSession().prepareStatement("CREATE TABLE IF NOT EXISTS author (\n" + "        id INT NOT NULL AUTO_INCREMENT,\n" + "        name VARCHAR(255) NOT NULL,\n" + "        surname VARCHAR(255) NOT NULL,\n" + "        PRIMARY KEY (id))\n" + "    ENGINE = InnoDB")) {
                statement.execute();
            }

            try (PreparedStatement statement = getCurrentSession().prepareStatement("CREATE TABLE IF NOT EXISTS genre (\n" + "        id INT NOT NULL AUTO_INCREMENT,\n" + "        name VARCHAR(255) NOT NULL,\n" + "        PRIMARY KEY (id))\n" + "    ENGINE = InnoDB")) {
                statement.execute();
            }

            try (PreparedStatement statement = getCurrentSession().prepareStatement("CREATE TABLE IF NOT EXISTS book (\n" + "        id INT NOT NULL AUTO_INCREMENT,\n" + "        title VARCHAR(255) NOT NULL,\n" + "        genre_id INT NOT NULL,\n" + "        PRIMARY KEY (id),\n" + "        FOREIGN KEY (genre_id)\n" + "            REFERENCES genre (id)\n" + "            ON DELETE NO ACTION\n" + "            ON UPDATE NO ACTION)\n" + "    ENGINE = InnoDB")) {
                statement.execute();
            }

            try (PreparedStatement statement = getCurrentSession().prepareStatement("CREATE TABLE IF NOT EXISTS authors_books (\n" + "        author_id INT NOT NULL,\n" + "        book_id INT NOT NULL,\n" + "        PRIMARY KEY (author_id, book_id),\n" + "        FOREIGN KEY (author_id)\n" + "         REFERENCES author (id)\n" + "         ON DELETE NO ACTION\n" + "         ON UPDATE NO ACTION,\n" + "        FOREIGN KEY (book_id)\n" + "         REFERENCES book (id)\n" + "         ON DELETE NO ACTION\n" + "         ON UPDATE NO ACTION)\n" + "    ENGINE = InnoDB")) {
                statement.execute();
            }

            close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
