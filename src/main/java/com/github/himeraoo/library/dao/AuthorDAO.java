package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AuthorDAO {
    Optional<Author> findAuthorById(int authorId, Connection connection) throws SQLException;

    List<Author> findAllAuthor(Connection connection) throws SQLException;

    int saveAuthor(Author author, Connection connection) throws SQLException;

    int updatedAuthor(Author author, Connection connection) throws SQLException;

    int deleteAuthor(int authorId, Connection connection) throws SQLException;

    List<Book> getBookListFromBDByAuthorId(int authorId, Connection connection) throws SQLException;

    void removeRelationBookAuthor(int author_id, Connection connection, List<Book> forRemoveRelation) throws SQLException;

//    void addRelationAuthorBook(int authorId, Connection connection, List<Book> forAdded) throws SQLException;

    void addRelationAuthorBook(int authorId, int bookId, Connection connection) throws SQLException;
}
