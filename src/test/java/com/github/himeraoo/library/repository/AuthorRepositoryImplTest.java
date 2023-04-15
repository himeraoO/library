package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.models.Author;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorRepositoryImplTest extends BaseRepositoryTest {

    @Test
    void findById() throws SQLException {
        int authorId = 1;
        Author expectedAuthor = getAuthorFromBD(1);

        Optional<Author> optionalAuthor = authorRepository.findById(authorId);

        Mockito.verify(authorDAO, Mockito.times(1)).findAuthorById(authorId, connection);
        assertEquals(Optional.of(expectedAuthor), optionalAuthor);
    }

    @Test
    void findAll() throws SQLException {
        Author oldAuthor = getAuthorFromBD(1);
        List<Author> expectedAuthorList = new ArrayList<>();
        expectedAuthorList.add(oldAuthor);

        List<Author> authorListFromBD = authorRepository.findAll();

        Mockito.verify(authorDAO, Mockito.times(1)).findAllAuthor(connection);
        assertEquals(expectedAuthorList, authorListFromBD);
    }

    @Test
    void save() throws SQLException {
        int authorId = 1;
        int expectedAddedId = 1;

        Author authorForSave = getAuthorForSave(authorId);
        int addedId = authorRepository.save(authorForSave);

        Mockito.verify(authorDAO, Mockito.times(1)).saveAuthor(authorForSave, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).countAuthorByNameAndSurname(authorForSave.getName(), authorForSave.getSurname(), connection);
        Mockito.verify(bookDAO, Mockito.times(1)).findAllBook(connection);
        Mockito.verify(genreDAO, Mockito.times(1)).findAllGenre(connection);
        Mockito.verify(authorDAO, Mockito.times(1)).addRelationAuthorBook(authorId, authorForSave.getBookList().get(0).getId(), connection);
        Mockito.verify(authorDAO, Mockito.times(1)).addRelationAuthorBook(authorId, authorForSave.getBookList().get(1).getId(), connection);
        assertEquals(expectedAddedId, addedId);
    }

    @Test
    void update() throws SQLException {
        int authorId = 1;
        int rowUpdatedExpected = 1;
        Author author = getAuthorForUpdate(authorId);

        int rowUpdated = authorRepository.update(author);

        Mockito.verify(authorDAO, Mockito.times(1)).updatedAuthor(author, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).findAuthorById(authorId, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).countAuthorByNameAndSurname(author.getName(), author.getSurname(), connection);
        Mockito.verify(authorDAO, Mockito.times(1)).getBookListFromBDByAuthorId(author.getId(), connection);
        Mockito.verify(bookDAO, Mockito.times(1)).findAllBook(connection);
        Mockito.verify(genreDAO, Mockito.times(1)).findAllGenre(connection);
        assertEquals(rowUpdatedExpected, rowUpdated);
    }

    @Test
    void deleteById() throws SQLException {
        int authorId = 1;
        Author author = getAuthorFromBD(authorId);
        int rowDeletedExpected = 1;

        int rowDeleted = authorRepository.deleteById(1);

        Mockito.verify(authorDAO, Mockito.times(1)).deleteAuthor(authorId, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).findAuthorById(authorId, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).removeRelationBookAuthor(authorId, author.getBookList(), connection);
        assertEquals(rowDeletedExpected, rowDeleted);
    }
}