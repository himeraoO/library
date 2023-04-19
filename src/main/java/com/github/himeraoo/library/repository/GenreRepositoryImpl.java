package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.SQLQuery;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreRepositoryImpl implements GenreRepository {

    private final SessionManager sessionManager;

    public GenreRepositoryImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Optional<Genre> findById(int genreId) throws SQLException {
        sessionManager.beginSession();

        Genre genre = null;
        try (Connection connection = sessionManager.getCurrentSession()) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindById.QUERY)) {
                pst.setInt(1, genreId);
                try (ResultSet rs = pst.executeQuery()) {
                    Genre dbGenre = new Genre();
                    while (rs.next()) {
                        dbGenre.setId((Integer.parseInt(rs.getString("id"))));
                        dbGenre.setName((rs.getString("name")));
                    }
                    if (dbGenre.getId() != 0) {
                        genre = dbGenre;
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return Optional.ofNullable(genre);
    }

    @Override
    public List<Genre> findAll() throws SQLException {
        sessionManager.beginSession();

        List<Genre> genreList = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindAll.QUERY)) {
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Genre dbGenre = new Genre();
                        dbGenre.setId((Integer.parseInt(rs.getString("id"))));
                        dbGenre.setName((rs.getString("name")));
                        genreList.add(dbGenre);
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return genreList;
    }

    @Override
    public int save(Genre genre) throws SQLException {
        sessionManager.beginSession();
        int genreId = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            int genreCount = 0;
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_CountGenreByName.QUERY)) {
                pst.setString(1, genre.getName());
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        genreCount = rs.getInt("Count(*)");
                    }
                }
            }
            if (genreCount == 0) {
                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
                    pst.setString(1, genre.getName());
                    pst.executeUpdate();

                    try (ResultSet rs = pst.getGeneratedKeys()) {
                        if (rs.next()) {
                            genreId = rs.getInt(1);
                        }
                    }
                }
            } else {
                genreId = -1;
            }

            sessionManager.commitSession();
            sessionManager.finishTransaction();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return genreId;
    }

    @Override
    public int update(Genre genre) throws SQLException {
        sessionManager.beginSession();
        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            int genreCount = 0;
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_CountGenreByName.QUERY)) {
                pst.setString(1, genre.getName());
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        genreCount = rs.getInt("Count(*)");
                    }
                }
            }
            if (genreCount == 0) {
                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreUpdateById.QUERY)) {
                    pst.setString(1, genre.getName());
                    pst.setInt(2, genre.getId());
                    rowsUpdated = pst.executeUpdate();
                }
            } else {
                rowsUpdated = -1;
            }

            sessionManager.commitSession();
            sessionManager.finishTransaction();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return rowsUpdated;
    }

    @Override
    public int deleteById(int genreId) throws SQLException {
        sessionManager.beginSession();
        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            int bookCount = 0;
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_CountBookByGenreId.QUERY)) {
                pst.setInt(1, genreId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        bookCount = rs.getInt("Count(*)");
                    }
                }
            }
            if (bookCount == 0) {
                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreDeleteById.QUERY)) {
                    pst.setInt(1, genreId);
                    rowsUpdated = pst.executeUpdate();
                }
            } else {
                rowsUpdated = -1;
            }

            sessionManager.commitSession();
            sessionManager.finishTransaction();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return rowsUpdated;
    }
}
