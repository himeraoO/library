package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Book;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BookDAO {

    Optional<Book> findById(int id) throws SQLException;

    List<Book> findAll() throws SQLException;

    int save(Book book) throws SQLException;

    int update(Book book) throws SQLException;

    int deleteById(int id) throws SQLException;
}
