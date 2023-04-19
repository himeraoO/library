package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.AuthorDAO;
import com.github.himeraoo.library.dao.BookDAO;
import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.github.himeraoo.library.util.TestUtils.getFullAuthor;
import static com.github.himeraoo.library.util.TestUtils.getFullBook;
import static com.github.himeraoo.library.util.TestUtils.getFullGenre;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class BaseRepositoryTest {

    protected AuthorRepository authorRepository;
    protected GenreRepository genreRepository;
    protected BookRepository bookRepository;

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

        authorRepository = new AuthorRepositoryImpl(sessionManager);
        genreRepository = new GenreRepositoryImpl(sessionManager);
        bookRepository = new BookRepositoryImpl(sessionManager);

        lenient().when(sessionManager.getCurrentSession()).thenReturn(connection);

        initAuthorRepositoryMock();
        initGenreRepositoryMock();
        initBookRepositoryMock();
    }

    private void initBookRepositoryMock() throws SQLException {
        int authorId = 1;
        Author author = getFullAuthor(authorId);
        int bookId = 1;
        Book book = getFullBook(bookId);
        lenient().when(bookDAO.findAllBook(connection)).thenReturn(author.getBookList());
        lenient().when(bookDAO.findBookById(bookId, connection)).thenReturn(Optional.of(book));
        int genreId = 1;
        lenient().when(bookDAO.countBookByGenreId(genreId, connection)).thenReturn(0);
        lenient().when(bookDAO.saveBook(book, connection)).thenReturn(1);
        lenient().when(bookDAO.countBookByTitle(book.getTitle(), connection)).thenReturn(0);
        Book bookForUpdate = getFullBook(bookId, "book1U");
        lenient().when(bookDAO.updatedBook(bookForUpdate, connection)).thenReturn(1);
        lenient().when(bookDAO.deleteBook(bookId, connection)).thenReturn(1);
        lenient().doNothing().when(bookDAO).removeRelationAuthorBook(bookId, book.getAuthorList(), connection);
    }

    private void initGenreRepositoryMock() throws SQLException {
        int authorId = 1;
        Author author = getFullAuthor(authorId);
        int genreId = 1;
        Genre genre = getFullGenre(genreId);
        lenient().when(genreDAO.findAllGenre(connection)).thenReturn(Arrays.asList(author.getBookList().get(0).getGenre(), author.getBookList().get(1).getGenre()));
        lenient().when(genreDAO.findGenreById(genreId, connection)).thenReturn(Optional.of(genre));
        lenient().when(genreDAO.findAllGenre(connection)).thenReturn(Collections.singletonList(genre));
        lenient().when(genreDAO.saveGenre(genre, connection)).thenReturn(1);
        lenient().when(genreDAO.countGenreByName(genre.getName(), connection)).thenReturn(0);
        Genre genreForUpdate = getFullGenre(genreId, "genre1U");
        lenient().when(genreDAO.updatedGenre(genreForUpdate, connection)).thenReturn(1);
        lenient().when(genreDAO.deleteGenre(genreId, connection)).thenReturn(1);
    }

    private void initAuthorRepositoryMock() throws SQLException {
        int authorId = 1;
        Author author = getFullAuthor(authorId);
        lenient().when(authorDAO.findAuthorById(authorId, connection)).thenReturn(Optional.of(author));
        lenient().when(authorDAO.findAllAuthor(connection)).thenReturn(Collections.singletonList(author));
        lenient().when(authorDAO.deleteAuthor(authorId, connection)).thenReturn(1);
        Author authorForUpdate = getFullAuthor(authorId, "author_name1U", "author_surname1U");
        lenient().when(authorDAO.updatedAuthor(authorForUpdate, connection)).thenReturn(1);
        lenient().when(authorDAO.saveAuthor(author, connection)).thenReturn(1);
        lenient().when(authorDAO.countAuthorByNameAndSurname(author.getName(), author.getSurname(), connection)).thenReturn(0);
        lenient().doNothing().when(authorDAO).addRelationAuthorBook(authorId, 1, connection);
        lenient().doNothing().when(authorDAO).addRelationAuthorBook(authorId, 5, connection);
        lenient().doNothing().when(authorDAO).removeRelationBookAuthor(authorId, author.getBookList(), connection);
        lenient().when(authorDAO.getBookListFromBDByAuthorId(authorId, connection)).thenReturn(author.getBookList());
    }
}
