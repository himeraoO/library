package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Genre;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface GenreDAO {
    Optional<Genre> findGenreById(int genreId, Connection connection) throws SQLException;

    List<Genre> findAllGenre(Connection connection) throws SQLException;

    int saveGenre(Genre genre, Connection connection) throws SQLException;

    int updatedGenre(Genre genre, Connection connection) throws SQLException;

    int deleteGenre(int genreId, Connection connection) throws SQLException;

    int countGenreByName(String genreName, Connection connection) throws SQLException;
}
