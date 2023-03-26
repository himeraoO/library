package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Author;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AuthorDAO {
    Optional<Author> findAuthorById(int authorId, Connection connection) throws SQLException;

    List<Author> findAllAuthor(Connection connection) throws SQLException;

    int saveAuthor(Author author, Connection connection) throws SQLException;

    int updatedAuthor(Author author, Connection connection) throws SQLException;

    int deleteAuthor(int authorId, Connection connection) throws SQLException;
}
