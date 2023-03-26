package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;

import java.sql.*;
import java.util.List;

public interface AuthorsBooksDAO {
    void updateAndSaveAuthors(int bookId, List<Author> authorList, Connection connection) throws SQLException;

    void addRelationAuthorBook(int bookId, Connection connection, List<Author> forAdded) throws SQLException;

    List<Author> getAuthorListFromBD(int bookId, Connection connection) throws SQLException;

    void removeRelationAuthorBook(int bookId, Connection connection, List<Author> forRemoveRelation) throws SQLException;

    void addAuthorList(Connection connection, List<Author> forAdded) throws SQLException;

    void updateAndSaveBooks(int authorId, List<Book> bookList, Connection connection) throws SQLException;

    List<Book> getBookListFromBD(int authorId, Connection connection) throws SQLException;

    void addBookList(Connection connection, List<Book> forAdded) throws SQLException;

    void addRelationBookAuthor(int authorId, Connection connection, List<Book> forAdded) throws SQLException;

    void removeRelationBookAuthor(int author_id, Connection connection, List<Book> forRemoveRelation) throws SQLException;
}
