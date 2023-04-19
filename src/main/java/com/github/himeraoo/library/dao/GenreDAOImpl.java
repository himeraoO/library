package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreDAOImpl implements GenreDAO {
//
//    @Override
//    public Optional<Genre> findGenreById(int genreId, Connection connection) throws SQLException {
//        Genre genre = null;
//        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindById.QUERY)) {
//            pst.setInt(1, genreId);
//            try (ResultSet rs = pst.executeQuery()) {
//                Genre dbGenre = new Genre();
//                while (rs.next()) {
//                    dbGenre.setId((Integer.parseInt(rs.getString("id"))));
//                    dbGenre.setName((rs.getString("name")));
//                }
//                if (dbGenre.getId() != 0) {
//                    genre = dbGenre;
//                }
//            }
//        }
//        return Optional.ofNullable(genre);
//    }
//
//    @Override
//    public List<Genre> findAllGenre(Connection connection) throws SQLException {
//        List<Genre> genreList = new ArrayList<>();
//        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindAll.QUERY)) {
//            try (ResultSet rs = pst.executeQuery()) {
//                while (rs.next()) {
//                    Genre dbGenre = new Genre();
//                    dbGenre.setId((Integer.parseInt(rs.getString("id"))));
//                    dbGenre.setName((rs.getString("name")));
//                    genreList.add(dbGenre);
//                }
//            }
//        }
//        return genreList;
//    }
//
//    @Override
//    public int saveGenre(Genre genre, Connection connection) throws SQLException {
//        int id = 0;
//        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
//            pst.setString(1, genre.getName());
//            pst.executeUpdate();
//
//            try (ResultSet rs = pst.getGeneratedKeys()) {
//                if (rs.next()) {
//                    id = rs.getInt(1);
//                }
//            }
//        }
//        return id;
//    }
//
//    @Override
//    public int updatedGenre(Genre genre, Connection connection) throws SQLException {
//        int rowsUpdated;
//        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreUpdateById.QUERY)) {
//
//            pst.setString(1, genre.getName());
//            pst.setInt(2, genre.getId());
//
//            rowsUpdated = pst.executeUpdate();
//        }
//        return rowsUpdated;
//    }
//
//    @Override
//    public int deleteGenre(int genreId, Connection connection) throws SQLException {
//        int rowsUpdated;
//        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreDeleteById.QUERY)) {
//            pst.setInt(1, genreId);
//
//            rowsUpdated = pst.executeUpdate();
//        }
//        return rowsUpdated;
//    }
//
//    @Override
//    public int countGenreByName(String genreName, Connection connection) throws SQLException {
//        int countRows = 0;
//        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_CountGenreByName.QUERY)) {
//            pst.setString(1, genreName);
//            try (ResultSet rs = pst.executeQuery()) {
//                if (rs.next()) {
//                    countRows = rs.getInt("Count(*)");
//                }
//            }
//        }
//        return countRows;
//    }
}
