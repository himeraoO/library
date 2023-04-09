package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookDAOImpl implements BookDAO {

    @Override
    public Optional<Book> findBookById(int bookId, Connection connection) throws SQLException {
        Book book = null;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindByIdWithAuthors.QUERY)) {
            pst.setInt(1, bookId);
            try (ResultSet rs = pst.executeQuery()) {
                Book dbBook = parseBookWithAuthors(rs);
                if (dbBook.getId() != 0) {
                    book = dbBook;
                }
            }
        }

        if (book == null) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindByIdWithoutAuthors.QUERY)) {
                pst.setInt(1, bookId);
                try (ResultSet rs = pst.executeQuery()) {
                    Book dbBook = parseBookWithoutAuthors(rs);
                    if (dbBook.getId() != 0) {
                        book = dbBook;
                    }
                }
            }
        }

        return Optional.ofNullable(book);
    }

    private Book parseBookWithAuthors(ResultSet rs) throws SQLException {
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
        return dbBook;
    }


    private Book parseBookWithoutAuthors(ResultSet rs) throws SQLException {
        Book dbBook = new Book();
        Genre genre = new Genre();
        while (rs.next()) {
            dbBook.setId(Integer.parseInt(rs.getString("bid")));
            dbBook.setTitle(rs.getString("btitle"));
            genre.setId((Integer.parseInt(rs.getString("gid"))));
            genre.setName((rs.getString("gname")));
        }
        dbBook.setGenre(genre);
        return dbBook;
    }

    @Override
    public List<Book> findAllBook(Connection connection) throws SQLException {
        List<Book> result = new ArrayList<>();
        List<Book> booksWithAuthors = getBooksWithAuthors(connection);
        List<Book> booksWithoutAuthors = getBooksWithoutAuthors(connection);
        result.addAll(booksWithAuthors);
        result.addAll(booksWithoutAuthors
                .stream()
                .filter(book -> !booksWithAuthors.contains(book))
                .collect(Collectors.toList()));
        return result;
    }

    private List<Book> getBooksWithAuthors(Connection connection) throws SQLException {
        HashMap<Integer, Book> integerBookHashMap = new HashMap<>();

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithAuthors.QUERY)) {
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Book dbBook = new Book();
                    dbBook.setId(Integer.parseInt(rs.getString("bid")));
                    dbBook.setTitle(rs.getString("btitle"));

                    Genre genre = new Genre();
                    genre.setId((Integer.parseInt(rs.getString("gid"))));
                    genre.setName((rs.getString("gname")));

                    dbBook.setGenre(genre);

                    Author author = new Author();
                    author.setId(Integer.parseInt(rs.getString("aid")));
                    author.setName(rs.getString("aname"));
                    author.setSurname(rs.getString("asurname"));

                    if (integerBookHashMap.containsKey(dbBook.getId())) {
                        integerBookHashMap.get(dbBook.getId()).getAuthorList().add(author);
                    } else {
                        dbBook.getAuthorList().add(author);
                        integerBookHashMap.put(dbBook.getId(), dbBook);
                    }
                }
            }
        }
        return new ArrayList<>(integerBookHashMap.values());
    }

    private List<Book> getBooksWithoutAuthors(Connection connection) throws SQLException {
        List<Book> bookListWithoutAuthor = new ArrayList<>();
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithoutAuthors.QUERY)) {
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Book dbBook = parseBookFromResultSetWithoutAuthors(rs);
                    bookListWithoutAuthor.add(dbBook);
                }
            }
        }
        return bookListWithoutAuthor;
    }

    private Book parseBookFromResultSetWithoutAuthors(ResultSet rs) throws SQLException {
        Book dbBook = new Book();
        dbBook.setId(Integer.parseInt(rs.getString("bid")));
        dbBook.setTitle(rs.getString("btitle"));

        Genre genre = new Genre();
        genre.setId((Integer.parseInt(rs.getString("gid"))));
        genre.setName((rs.getString("gname")));

        dbBook.setGenre(genre);
        return dbBook;
    }

    @Override
    public int saveBook(Book book, Connection connection) throws SQLException {
        int id = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, book.getTitle());
            pst.setInt(2, book.getGenre().getId());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }
        }
        return id;
    }

    @Override
    public int updatedBook(Book book, Connection connection) throws SQLException {
        int rowsUpdated;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookUpdateById.QUERY)) {

            pst.setString(1, book.getTitle());
            pst.setInt(2, book.getGenre().getId());
            pst.setInt(3, book.getId());

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

    @Override
    public List<Author> getAuthorListFromBDByBookId(int bookId, Connection connection) throws SQLException {
        List<Author> listAuthorFromBD = new ArrayList<>();

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AllAuthorFindByBookId.QUERY)) {
            pst.setInt(1, bookId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Author author = new Author();
                    author.setId(Integer.parseInt(rs.getString("aid")));
                    author.setName(rs.getString("aname"));
                    author.setSurname(rs.getString("asurname"));
                    author.setBookList(new ArrayList<>());
                    listAuthorFromBD.add(author);
                }
            }
        }
        return listAuthorFromBD;
    }

    @Override
    public void addRelationAuthorBook(int authorId, int bookId, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
            pst.setInt(1, authorId);
            pst.setInt(2, bookId);
            pst.executeUpdate();
        }
    }

    @Override
    public void removeRelationAuthorBook(int bookId, List<Author> forRemoveRelation, Connection connection) throws SQLException {
        if (!forRemoveRelation.isEmpty()) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {
                for (Author author : forRemoveRelation) {
                    pst.setInt(1, author.getId());
                    pst.setInt(2, bookId);
                    pst.executeUpdate();
                }
            }
        }
    }

    @Override
    public int countBookByGenreId(int genreId, Connection connection) throws SQLException {
        int countRows = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_CountBookByGenreId.QUERY)) {
            pst.setInt(1, genreId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    countRows = rs.getInt("Count(*)");
                }
            }
        }
        return countRows;
    }

    @Override
    public int countBookByTitle(String bookTitle, Connection connection) throws SQLException {
        int countRows = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_CountBookByTitle.QUERY)) {
            pst.setString(1, bookTitle);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    countRows = rs.getInt("Count(*)");
                }
            }
        }
        return countRows;
    }
}
