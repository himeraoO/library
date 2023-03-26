package com.github.himeraoo.library.dao;

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

    public AuthorRepositoryImpl(SessionManager sessionManager, AuthorDAO authorDAO) {
        this.sessionManager = sessionManager;
        this.authorDAO = authorDAO;
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
            updateAndSaveBooks(author.getId(), author.getBookList(), connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();
            return id;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    private void updateAndSaveBooks(int authorId, List<Book> bookList, Connection connection) throws SQLException {
        //список авторов связанных с книгой
        List<Book> listBookFromBD = getBookListFromBD(authorId, connection);

        if (!bookList.isEmpty()){
            //общие книги между переданным списком и тех что в БД
            List<Book> commonElements = bookList.stream().filter(listBookFromBD::contains).collect(Collectors.toList());
            //новые книги которые надо добавить
            List<Book> forAdded = bookList.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());
            //книги с которыми надо удалить связи
            List<Book> forRemoveRelation = listBookFromBD.stream().filter(i -> !commonElements.contains(i)).collect(Collectors.toList());
            //добавление недостающих жанров
            checkAndAddGenreListFromBookList(bookList, connection);
            //добавление новых книг
            addBookList(connection, forAdded);
            //добавление связей
            addRelationBookAuthor(authorId, connection, forAdded);
            //удаление связей
            removeRelationBookAuthor(authorId, connection, forRemoveRelation);
        }else{
            //удаление связей
            removeRelationBookAuthor(authorId, connection, listBookFromBD);
        }
    }

    private void checkAndAddGenreListFromBookList(List<Book> bookList, Connection connection) throws SQLException {
        for (Book book:bookList) {
            checkAddGenre(book.getGenre(), connection);
        }
    }

    private void checkAddGenre(Genre genre, Connection connection) throws SQLException {
        Optional<Genre> optionalGenre = findGenreById(genre.getId(), connection);
        if (!optionalGenre.isPresent()){
            saveGenre(genre, connection);
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

    private List<Book> getBookListFromBD(int authorId, Connection connection) throws SQLException {
        List<Book> listBookFromBD = new ArrayList<>();

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AllBookFindByAuthorId.QUERY)) {
            pst.setInt(1, authorId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(Integer.parseInt(rs.getString("bid")));
                    book.setTitle(rs.getString("btitle"));

                    Genre genre = new Genre();
                    genre.setId(Integer.parseInt(rs.getString("gid")));
                    genre.setName(rs.getString("gname"));

                    book.setGenre(genre);

                    listBookFromBD.add(book);
                }
            }
        }
        return listBookFromBD;
    }

    private void addBookList(Connection connection, List<Book> forAdded) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookSave.QUERY)){
            for (Book book : forAdded) {
                pst.setString(1, book.getTitle());
                pst.setInt(2, book.getGenre().getId());
                pst.executeUpdate();
            }
        }
    }

    private void addRelationBookAuthor(int authorId, Connection connection, List<Book> forAdded) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
            for (Book book : forAdded) {
                pst.setInt(1, authorId);
                pst.setInt(2, book.getId());
                pst.executeUpdate();
            }
        }
    }

    private void removeRelationBookAuthor(int author_id, Connection connection, List<Book> forRemoveRelation) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {
            for (Book book : forRemoveRelation) {
                pst.setInt(1, author_id);
                pst.setInt(2, book.getId());
                pst.executeUpdate();
            }
        }
    }

    @Override
    public int update(Author author) throws SQLException {

        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            rowsUpdated = authorDAO.updatedAuthor(author, connection);

            updateAndSaveBooks(author.getId(), author.getBookList(), connection);

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
                removeRelationBookAuthor(authorId, connection, dbAuthor.getBookList());
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
