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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class BaseRepositoryTest {

    protected AuthorRepository authorRepository;
    protected GenreRepository genreRepository;

    @Mock
    protected AuthorDAO authorDAO;
    @Mock
    protected GenreDAO genreDAO;
    @Mock
    protected BookDAO bookDAO;
    @Mock
    protected SessionManager sessionManager;
    @Mock
    protected Connection connection;

    @BeforeEach
    public void init() throws SQLException {

        authorRepository = new AuthorRepositoryImpl(sessionManager, authorDAO, genreDAO, bookDAO);
        genreRepository = new GenreRepositoryImpl(sessionManager, genreDAO, bookDAO);

        lenient().when(sessionManager.getCurrentSession()).thenReturn(connection);

        initAuthorRepositoryMock();
        initGenreRepositoryMock();
        initBookRepositoryMock();
    }

    private void initBookRepositoryMock() throws SQLException {
        int authorId = 1;
        Author author = getAuthorFromBD(authorId);
        lenient().when(bookDAO.findAllBook(connection)).thenReturn(author.getBookList());
    }

    private void initGenreRepositoryMock() throws SQLException {
//        int authorId = 1;
//        Author author = getAuthorFromBD(authorId);
//
//        int genreId = 1;
//        Genre genre = getGenre(1, "genre1");
//
//        lenient().when(genreDAO.findAllGenre(connection)).thenReturn(Arrays.asList(author.getBookList().get(0).getGenre(), author.getBookList().get(1).getGenre()));
//        lenient().when(genreDAO.findGenreById(genreId, connection)).thenReturn(Optional.of(genre));
//
//        List<Genre> genreList = new ArrayList<>();
//        genreList.add(genre);
//        lenient().when(genreDAO.findAllGenre(connection)).thenReturn(genreList);
//
//        lenient().when(genreDAO.saveGenre(genre, connection)).thenReturn(1);
//        lenient().when(genreDAO.countGenreByName(genre.getName(), connection)).thenReturn(0);
//
//        Genre genreForUpdate = getGenre(genreId, "genre1U");
    }

    private void initAuthorRepositoryMock() throws SQLException {
        int authorId = 1;
        Author author = getAuthorFromBD(authorId);

        lenient().when(authorDAO.findAuthorById(authorId, connection)).thenReturn(Optional.of(author));

        List<Author> authorList = new ArrayList<>();
        authorList.add(author);
        lenient().when(authorDAO.findAllAuthor(connection)).thenReturn(authorList);

        int rowDeletedExpected = 1;
        lenient().when(authorDAO.deleteAuthor(authorId, connection)).thenReturn(rowDeletedExpected);

        int rowUpdatedExpected = 1;
        Author authorForUpdate = getAuthorForUpdate(authorId);
        lenient().when(authorDAO.updatedAuthor(authorForUpdate, connection)).thenReturn(rowUpdatedExpected);

        int addedId = 1;
        lenient().when(authorDAO.saveAuthor(author, connection)).thenReturn(addedId);

        lenient().when(authorDAO.countAuthorByNameAndSurname(author.getName(), author.getSurname(), connection)).thenReturn(0);
        lenient().doNothing().when(authorDAO).addRelationAuthorBook(authorId, 1, connection);
        lenient().doNothing().when(authorDAO).addRelationAuthorBook(authorId, 5, connection);
        lenient().doNothing().when(authorDAO).removeRelationBookAuthor(authorId, author.getBookList(), connection);
        lenient().when(authorDAO.getBookListFromBDByAuthorId(authorId, connection)).thenReturn(author.getBookList());
    }

    @NotNull
    protected Author getAuthorFromBD(int authorId) {
        Genre genre1 = getGenre(1, "genre1");
        Book book1 = getBook(1, "book1", genre1);

        Genre genre2 = getGenre(5, "genre5");
        Book book2 = getBook(5, "book5", genre2);

        return getAuthor(authorId, "author_name1", "author_surname1", Arrays.asList(book1, book2));
    }

    @NotNull
    protected Author getAuthorForUpdate(int authorId) {
        Genre genre1 = getGenre(1, "genre1");
        Book book1 = getBook(1, "book1", genre1);

        Genre genre2 = getGenre(5, "genre5");
        Book book2 = getBook(5, "book5", genre2);

        return getAuthor(authorId, "author_name1U", "author_surname1U", Arrays.asList(book1, book2));
    }

    @NotNull
    protected Author getAuthorForSave(int authorId) {
        Genre genre1 = getGenre(1, "genre1");
        Book book1 = getBook(1, "book1", genre1);

        Genre genre2 = getGenre(5, "genre5");
        Book book2 = getBook(5, "book5", genre2);

        return getAuthor(authorId, "author_name1", "author_surname1", Arrays.asList(book1, book2));
    }

    @NotNull
    protected Genre getGenre(int genreId, String genreName) {
        Genre genre = new Genre();
        genre.setId(genreId);
        genre.setName(genreName);
        return genre;
    }

    @NotNull
    protected Book getBook(int bookId, String bookName, Genre genre) {
        Book book = new Book();
        book.setId(bookId);
        book.setTitle(bookName);
        book.setGenre(genre);
        return book;
    }

    @NotNull
    protected Author getAuthor(int authorId, String authorName, String authorSurname, List<Book> bookList) {
        Author author = new Author();
        author.setId(authorId);
        author.setName(authorName);
        author.setSurname(authorSurname);
        author.setBookList(bookList);
        return author;
    }
}
