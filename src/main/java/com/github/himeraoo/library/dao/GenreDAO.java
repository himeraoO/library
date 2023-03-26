package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Genre;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface GenreDAO {
    Optional<Genre> findById(int id) throws SQLException;

    List<Genre> findAll() throws SQLException;

    int save(Genre genre) throws SQLException;

    int update(Genre genre) throws SQLException;

    int deleteById(int id) throws SQLException;
}
