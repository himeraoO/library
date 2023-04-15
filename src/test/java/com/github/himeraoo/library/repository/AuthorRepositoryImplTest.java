package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.AuthorDAO;
import com.github.himeraoo.library.dao.BookDAO;
import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthorRepositoryImplTest {

    private AuthorRepository authorRepository;

    @Mock
    private AuthorDAO authorDAO;
    @Mock
    private GenreDAO genreDAO;
    @Mock
    private BookDAO bookDAO;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private Connection connection;

    private Author author;
    private List<Author> authorList;
    private Author authorForUpdate;

    @BeforeEach
    public void init() throws SQLException {

        authorRepository = new AuthorRepositoryImpl(sessionManager, authorDAO, genreDAO, bookDAO);

        lenient().when(sessionManager.getCurrentSession()).thenReturn(connection);
        int authorId = 1;
        author = getAuthorFromBD(authorId);
        lenient().when(authorDAO.findAuthorById(authorId, connection)).thenReturn(Optional.of(author));

        authorList = new ArrayList<>();
        authorList.add(author);
        lenient().when(authorDAO.findAllAuthor(connection)).thenReturn(authorList);

        int rowDeletedExpected = 1;
        lenient().when(authorDAO.deleteAuthor(authorId, connection)).thenReturn(rowDeletedExpected);

        int rowUpdatedExpected = 1;
        authorForUpdate = getAuthorForUpdate(authorId);
        lenient().when(authorDAO.updatedAuthor(authorForUpdate, connection)).thenReturn(rowUpdatedExpected);

        int addedId = 1;
        lenient().when(authorDAO.saveAuthor(author, connection)).thenReturn(addedId);

        lenient().when(authorDAO.countAuthorByNameAndSurname(author.getName(), author.getSurname(), connection)).thenReturn(0);
        lenient().when(bookDAO.findAllBook(connection)).thenReturn(author.getBookList());
        lenient().when(genreDAO.findAllGenre(connection)).thenReturn(Arrays.asList(author.getBookList().get(0).getGenre(), author.getBookList().get(1).getGenre()));
        lenient().doNothing().when(authorDAO).addRelationAuthorBook(authorId, 1, connection);
        lenient().doNothing().when(authorDAO).addRelationAuthorBook(authorId, 5, connection);
        lenient().doNothing().when(authorDAO).removeRelationBookAuthor(authorId, author.getBookList(), connection);
        lenient().when(authorDAO.getBookListFromBDByAuthorId(authorId, connection)).thenReturn(author.getBookList());
    }

    @Test
    void findById() throws SQLException {
        int authorId = 1;
        Author expectedAuthor = getAuthorFromBD(1);

        Optional<Author> optionalAuthor = authorRepository.findById(1);

        Mockito.verify(authorDAO, Mockito.times(1)).findAuthorById(authorId, connection);
        assertEquals(Optional.of(expectedAuthor), optionalAuthor);
    }

    @NotNull
    private Author getAuthorFromBD(int authorId) {
        Author author = new Author();
        author.setId(authorId);
        author.setName("author_name1");
        author.setSurname("author_surname1");

        Book book1 = new Book();
        book1.setId(1);
        book1.setTitle("book1");
        Genre genre1 = new Genre();
        genre1.setId(1);
        genre1.setName("genre1");
        book1.setGenre(genre1);
        author.getBookList().add(book1);

        Book book2 = new Book();
        book2.setId(5);
        book2.setTitle("book5");
        Genre genre2 = new Genre();
        genre2.setId(5);
        genre2.setName("genre5");
        book2.setGenre(genre2);
        author.getBookList().add(book2);
        return author;
    }

    @NotNull
    private Author getAuthorForUpdate(int authorId) {
        Author author = new Author();
        author.setId(authorId);
        author.setName("author_name1U");
        author.setSurname("author_surname1U");

        Book book1 = new Book();
        book1.setId(1);
        book1.setTitle("book1");
        Genre genre1 = new Genre();
        genre1.setId(1);
        genre1.setName("genre1");
        book1.setGenre(genre1);
        author.getBookList().add(book1);

        Book book2 = new Book();
        book2.setId(5);
        book2.setTitle("book5");
        Genre genre2 = new Genre();
        genre2.setId(5);
        genre2.setName("genre5");
        book2.setGenre(genre2);
        author.getBookList().add(book2);
        return author;
    }

    @NotNull
    private Author getAuthorForSave(int authorId) {
        Author author = new Author();
        author.setId(authorId);
        author.setName("author_name1");
        author.setSurname("author_surname1");

        Book book1 = new Book();
        book1.setId(1);
        book1.setTitle("book1");
        Genre genre1 = new Genre();
        genre1.setId(1);
        genre1.setName("genre1");
        book1.setGenre(genre1);
        author.getBookList().add(book1);

        Book book2 = new Book();
        book2.setId(5);
        book2.setTitle("book5");
        Genre genre2 = new Genre();
        genre2.setId(5);
        genre2.setName("genre5");
        book2.setGenre(genre2);
        author.getBookList().add(book2);
        return author;
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
        Mockito.verify(authorDAO, Mockito.times(1)).addRelationAuthorBook(authorId, authorForUpdate.getBookList().get(0).getId(), connection);
        Mockito.verify(authorDAO, Mockito.times(1)).addRelationAuthorBook(authorId, authorForUpdate.getBookList().get(1).getId(), connection);
        assertEquals(expectedAddedId, addedId);
    }

    @Test
    void update() throws SQLException {
        int authorId = 1;
        int rowUpdatedExpected = 1;
        Author author = getAuthorForUpdate(authorId);

        int rowUpdated = authorRepository.update(author);

        Mockito.verify(authorDAO, Mockito.times(1)).updatedAuthor(author, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).findAuthorById(author.getId(), connection);
        Mockito.verify(authorDAO, Mockito.times(1)).countAuthorByNameAndSurname(author.getName(), author.getSurname(), connection);
        Mockito.verify(authorDAO, Mockito.times(1)).getBookListFromBDByAuthorId(author.getId(), connection);
        Mockito.verify(bookDAO, Mockito.times(1)).findAllBook(connection);
        Mockito.verify(genreDAO, Mockito.times(1)).findAllGenre(connection);
        assertEquals(rowUpdatedExpected, rowUpdated);
    }

    @Test
    void deleteById() throws SQLException {
        int authorId = 1;
        int rowDeletedExpected = 1;

        int rowDeleted = authorRepository.deleteById(1);

        Mockito.verify(authorDAO, Mockito.times(1)).deleteAuthor(authorId, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).findAuthorById(authorId, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).removeRelationBookAuthor(authorId, author.getBookList(), connection);
        assertEquals(rowDeletedExpected, rowDeleted);
    }
}