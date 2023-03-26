package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.models.Book;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Optional<Book> findById(int bookId) throws SQLException;

    List<Book> findAll() throws SQLException;

    int save(Book book) throws SQLException;

    int update(Book book) throws SQLException;

    int deleteById(int bookId) throws SQLException;
}
