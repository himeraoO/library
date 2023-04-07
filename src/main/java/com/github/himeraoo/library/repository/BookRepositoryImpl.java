package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.AuthorDAO;
import com.github.himeraoo.library.dao.BookDAO;
import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class BookRepositoryImpl implements BookRepository {

    private final SessionManager sessionManager;
    private final BookDAO bookDAO;
    private final GenreDAO genreDAO;
    private final AuthorDAO authorDAO;

    public BookRepositoryImpl(SessionManager sessionManager, BookDAO bookDAO, GenreDAO genreDAO, AuthorDAO authorDAO) {
        this.sessionManager = sessionManager;
        this.bookDAO = bookDAO;
        this.genreDAO = genreDAO;
        this.authorDAO = authorDAO;
    }

    @Override
    public Optional<Book> findById(int bookId) throws SQLException {
        sessionManager.beginSession();

        Optional<Book> optionalBook;

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalBook = bookDAO.findBookById(bookId, connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
        return optionalBook;
    }

    @Override
    public List<Book> findAll() throws SQLException {
        sessionManager.beginSession();

        List<Book> bookList;

        try (Connection connection = sessionManager.getCurrentSession()) {
            bookList = bookDAO.findAllBook(connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
        return bookList;
    }

    @Override
    public int save(Book book) throws SQLException {
        sessionManager.beginSession();

        int bookId = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            List<Genre> genreList = genreDAO.findAllGenre(connection);

            Genre genre = book.getGenre();
            if (!genreList.contains(genre)){
                int addedGenre = genreDAO.saveGenre(genre, connection);
                genre.setId(addedGenre);
                book.setGenre(genre);
            }else {
                Genre genreDB = genreList.get(genreList.indexOf(genre));
                book.setGenre(genreDB);
            }

            //сохранение книги
            bookId = bookDAO.saveBook(book, connection);

            //сохранение списка авторов книги, если они отсутствуют в бд
            //добавление связи между книгой и авторами

            List<Author> authorList = book.getAuthorList();

            //список авторов связанных с книгой
            List<Author> listAuthorFromBD = bookDAO.getAuthorListFromBDByBookId(bookId, connection);

            if (!authorList.isEmpty()){
                //общие авторы между переданным списком и тех что в БД
                List<Author> commonElements = authorList.stream().filter(listAuthorFromBD::contains).collect(Collectors.toList());
                //новые авторы которые надо добавить
                List<Author> forAdded = authorList.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());
                //авторы с которыми надо удалить связи
                List<Author> forRemoveRelation = listAuthorFromBD.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());

                for (Author a: forAdded) {
                    int added = authorDAO.saveAuthor(a, connection);
                    bookDAO.addRelationAuthorBook(added, bookId, connection);
                }

                //удаление связей
                bookDAO.removeRelationAuthorBook(bookId, connection, forRemoveRelation);
            }else{
                //удаление связей
                bookDAO.removeRelationAuthorBook(bookId, connection, listAuthorFromBD);
            }

            sessionManager.commitSession();
            sessionManager.finishTransaction();
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
        return bookId;
    }

    @Override
    public int update(Book book) throws SQLException {


        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();
            int rowsUpdated = 0;
            List<Genre> genreList = genreDAO.findAllGenre(connection);

            Genre genre = book.getGenre();
            if (!genreList.contains(genre)){
                int addedGenre = genreDAO.saveGenre(genre, connection);
                genre.setId(addedGenre);
                book.setGenre(genre);
            }else {
                Genre genreDB = genreList.get(genreList.indexOf(genre));
                book.setGenre(genreDB);
            }

            rowsUpdated = bookDAO.updatedBook(book, connection);

            List<Author> authorList = book.getAuthorList();

            //список авторов связанных с книгой
            List<Author> listAuthorFromBD = bookDAO.getAuthorListFromBDByBookId(book.getId(), connection);

            if (!authorList.isEmpty()){
                //общие авторы между переданным списком и тех что в БД
                List<Author> commonElements = authorList.stream().filter(listAuthorFromBD::contains).collect(Collectors.toList());
                //новые авторы которые надо добавить
                List<Author> forAdded = authorList.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());
                //авторы с которыми надо удалить связи
                List<Author> forRemoveRelation = listAuthorFromBD.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());

                for (Author a: forAdded) {
                    int added = authorDAO.saveAuthor(a, connection);
                    if(added != 0) {
                        bookDAO.addRelationAuthorBook(added, book.getId(), connection);
                    }
                }

                //удаление связей
                bookDAO.removeRelationAuthorBook(book.getId(), connection, forRemoveRelation);
            }else{
                //удаление связей
                bookDAO.removeRelationAuthorBook(book.getId(), connection, listAuthorFromBD);
            }

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
                bookDAO.removeRelationAuthorBook(bookId, connection, dbBook.getAuthorList());
                rowsUpdated = bookDAO.deleteBook(bookId, connection);
            }

            sessionManager.commitSession();
            sessionManager.finishTransaction();
            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }
}
