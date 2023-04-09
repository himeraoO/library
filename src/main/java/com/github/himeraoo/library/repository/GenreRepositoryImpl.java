package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.BookDAO;
import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Genre;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GenreRepositoryImpl implements GenreRepository {

    private final SessionManager sessionManager;

    private final GenreDAO genreDAO;
    private final BookDAO bookDAO;

    public GenreRepositoryImpl(SessionManager sessionManager, GenreDAO genreDAO, BookDAO bookDAO) {
        this.sessionManager = sessionManager;
        this.genreDAO = genreDAO;
        this.bookDAO = bookDAO;
    }

    @Override
    public Optional<Genre> findById(int genreId) throws SQLException {
        sessionManager.beginSession();

        Optional<Genre> optionalGenre;
        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalGenre = genreDAO.findGenreById(genreId, connection);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
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

            int genreCount = genreDAO.countGenreByName(genre.getName(), connection);
            if (genreCount == 0) {
                genreId = genreDAO.saveGenre(genre, connection);
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

            int genreCount = genreDAO.countGenreByName(genre.getName(), connection);
            if (genreCount == 0) {
                rowsUpdated = genreDAO.updatedGenre(genre, connection);
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

            int bookCount = bookDAO.countBookByGenreId(genreId, connection);
            if (bookCount == 0) {
                rowsUpdated = genreDAO.deleteGenre(genreId, connection);
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
