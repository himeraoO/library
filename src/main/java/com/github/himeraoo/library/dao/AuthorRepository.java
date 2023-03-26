package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Author;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AuthorRepository {
    Optional<Author> findById(int authorId) throws SQLException;

    List<Author> findAll() throws SQLException;

    int save(Author author) throws SQLException;

    int update(Author author) throws SQLException;

    int deleteById(int authorId) throws SQLException;
}
