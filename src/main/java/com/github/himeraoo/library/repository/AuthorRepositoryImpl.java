package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.AuthorDAO;
import com.github.himeraoo.library.dao.AuthorsBooksDAO;
import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Author;

import java.sql.*;
import java.util.List;
import java.util.Optional;


public class AuthorRepositoryImpl implements AuthorRepository {
    private final SessionManager sessionManager;
    private final AuthorDAO authorDAO;
    private final GenreDAO genreDAO;
    private final AuthorsBooksDAO authorsBooksDAO;


    public AuthorRepositoryImpl(SessionManager sessionManager, AuthorDAO authorDAO, GenreDAO genreDAO, AuthorsBooksDAO authorsBooksDAO) {
        this.sessionManager = sessionManager;
        this.authorDAO = authorDAO;
        this.genreDAO = genreDAO;
        this.authorsBooksDAO = authorsBooksDAO;
    }

    @Override
    public Optional<Author> findById(int authorId) throws SQLException {
        sessionManager.beginSession();
        try (Connection connection = sessionManager.getCurrentSession()) {
            return authorDAO.findAuthorById(authorId, connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public List<Author> findAll() throws SQLException {
        sessionManager.beginSession();
        try (Connection connection = sessionManager.getCurrentSession()) {
            return authorDAO.findAllAuthor(connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public int save(Author author) throws SQLException {

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            //сохранение автора
            int id = authorDAO.saveAuthor(author, connection);

            //сохранение списка книг автора, если они отсутствуют в бд
            //добавление связи между автором и книгами
            authorsBooksDAO.updateAndSaveBooks(author.getId(), author.getBookList(), connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();
            return id;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public int update(Author author) throws SQLException {

        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            rowsUpdated = authorDAO.updatedAuthor(author, connection);

            authorsBooksDAO.updateAndSaveBooks(author.getId(), author.getBookList(), connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();

            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public int deleteById(int authorId) throws SQLException {

        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {

            sessionManager.startTransaction();

            Optional<Author> optionalAuthor = authorDAO.findAuthorById(authorId, connection);

            if (optionalAuthor.isPresent()){
                Author dbAuthor = optionalAuthor.get();
                authorsBooksDAO.removeRelationBookAuthor(authorId, connection, dbAuthor.getBookList());
                rowsUpdated = authorDAO.deleteAuthor(authorId, connection);
            }

            sessionManager.finishTransaction();
            sessionManager.commitSession();
            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }
}
