package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.dao.jdbc.SessionManager;
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
    public Optional<Genre> findById(int id) throws SQLException {

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {

            return genreDAO.findGenreById(id, connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public List<Genre> findAll() throws SQLException {

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {

            return genreDAO.findAllGenre(connection);

        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public int save(Genre genre) throws SQLException {
        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            int id = genreDAO.saveGenre(genre, connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();
            return id;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
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
    public int deleteById(int id) throws SQLException {
        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {

            sessionManager.startTransaction();

            rowsUpdated = genreDAO.deleteGenre(id, connection);

            sessionManager.finishTransaction();
            sessionManager.commitSession();
            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }
}
