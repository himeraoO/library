package com.github.himeraoo.library.jdbc;

import java.sql.Connection;

public interface SessionManager extends AutoCloseable{
    void beginSession();

    void startTransaction();

    void finishTransaction();

    void commitSession();

    void rollbackSession();

    void close();

    Connection getCurrentSession();
}
