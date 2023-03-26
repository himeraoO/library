package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Author;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AuthorDAO {
    Optional<Author> findById(int id) throws SQLException;

    List<Author> findAll() throws SQLException;

    int save(Author author) throws SQLException;

    int update(Author author) throws SQLException;

    int deleteById(int id) throws SQLException;
}
