package com.github.himeraoo.library.service;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;
import com.github.himeraoo.library.dao.AuthorDAO;
import com.github.himeraoo.library.dao.BookDAO;
import com.github.himeraoo.library.dao.GenreDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.himeraoo.library.util.TestUtils.getAuthorWithoutBooks;
import static com.github.himeraoo.library.util.TestUtils.getBookWithoutAuthors;
import static com.github.himeraoo.library.util.TestUtils.getFullAuthor;
import static com.github.himeraoo.library.util.TestUtils.getFullBook;
import static com.github.himeraoo.library.util.TestUtils.getFullGenre;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class BaseServiceTest {

    protected AuthorService authorService;
    protected BookService bookService;
    protected GenreService genreService;

    @Mock
    protected AuthorDAO authorDAO;
    @Mock
    protected BookDAO bookDAO;
    @Mock
    protected GenreDAO genreDAO;

    @BeforeEach
    public void init() throws SQLException {

        authorService = new AuthorServiceImpl(authorDAO);
        bookService = new BookServiceImpl(bookDAO);
        genreService = new GenreServiceImpl(genreDAO);

        initAuthorServiceMock();
        initGenreServiceMock();
        initBookServiceMock();
    }

    private void initBookServiceMock() throws SQLException {
        int bookId = 1;
        Book book = getFullBook(bookId);
        List<Book> bookList = Collections.singletonList(book);
        lenient().when(bookDAO.findById(bookId)).thenReturn(Optional.of(book));
        lenient().when(bookDAO.findAll()).thenReturn(bookList);
        lenient().when(bookDAO.save(book)).thenReturn(1);
        Book bookSaveNotAdded = getBookWithoutAuthors(0, "NotAdded", getFullGenre(1));
        lenient().when(bookDAO.save(bookSaveNotAdded)).thenReturn(0);
        Book bookSaveNotAddedIsExist = getBookWithoutAuthors(777, "exist", getFullGenre(1));
        lenient().when(bookDAO.save(bookSaveNotAddedIsExist)).thenReturn(-1);
        Book bookForUpdate = getFullBook(bookId, "book1U");
        lenient().when(bookDAO.update(bookForUpdate)).thenReturn(1);

        Book bookForUpdateNotFound = getBookWithoutAuthors(100, "NotFound", getFullGenre(1));
        lenient().when(bookDAO.update(bookForUpdateNotFound)).thenReturn(0);
        Book bookForUpdateNotUpdated = getBookWithoutAuthors(0, "NotUpdated", getFullGenre(1));
        lenient().when(bookDAO.update(bookForUpdateNotUpdated)).thenReturn(-1);

        lenient().when(bookDAO.deleteById(bookId)).thenReturn(1);
        lenient().when(bookDAO.findById(100)).thenReturn(Optional.empty());
        lenient().when(bookDAO.deleteById(100)).thenReturn(0);
    }

    private void initGenreServiceMock() throws SQLException {
        int genreId = 1;
        Genre genre = getFullGenre(genreId);
        List<Genre> genreList = Collections.singletonList(genre);
        lenient().when(genreDAO.findById(genreId)).thenReturn(Optional.of(genre));
        lenient().when(genreDAO.findAll()).thenReturn(genreList);
        lenient().when(genreDAO.save(genre)).thenReturn(1);
        Genre genreSaveNotAdded = getFullGenre(0, "NotAdded");
        lenient().when(genreDAO.save(genreSaveNotAdded)).thenReturn(0);
        Genre genreSaveNotAddedIsExist = getFullGenre(777, "exist");
        lenient().when(genreDAO.save(genreSaveNotAddedIsExist)).thenReturn(-1);
        Genre genreForUpdate = getFullGenre(genreId, "genre1U");
        lenient().when(genreDAO.update(genreForUpdate)).thenReturn(1);
        Genre genreForUpdateNotFound = getFullGenre(100, "genre1");
        lenient().when(genreDAO.update(genreForUpdateNotFound)).thenReturn(0);
        Genre genreForUpdateNotUpdated = getFullGenre(0, "genre0");
        lenient().when(genreDAO.update(genreForUpdateNotUpdated)).thenReturn(-1);
        lenient().when(genreDAO.deleteById(genreId)).thenReturn(1);
        lenient().when(genreDAO.findById(100)).thenReturn(Optional.empty());
        lenient().when(genreDAO.deleteById(100)).thenReturn(0);
        lenient().when(genreDAO.deleteById(0)).thenReturn(-1);
    }

    private void initAuthorServiceMock() throws SQLException {
        int authorId = 1;
        Author author = getFullAuthor(authorId);
        List<Author> authorList = Collections.singletonList(author);
        lenient().when(authorDAO.findById(authorId)).thenReturn(Optional.of(author));
        lenient().when(authorDAO.findAll()).thenReturn(authorList);
        lenient().when(authorDAO.save(author)).thenReturn(1);
        Author authorSaveNotAdded = getAuthorWithoutBooks(404, "NotAdded", "NotAdded");
        lenient().when(authorDAO.save(authorSaveNotAdded)).thenReturn(0);
        Author authorSaveNotAddedIsExist = getAuthorWithoutBooks(400, "exist", "exist");
        lenient().when(authorDAO.save(authorSaveNotAddedIsExist)).thenReturn(-1);
        Author authorForUpdate = getFullAuthor(authorId, "author_name1U", "author_surname1U");
        lenient().when(authorDAO.update(authorForUpdate)).thenReturn(1);
        lenient().when(authorDAO.deleteById(authorId)).thenReturn(1);
        lenient().when(authorDAO.findById(100)).thenReturn(Optional.empty());
        lenient().when(authorDAO.deleteById(100)).thenReturn(0);
    }
}
