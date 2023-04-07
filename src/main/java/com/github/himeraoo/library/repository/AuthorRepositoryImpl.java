package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.AuthorDAO;
import com.github.himeraoo.library.dao.BookDAO;
import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class AuthorRepositoryImpl implements AuthorRepository {
    private final SessionManager sessionManager;
    private final AuthorDAO authorDAO;
    private final GenreDAO genreDAO;
    private final BookDAO bookDAO;


    public AuthorRepositoryImpl(SessionManager sessionManager, AuthorDAO authorDAO, GenreDAO genreDAO, BookDAO bookDAO) {
        this.sessionManager = sessionManager;
        this.authorDAO = authorDAO;
        this.genreDAO = genreDAO;
        this.bookDAO = bookDAO;
    }

    @Override
    public Optional<Author> findById(int authorId) throws SQLException {
        sessionManager.beginSession();

        Optional<Author> optionalAuthor;

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalAuthor = authorDAO.findAuthorById(authorId, connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
        return optionalAuthor;
    }

    @Override
    public List<Author> findAll() throws SQLException {
        sessionManager.beginSession();

        List<Author> authorList;

        try (Connection connection = sessionManager.getCurrentSession()) {
            authorList = authorDAO.findAllAuthor(connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
        return authorList;
    }

    @Override
    public int save(Author author) throws SQLException {

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            //сохранение автора
            int authorId = authorDAO.saveAuthor(author, connection);

            //сохранение списка книг автора, если они отсутствуют в бд
            List<Book> bookList = author.getBookList();
            //список авторов связанных с книгой
            List<Book> listBookFromBD = authorDAO.getBookListFromBDByAuthorId(authorId, connection);

            if (!bookList.isEmpty()){
                //общие книги между переданным списком и тех что в БД
                List<Book> commonElements = bookList.stream().filter(listBookFromBD::contains).collect(Collectors.toList());
                //новые книги которые надо добавить
                List<Book> forAdded = bookList.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());
                //книги с которыми надо удалить связи
                List<Book> forRemoveRelation = listBookFromBD.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());

                List<Genre> genreList = genreDAO.findAllGenre(connection);

                for (Book b :forAdded) {
                    Genre genre = b.getGenre();
                    if (!genreList.contains(genre)){
                        int addedGenre = genreDAO.saveGenre(genre, connection);
                        genre.setId(addedGenre);
                        b.setGenre(genre);
                    }else {
                        Genre genreDB = genreList.get(genreList.indexOf(genre));
                        b.setGenre(genreDB);
                    }
                    int bookId = bookDAO.saveBook(b, connection);
                    authorDAO.addRelationAuthorBook(authorId, bookId, connection);
                }

                //удаление связей
                authorDAO.removeRelationBookAuthor(authorId, connection, forRemoveRelation);
            }else{
                //удаление связей
                authorDAO.removeRelationBookAuthor(authorId, connection, listBookFromBD);
            }

            sessionManager.commitSession();
            sessionManager.finishTransaction();
            return authorId;
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

            List<Book> bookList = author.getBookList();
            //список авторов связанных с книгой
            List<Book> listBookFromBD = authorDAO.getBookListFromBDByAuthorId(author.getId(), connection);

            if (!bookList.isEmpty()){
                //общие книги между переданным списком и тех что в БД
                List<Book> commonElements = bookList.stream().filter(listBookFromBD::contains).collect(Collectors.toList());
                //новые книги которые надо добавить
                List<Book> forAdded = bookList.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());
                //книги с которыми надо удалить связи
                List<Book> forRemoveRelation = listBookFromBD.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());

                ArrayList<Genre> genreList = (ArrayList<Genre>) genreDAO.findAllGenre(connection);

                for (Book b :forAdded) {
                    Genre genre = b.getGenre();
                    if (!genreList.contains(genre)){
                        int addedGenre = genreDAO.saveGenre(genre, connection);
                        genre.setId(addedGenre);
                        b.setGenre(genre);
                    }else {
                        Genre genre1 = genreList.get(genreList.indexOf(genre));
                        b.setGenre(genre1);
                    }
                    int bookId = bookDAO.saveBook(b, connection);
                    authorDAO.addRelationAuthorBook(author.getId(), bookId, connection);
                }

                //удаление связей
                authorDAO.removeRelationBookAuthor(author.getId(), connection, forRemoveRelation);
            }else{
                //удаление связей
                authorDAO.removeRelationBookAuthor(author.getId(), connection, listBookFromBD);
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
    public int deleteById(int authorId) throws SQLException {

        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {

            sessionManager.startTransaction();

            Optional<Author> optionalAuthor = authorDAO.findAuthorById(authorId, connection);

            if (optionalAuthor.isPresent()){
                Author dbAuthor = optionalAuthor.get();
                authorDAO.removeRelationBookAuthor(authorId, connection, dbAuthor.getBookList());
                rowsUpdated = authorDAO.deleteAuthor(authorId, connection);
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
