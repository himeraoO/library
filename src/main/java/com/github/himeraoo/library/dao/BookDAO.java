package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Book;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public interface BookDAO {
    Optional<Book> findBookById(int bookId, Connection connection) throws SQLException;

    List<Book> findAllBook(Connection connection) throws SQLException;

    int saveBook(Book book, Connection connection) throws SQLException;

    int updatedBook(Book book, Connection connection) throws SQLException;

    int deleteBook(int bookId, Connection connection) throws SQLException;
}
