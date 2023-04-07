package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Genre;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class GenreRepositoryImpl implements GenreRepository {

    private final SessionManager sessionManager;

    private final GenreDAO genreDAO;

    public GenreRepositoryImpl(SessionManager sessionManager, GenreDAO genreDAO) {
        this.sessionManager = sessionManager;
        this.genreDAO = genreDAO;
    }

    @Override
    public Optional<Genre> findById(int genreId) throws SQLException {
        sessionManager.beginSession();

        Optional<Genre> optionalGenre;

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalGenre = genreDAO.findGenreById(genreId, connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
        return optionalGenre;
    }

    @Override
    public List<Genre> findAll() throws SQLException {
        sessionManager.beginSession();

        List<Genre> genreList;

        try (Connection connection = sessionManager.getCurrentSession()) {
            genreList = genreDAO.findAllGenre(connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
        return genreList;
    }

    @Override
    public int save(Genre genre) throws SQLException {
        sessionManager.beginSession();

        int id = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            id = genreDAO.saveGenre(genre, connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
        return id;
    }

    @Override
    public int update(Genre genre) throws SQLException {
        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            rowsUpdated = genreDAO.updatedGenre(genre, connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();

            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public int deleteById(int genreId) throws SQLException {
        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {

            sessionManager.startTransaction();

            rowsUpdated = genreDAO.deleteGenre(genreId, connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();
            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }
}
