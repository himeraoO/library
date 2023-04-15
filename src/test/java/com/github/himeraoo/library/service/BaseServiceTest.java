package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.AuthorDTO;
import com.github.himeraoo.library.dto.BookDTO;
import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;
import com.github.himeraoo.library.repository.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.*;

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
        lenient().when(bookRepository.update(book)).thenReturn(1);
        lenient().when(bookRepository.deleteById(bookId)).thenReturn(1);
    }

    private void initGenreServiceMock() throws SQLException {
        int genreId = 1;
        Genre genre = getGenre(genreId, "genre1");
        List<Genre> genreList = Collections.singletonList(genre);
        lenient().when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        lenient().when(genreRepository.findAll()).thenReturn(genreList);
        lenient().when(genreRepository.save(genre)).thenReturn(1);
        lenient().when(genreRepository.update(genre)).thenReturn(1);
        lenient().when(genreRepository.deleteById(genreId)).thenReturn(1);
    }

    private void initAuthorServiceMock() throws SQLException {
        int authorId = 1;
        Author author = getFullAuthor(authorId);
        List<Author> authorList = Collections.singletonList(author);
        lenient().when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        lenient().when(authorRepository.findAll()).thenReturn(authorList);
        lenient().when(authorRepository.save(author)).thenReturn(1);
        lenient().when(authorRepository.update(author)).thenReturn(1);
        lenient().when(authorRepository.deleteById(authorId)).thenReturn(1);
    }

    @NotNull
    protected Author getFullAuthor(int authorId) {
        Genre genre1 = getGenre(1, "genre1");
        Book book1 = getBook(1, "book1", genre1);

        Genre genre2 = getGenre(5, "genre5");
        Book book2 = getBook(5, "book5", genre2);

        return getAuthor(authorId, "author_name1", "author_surname1", Arrays.asList(book1, book2));
    }
    @NotNull
    protected Book getFullBook(int bookId) {
        Genre genre = getGenre(1, "genre1");
        Book book = getBook(bookId, "book1", genre);
        Author author = getAuthor(1, "author_name1", "author_surname1", new ArrayList<>());
        book.getAuthorList().add(author);
        return book;
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

    @NotNull
    protected AuthorDTO getAuthorDTO(int authorId, String authorName, String authorSurname, List<Book> bookList) {
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setId(authorId);
        authorDTO.setName(authorName);
        authorDTO.setSurname(authorSurname);
        authorDTO.setBookList(bookList);
        return authorDTO;
    }

    @NotNull
    protected GenreDTO getGenreDTO(int genreId, String genreName) {
        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setId(genreId);
        genreDTO.setName(genreName);
        return genreDTO;
    }

    @NotNull
    protected BookDTO getBookDTO(int bookId, String bookName, Genre genre) {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(bookId);
        bookDTO.setTitle(bookName);
        bookDTO.setGenre(genre);
        return bookDTO;
    }

    @NotNull
    protected AuthorDTO getFullAuthorDTO(int authorId) {
        Genre genre1 = getGenre(1, "genre1");
        Book book1 = getBook(1, "book1", genre1);

        Genre genre2 = getGenre(5, "genre5");
        Book book2 = getBook(5, "book5", genre2);

        return getAuthorDTO(authorId, "author_name1", "author_surname1", Arrays.asList(book1, book2));
    }
    @NotNull
    protected BookDTO getFullBookDTO(int bookId) {
        Genre genre = getGenre(1, "genre1");
        BookDTO bookDTO = getBookDTO(bookId, "book1", genre);
        Author author = getAuthor(1, "author_name1", "author_surname1", new ArrayList<>());
        bookDTO.getAuthorList().add(author);
        return bookDTO;
    }
}
