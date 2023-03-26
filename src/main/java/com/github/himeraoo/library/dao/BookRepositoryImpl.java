package com.github.himeraoo.library.dao;

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

    public BookRepositoryImpl(SessionManager sessionManager, BookDAO bookDAO) {
        this.sessionManager = sessionManager;
        this.bookDAO = bookDAO;
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

            checkAddGenre(book.getGenre(), connection);

            //сохранение книги
            int id = bookDAO.saveBook(book, connection);

            //сохранение списка авторов книги, если они отсутствуют в бд
            //добавление связи между книгой и авторами
            updateAndSaveAuthors(book.getId(), book.getAuthorList(), connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();
            return id;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    private void updateAndSaveAuthors(int bookId, List<Author> authorList, Connection connection) throws SQLException {
        //список авторов связанных с книгой
        List<Author> listAuthorFromBD = getAuthorListFromBD(bookId, connection);

        if (!authorList.isEmpty()){
            //общие авторы между переданным списком и тех что в БД
            List<Author> commonElements = authorList.stream().filter(listAuthorFromBD::contains).collect(Collectors.toList());
            //новые авторы которые надо добавить
            List<Author> forAdded = authorList.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());
            //авторы с которыми надо удалить связи
            List<Author> forRemoveRelation = listAuthorFromBD.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());
            //добавление новых авторов
            addAuthorList(connection, forAdded);
            //добавление связей
            addRelationAuthorBook(bookId, connection, forAdded);
            //удаление связей
            removeRelationAuthorBook(bookId, connection, forRemoveRelation);
        }else{
            //удаление связей
            removeRelationAuthorBook(bookId, connection, listAuthorFromBD);
        }
    }

    private void addRelationAuthorBook(int bookId, Connection connection, List<Author> forAdded) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
            for (Author author : forAdded) {
                pst.setInt(1, author.getId());
                pst.setInt(2, bookId);
                pst.executeUpdate();
            }
        }
    }

    private List<Author> getAuthorListFromBD(int bookId, Connection connection) throws SQLException {
        List<Author> listAuthorFromBD = new ArrayList<>();

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AllAuthorFindByBookId.QUERY)) {
            pst.setInt(1, bookId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Author author = new Author();
                    author.setId(Integer.parseInt(rs.getString("aid")));
                    author.setName(rs.getString("aname"));
                    author.setSurname(rs.getString("asurname"));
                    listAuthorFromBD.add(author);
                }
            }
        }
        return listAuthorFromBD;
    }

    private void removeRelationAuthorBook(int bookId, Connection connection, List<Author> forRemoveRelation) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {

            for (Author author : forRemoveRelation) {
                pst.setInt(1, author.getId());
                pst.setInt(2, bookId);
                pst.executeUpdate();
            }
        }
    }

    private void addAuthorList(Connection connection, List<Author> forAdded) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorSave.QUERY)){
            for (Author author : forAdded) {
                pst.setString(1, author.getName());
                pst.setString(2, author.getSurname());
                pst.executeUpdate();
            }
        }
    }

    private void checkAddGenre(Genre genre, Connection connection) throws SQLException {
         Optional<Genre> optionalGenre = findGenreById(genre.getId(), connection);
         if (!optionalGenre.isPresent()){
             saveGenre(genre, connection);
         }
    }

    private int saveGenre(Genre genre, Connection connection) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, genre.getName());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Optional<Genre> findGenreById(int genreId, Connection connection) throws SQLException {
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

    @Override
    public int update(Book book) throws SQLException {
        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            rowsUpdated = bookDAO.updatedBook(book, connection);

            updateAndSaveAuthors(book.getId(), book.getAuthorList(), connection);

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
                removeRelationAuthorBook(bookId, connection, dbBook.getAuthorList());
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
