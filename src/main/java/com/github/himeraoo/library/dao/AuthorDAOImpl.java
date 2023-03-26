package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class AuthorDAOImpl implements AuthorDAO{
    @Override
    public Optional<Author> findAuthorById(int authorId, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindById.QUERY)) {
            pst.setInt(1, authorId);
            Author dbAuthor = new Author();
            List<Book> books = new ArrayList<>();
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
                    dbAuthor.setName(rs.getString("aname"));
                    dbAuthor.setSurname(rs.getString("asurname"));

                    Book book = new Book();
                    book.setId(Integer.parseInt(rs.getString("bid")));
                    book.setTitle(rs.getString("btitle"));

                    Genre genre = new Genre();
                    genre.setId((Integer.parseInt(rs.getString("gid"))));
                    genre.setName((rs.getString("gname")));

                    book.setGenre(genre);
                    books.add(book);
                }
            }
            dbAuthor.setBookList(books);
            return Optional.ofNullable(dbAuthor);
        }
    }

    @Override
    public List<Author> findAllAuthor(Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindAll.QUERY)) {
            List<Author> authorList = null;
            HashMap<Integer, Author> integerAuthorHashMap = new HashMap<>();
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Author dbAuthor = new Author();
                    dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
                    dbAuthor.setName(rs.getString("aname"));
                    dbAuthor.setSurname(rs.getString("asurname"));

                    Book book = new Book();
                    book.setId(Integer.parseInt(rs.getString("bid")));
                    book.setTitle(rs.getString("btitle"));

                    Genre genre = new Genre();
                    genre.setId((Integer.parseInt(rs.getString("gid"))));
                    genre.setName((rs.getString("gname")));

                    book.setGenre(genre);

                    if (integerAuthorHashMap.containsKey(dbAuthor.getId())) {
                        integerAuthorHashMap.get(dbAuthor.getId()).getBookList().add(book);
                    } else {
                        List<Book> bookList = new ArrayList<>();
                        bookList.add(book);
                        dbAuthor.setBookList(bookList);
                        integerAuthorHashMap.put(dbAuthor.getId(), dbAuthor);
                    }
                }
                authorList = new ArrayList<>(integerAuthorHashMap.values());
                return authorList;
            }
        }
    }

    @Override
    public int saveAuthor(Author author, Connection connection) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, author.getName());
            pst.setString(2, author.getSurname());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public int updatedAuthor(Author author, Connection connection) throws SQLException {
        int rowsUpdated;
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorUpdateById.QUERY)) {

            pst.setString(1, author.getName());
            pst.setString(2, author.getSurname());
            pst.setInt(3, author.getId());

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }

    @Override
    public int deleteAuthor(int authorId, Connection connection) throws SQLException {
        int rowsUpdated;
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorDeleteById.QUERY)) {
            pst.setInt(1, authorId);

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }
}
