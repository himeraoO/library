package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuthorsBooksDAOImpl implements AuthorsBooksDAO{

    private final AuthorDAO authorDAO;
    private final BookDAO bookDAO;
    private final GenreDAO genreDAO;


    public AuthorsBooksDAOImpl(AuthorDAO authorDAO, BookDAO bookDAO, GenreDAO genreDAO) {
        this.authorDAO = authorDAO;
        this.bookDAO = bookDAO;
        this.genreDAO = genreDAO;
    }

    @Override
    public void updateAndSaveAuthors(int bookId, List<Author> authorList, Connection connection) throws SQLException {
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

    @Override
    public void addRelationAuthorBook(int bookId, Connection connection, List<Author> forAdded) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
            for (Author author : forAdded) {
                pst.setInt(1, author.getId());
                pst.setInt(2, bookId);
                pst.executeUpdate();
            }
        }
    }

    @Override
    public List<Author> getAuthorListFromBD(int bookId, Connection connection) throws SQLException {
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

    @Override
    public void removeRelationAuthorBook(int bookId, Connection connection, List<Author> forRemoveRelation) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {

            for (Author author : forRemoveRelation) {
                pst.setInt(1, author.getId());
                pst.setInt(2, bookId);
                pst.executeUpdate();
            }
        }
    }

    @Override
    public void addAuthorList(Connection connection, List<Author> forAdded) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorSave.QUERY)){
            for (Author author : forAdded) {
                pst.setString(1, author.getName());
                pst.setString(2, author.getSurname());
                pst.executeUpdate();
            }
        }
    }

    @Override
    public void updateAndSaveBooks(int authorId, List<Book> bookList, Connection connection) throws SQLException {
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
            genreDAO.checkAndAddGenreListFromBookList(bookList, connection);
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

    @Override
    public List<Book> getBookListFromBD(int authorId, Connection connection) throws SQLException {
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

    @Override
    public void addBookList(Connection connection, List<Book> forAdded) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookSave.QUERY)){
            for (Book book : forAdded) {
                pst.setString(1, book.getTitle());
                pst.setInt(2, book.getGenre().getId());
                pst.executeUpdate();
            }
        }
    }

    @Override
    public void addRelationBookAuthor(int authorId, Connection connection, List<Book> forAdded) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
            for (Book book : forAdded) {
                pst.setInt(1, authorId);
                pst.setInt(2, book.getId());
                pst.executeUpdate();
            }
        }
    }

    @Override
    public void removeRelationBookAuthor(int author_id, Connection connection, List<Book> forRemoveRelation) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {
            for (Book book : forRemoveRelation) {
                pst.setInt(1, author_id);
                pst.setInt(2, book.getId());
                pst.executeUpdate();
            }
        }
    }
}
