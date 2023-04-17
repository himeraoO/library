package com.github.himeraoo.library.service;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;
import com.github.himeraoo.library.repository.AuthorRepository;
import com.github.himeraoo.library.repository.BookRepository;
import com.github.himeraoo.library.repository.GenreRepository;
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
    protected AuthorRepository authorRepository;
    @Mock
    protected BookRepository bookRepository;
    @Mock
    protected GenreRepository genreRepository;

    @BeforeEach
    public void init() throws SQLException {

        authorService = new AuthorServiceImpl(authorRepository);
        bookService = new BookServiceImpl(bookRepository);
        genreService = new GenreServiceImpl(genreRepository);

        initAuthorServiceMock();
        initGenreServiceMock();
        initBookServiceMock();
    }

    private void initBookServiceMock() throws SQLException {
        int bookId = 1;
        Book book = getFullBook(bookId);
        List<Book> bookList = Collections.singletonList(book);
        lenient().when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        lenient().when(bookRepository.findAll()).thenReturn(bookList);
        lenient().when(bookRepository.save(book)).thenReturn(1);
        Book bookSaveNotAdded = getBookWithoutAuthors(0, "NotAdded", getFullGenre(1));
        lenient().when(bookRepository.save(bookSaveNotAdded)).thenReturn(0);
        Book bookSaveNotAddedIsExist = getBookWithoutAuthors(777, "exist", getFullGenre(1));
        lenient().when(bookRepository.save(bookSaveNotAddedIsExist)).thenReturn(-1);
        Book bookForUpdate = getFullBook(bookId, "book1U");
        lenient().when(bookRepository.update(bookForUpdate)).thenReturn(1);

        Book bookForUpdateNotFound = getBookWithoutAuthors(100, "NotFound", getFullGenre(1));
        lenient().when(bookRepository.update(bookForUpdateNotFound)).thenReturn(0);
        Book bookForUpdateNotUpdated = getBookWithoutAuthors(0, "NotUpdated", getFullGenre(1));
        lenient().when(bookRepository.update(bookForUpdateNotUpdated)).thenReturn(-1);

        lenient().when(bookRepository.deleteById(bookId)).thenReturn(1);
        lenient().when(bookRepository.findById(100)).thenReturn(Optional.empty());
        lenient().when(bookRepository.deleteById(100)).thenReturn(0);
    }

    private void initGenreServiceMock() throws SQLException {
        int genreId = 1;
        Genre genre = getFullGenre(genreId);
        List<Genre> genreList = Collections.singletonList(genre);
        lenient().when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        lenient().when(genreRepository.findAll()).thenReturn(genreList);
        lenient().when(genreRepository.save(genre)).thenReturn(1);
        Genre genreSaveNotAdded = getFullGenre(0, "NotAdded");
        lenient().when(genreRepository.save(genreSaveNotAdded)).thenReturn(0);
        Genre genreSaveNotAddedIsExist = getFullGenre(777, "exist");
        lenient().when(genreRepository.save(genreSaveNotAddedIsExist)).thenReturn(-1);
        Genre genreForUpdate = getFullGenre(genreId, "genre1U");
        lenient().when(genreRepository.update(genreForUpdate)).thenReturn(1);
        Genre genreForUpdateNotFound = getFullGenre(100, "genre1");
        lenient().when(genreRepository.update(genreForUpdateNotFound)).thenReturn(0);
        Genre genreForUpdateNotUpdated = getFullGenre(0, "genre0");
        lenient().when(genreRepository.update(genreForUpdateNotUpdated)).thenReturn(-1);
        lenient().when(genreRepository.deleteById(genreId)).thenReturn(1);
        lenient().when(genreRepository.findById(100)).thenReturn(Optional.empty());
        lenient().when(genreRepository.deleteById(100)).thenReturn(0);
        lenient().when(genreRepository.deleteById(0)).thenReturn(-1);
    }

    private void initAuthorServiceMock() throws SQLException {
        int authorId = 1;
        Author author = getFullAuthor(authorId);
        List<Author> authorList = Collections.singletonList(author);
        lenient().when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        lenient().when(authorRepository.findAll()).thenReturn(authorList);
        lenient().when(authorRepository.save(author)).thenReturn(1);
        Author authorSaveNotAdded = getAuthorWithoutBooks(404, "NotAdded", "NotAdded");
        lenient().when(authorRepository.save(authorSaveNotAdded)).thenReturn(0);
        Author authorSaveNotAddedIsExist = getAuthorWithoutBooks(400, "exist", "exist");
        lenient().when(authorRepository.save(authorSaveNotAddedIsExist)).thenReturn(-1);
        Author authorForUpdate = getFullAuthor(authorId, "author_name1U", "author_surname1U");
        lenient().when(authorRepository.update(authorForUpdate)).thenReturn(1);
        lenient().when(authorRepository.deleteById(authorId)).thenReturn(1);
        lenient().when(authorRepository.findById(100)).thenReturn(Optional.empty());
        lenient().when(authorRepository.deleteById(100)).thenReturn(0);
    }
}
