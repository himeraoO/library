package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Genre;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class GenreDAOImpl implements GenreDAO {

    public Optional<Genre> findGenreById(int genreId, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindById.QUERY)) {
            pst.setInt(1, genreId);

            Genre dbGenre = new Genre();

            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                dbGenre.setId((Integer.parseInt(rs.getString("id"))));
                dbGenre.setName((rs.getString("name")));
            }
            return Optional.ofNullable(dbGenre);
        }
    }

    public List<Genre> findAllGenre(Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindAll.QUERY)) {

            List<Genre> genreList = null;

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {

                    Genre dbGenre = new Genre();
                    dbGenre.setId((Integer.parseInt(rs.getString("id"))));
                    dbGenre.setName((rs.getString("name")));

                    genreList.add(dbGenre);
                }
            }
            return genreList;
        }
    }

    public int saveGenre(Genre genre, Connection connection) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, genre.getName());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int updatedGenre(Genre genre, Connection connection) throws SQLException {
        int rowsUpdated;
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreUpdateById.QUERY)) {

            pst.setString(1, genre.getName());
            pst.setInt(2, genre.getId());

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }

    public int deleteGenre(int genreId, Connection connection) throws SQLException {
        int rowsUpdated;
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreDeleteById.QUERY)) {
            pst.setInt(1, genreId);

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }
}
