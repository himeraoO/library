package com.github.himeraoo.library.jdbc;

import com.github.himeraoo.library.dao.AuthorRepositoryImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SessionManagerJDBC implements SessionManager {
    private Connection connection;
    private static final int TIMEOUT_IN_SECONDS = 10;
    private final String url;
    private final String username;
    private final String password;

    public SessionManagerJDBC() {
        url = "jdbc:mysql://localhost:3306/library_db";
        username = "root";
        password = "1234598760";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
        try{
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public void finishTransaction() {
        try{
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
}
