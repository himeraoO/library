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
             Author author = null;

            try (ResultSet rs = pst.executeQuery()) {
                Author dbAuthor = new Author();
                List<Book> books = new ArrayList<>();
                while (rs.next()) {
                    dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
                    dbAuthor.setName(rs.getString("aname"));
                    dbAuthor.setSurname(rs.getString("asurname"));

                    Book book = new Book();
                    book.setId(Integer.parseInt(rs.getString("bid")));
                    book.setTitle(rs.getString("btitle"));
                    book.setAuthorList(new ArrayList<>());

                    Genre genre = new Genre();
                    genre.setId((Integer.parseInt(rs.getString("gid"))));
                    genre.setName((rs.getString("gname")));

                    book.setGenre(genre);
                    books.add(book);
                }
                dbAuthor.setBookList(books);
                if(dbAuthor.getId() != 0){
                    author = dbAuthor;
                }
            }
            return Optional.ofNullable(author);
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
                    book.setAuthorList(new ArrayList<>());

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

    @Override
    public List<Book> getBookListFromBDByAuthorId(int authorId, Connection connection) throws SQLException {
        List<Book> listBookFromBD = new ArrayList<>();

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AllBookFindByAuthorId.QUERY)) {
            pst.setInt(1, authorId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(Integer.parseInt(rs.getString("bid")));
                    book.setTitle(rs.getString("btitle"));
                    book.setAuthorList(new ArrayList<>());

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

    @Override
    public void removeRelationBookAuthor(int author_id, Connection connection, List<Book> forRemoveRelation) throws SQLException {
        if(!forRemoveRelation.isEmpty()) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {
                for (Book book : forRemoveRelation) {
                    pst.setInt(1, author_id);
                    pst.setInt(2, book.getId());
                    pst.executeUpdate();
                }
            }
        }
    }

    @Override
    public void addRelationAuthorBook(int authorId, int bookId, Connection connection) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                pst.setInt(1, authorId);
                pst.setInt(2, bookId);
                pst.executeUpdate();
        }
    }
}
