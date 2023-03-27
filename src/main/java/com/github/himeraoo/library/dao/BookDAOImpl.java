package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BookDAOImpl implements BookDAO {
    @Override
    public Optional<Book> findBookById(int bookId, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindById.QUERY)) {
            pst.setInt(1, bookId);
            Book book = null;

            try (ResultSet rs = pst.executeQuery()) {
                Book dbBook = new Book();
                Genre genre = new Genre();
                List<Author> authors = new ArrayList<>();
                while (rs.next()) {
                    dbBook.setId(Integer.parseInt(rs.getString("bid")));
                    dbBook.setTitle(rs.getString("btitle"));

                    genre.setId((Integer.parseInt(rs.getString("gid"))));
                    genre.setName((rs.getString("gname")));

                    Author author = new Author();
                    author.setId(Integer.parseInt(rs.getString("aid")));
                    author.setName(rs.getString("aname"));
                    author.setSurname(rs.getString("asurname"));
                    author.setBookList(new ArrayList<>());
                    authors.add(author);
                }
                dbBook.setGenre(genre);
                dbBook.setAuthorList(authors);
                if(dbBook.getId() != 0){
                    book = dbBook;
                }
            }
            return Optional.ofNullable(book);
        }
    }

    @Override
    public List<Book> findAllBook(Connection connection) throws SQLException {
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
                    author.setBookList(new ArrayList<>());

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
    public int saveBook(Book book, Connection connection) throws SQLException {

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, book.getTitle());
            pst.setInt(2, book.getGenre().getId());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public int updatedBook(Book book, Connection connection) throws SQLException {
        int rowsUpdated;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookUpdateById.QUERY)) {

            pst.setString(1, book.getTitle());
            pst.setInt(2, book.getId());

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }

    @Override
    public int deleteBook(int bookId, Connection connection) throws SQLException {
        int rowsUpdated;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookDeleteById.QUERY)) {
            pst.setInt(1, bookId);

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }
}
