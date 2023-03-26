package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.dao.jdbc.SessionManager;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class BookDAOImpl implements BookDAO{

    private final SessionManager sessionManager;

    public BookDAOImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Optional<Book> findById(int id) throws SQLException {

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {

            return findBookById(id, connection);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    private Optional<Book> findBookById(int id, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindById.QUERY)) {
            pst.setInt(1, id);

            Book dbBook = new Book();
            List<Author> authors = new ArrayList<>();
            Genre genre = new Genre();
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    dbBook.setId(Integer.parseInt(rs.getString("bid")));
                    dbBook.setTitle(rs.getString("btitle"));

                    genre.setId((Integer.parseInt(rs.getString("gid"))));
                    genre.setName((rs.getString("gname")));

                    Author author = new Author();
                    author.setId(Integer.parseInt(rs.getString("aid")));
                    author.setName(rs.getString("aname"));
                    author.setSurname(rs.getString("asurname"));
                    authors.add(author);
                }
            }
            dbBook.setGenre(genre);
            dbBook.setAuthorList(authors);
            return Optional.ofNullable(dbBook);
        }
    }

    @Override
    public List<Book> findAll() throws SQLException {

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {

            return findAllBook(connection);

        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    private List<Book> findAllBook(Connection connection) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll.QUERY)) {

            List<Book> bookList = null;

            HashMap<Integer, Book> integerBookHashMap = new HashMap<>();

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {

                    Book dbBook = new Book();
                    dbBook.setId(Integer.parseInt(rs.getString("bid")));
                    dbBook.setTitle(rs.getString("btitle"));

                    Genre genre = new Genre();
                    genre.setId((Integer.parseInt(rs.getString("gid"))));
                    genre.setName((rs.getString("gname")));

                    Author author = new Author();
                    author.setId(Integer.parseInt(rs.getString("aid")));
                    author.setName(rs.getString("aname"));
                    author.setSurname(rs.getString("asurname"));

                    if (integerBookHashMap.containsKey(dbBook.getId())) {
                        integerBookHashMap.get(dbBook.getId()).getAuthorList().add(author);
                    } else {
                        List<Author> authorList = new ArrayList<>();
                        authorList.add(author);
                        dbBook.setGenre(genre);
                        dbBook.setAuthorList(authorList);
                        integerBookHashMap.put(dbBook.getId(), dbBook);
                    }
                }
            }
            bookList = new ArrayList<>(integerBookHashMap.values());
            return bookList;
        }
    }

    @Override
    public int save(Book book) throws SQLException {
        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            int id = saveBook(book, connection);

            updateAndSaveAuthors(book.getId(), book.getAuthorList(), connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();
            return id;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    private void updateAndSaveAuthors(int id, List<Author> authorList, Connection connection) throws SQLException {
        //список авторов связанных с книгой
        List<Author> listAuthorFromBD = getAuthorListFromBD(id, connection);

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
            addRelationAuthorBook(id, connection, forAdded);
            //удаление связей
            removeRelationAuthorBook(id, connection, forRemoveRelation);
        }else{
            //удаление связей
            removeRelationAuthorBook(id, connection, listAuthorFromBD);
        }
    }

    private void addRelationAuthorBook(int id, Connection connection, List<Author> forAdded) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
            for (Author author : forAdded) {
                pst.setInt(1, author.getId());
                pst.setInt(2, id);
                pst.executeUpdate();
            }
        }
    }

    private List<Author> getAuthorListFromBD(int id, Connection connection) throws SQLException {
        List<Author> listAuthorFromBD = new ArrayList<>();

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AllAuthorFindByBookId.QUERY)) {
            pst.setInt(1, id);

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

    private void removeRelationAuthorBook(int id, Connection connection, List<Author> forRemoveRelation) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {

            for (Author author : forRemoveRelation) {
                pst.setInt(1, author.getId());
                pst.setInt(2, id);
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

    private int saveBook(Book book, Connection connection) throws SQLException {

        checkAddGenre(book.getGenre(), connection);

        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, book.getTitle());
            pst.setInt(2, book.getGenre().getId());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
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

    private Optional<Genre> findGenreById(int id, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindById.QUERY)) {
            pst.setInt(1, id);

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

            rowsUpdated = updatedBook(book, connection);

            updateAndSaveAuthors(book.getId(), book.getAuthorList(), connection);

            sessionManager.commitSession();
            sessionManager.finishTransaction();

            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    private int updatedBook(Book book, Connection connection) throws SQLException {
        int rowsUpdated;
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookUpdateById.QUERY)) {

            pst.setString(1, book.getTitle());
            pst.setInt(2, book.getId());

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }

    @Override
    public int deleteById(int id) throws SQLException {
        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {

            sessionManager.startTransaction();

            Optional<Book> optionalBook = findBookById(id, connection);

            if (optionalBook.isPresent()){
                Book dbBook = optionalBook.get();
                removeRelationAuthorBook(id, connection, dbBook.getAuthorList());
                rowsUpdated = deleteBook(id, connection);
            }

            sessionManager.finishTransaction();
            sessionManager.commitSession();
            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    private int deleteBook(int id, Connection connection) throws SQLException {
        int rowsUpdated;
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookDeleteById.QUERY)) {
            pst.setInt(1, id);

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }
}
