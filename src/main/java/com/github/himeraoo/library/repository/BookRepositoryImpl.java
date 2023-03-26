package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.AuthorsBooksDAO;
import com.github.himeraoo.library.dao.BookDAO;
import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Book;

import java.sql.*;
import java.util.*;

public class BookRepositoryImpl implements BookRepository {

    private final SessionManager sessionManager;
    private final BookDAO bookDAO;
    private final GenreDAO genreDAO;
    private final AuthorsBooksDAO authorsBooksDAO;

    public BookRepositoryImpl(SessionManager sessionManager, BookDAO bookDAO, GenreDAO genreDAO, AuthorsBooksDAO authorsBooksDAO) {
        this.sessionManager = sessionManager;
        this.bookDAO = bookDAO;
        this.genreDAO = genreDAO;
        this.authorsBooksDAO = authorsBooksDAO;
    }

    @Override
    public Optional<Book> findById(int bookId) throws SQLException {
        sessionManager.beginSession();
        try (Connection connection = sessionManager.getCurrentSession()) {
            return bookDAO.findBookById(bookId, connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public List<Book> findAll() throws SQLException {
        sessionManager.beginSession();
        try (Connection connection = sessionManager.getCurrentSession()) {
            return bookDAO.findAllBook(connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public int save(Book book) throws SQLException {
        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            genreDAO.checkAddGenre(book.getGenre(), connection);

            //сохранение книги
            int id = bookDAO.saveBook(book, connection);

            //сохранение списка авторов книги, если они отсутствуют в бд
            //добавление связи между книгой и авторами
            authorsBooksDAO.updateAndSaveAuthors(book.getId(), book.getAuthorList(), connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();
            return id;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public int update(Book book) throws SQLException {
        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            rowsUpdated = bookDAO.updatedBook(book, connection);

            authorsBooksDAO.updateAndSaveAuthors(book.getId(), book.getAuthorList(), connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();

            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public int deleteById(int bookId) throws SQLException {
        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {

            sessionManager.startTransaction();

            Optional<Book> optionalBook = bookDAO.findBookById(bookId, connection);

            if (optionalBook.isPresent()){
                Book dbBook = optionalBook.get();
                authorsBooksDAO.removeRelationAuthorBook(bookId, connection, dbBook.getAuthorList());
                rowsUpdated = bookDAO.deleteBook(bookId, connection);
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
