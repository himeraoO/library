package com.github.himeraoo.library.util;

import com.github.himeraoo.library.dto.AuthorDTO;
import com.github.himeraoo.library.dto.BookDTO;
import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestUtils {

    @NotNull
    public static Author getFullAuthor(int authorId) {
        Genre genre1 = getGenre(1, "genre1");
        Book book1 = getBookWithoutAuthors(1, "book1", genre1);

        Genre genre2 = getGenre(5, "genre5");
        Book book2 = getBookWithoutAuthors(5, "book5", genre2);

        return getAuthor(authorId, "author_name1", "author_surname1", Arrays.asList(book1, book2));
    }

    @NotNull
    public static Author getFullAuthor(int authorId, String updateAuthorName, String updateAuthorSurname) {
        Genre genre1 = getGenre(1, "genre1");
        Book book1 = getBookWithoutAuthors(1, "book1", genre1);

        Genre genre2 = getGenre(5, "genre5");
        Book book2 = getBookWithoutAuthors(5, "book5", genre2);

        return getAuthor(authorId, updateAuthorName, updateAuthorSurname, Arrays.asList(book1, book2));
    }

    @NotNull
    public static Book getFullBook(int bookId) {
        Genre genre = getGenre(1, "genre1");
        Author author = getAuthorWithoutBooks(1, "author_name1", "author_surname1");
        return getBook(bookId, "book1", genre, Collections.singletonList(author));
    }

    @NotNull
    public static Book getFullBook(int bookId, String updateBookTitle) {
        Genre genre = getGenre(1, "genre1");
        Author author = getAuthorWithoutBooks(1, "author_name1", "author_surname1");
        return getBook(bookId, updateBookTitle, genre, Collections.singletonList(author));
    }

    @NotNull
    public static Genre getFullGenre(int genreId) {
        return getGenre(genreId, "genre1");
    }

    @NotNull
    public static Genre getFullGenre(int genreId, String updateGenreName) {
        return getGenre(genreId, updateGenreName);
    }

    @NotNull
    public static Genre getGenre(int genreId, String genreName) {
        Genre genre = new Genre();
        genre.setId(genreId);
        genre.setName(genreName);
        return genre;
    }

    @NotNull
    public static Book getBook(int bookId, String bookName, Genre genre, List<Author> authorList) {
        Book book = getBookWithoutAuthors(bookId, bookName, genre);
        book.setAuthorList(authorList);
        return book;
    }

    @NotNull
    public static Book getBookWithoutAuthors(int bookId, String bookName, Genre genre) {
        Book book = new Book();
        book.setId(bookId);
        book.setTitle(bookName);
        book.setGenre(genre);
        return book;
    }

    @NotNull
    public static Author getAuthor(int authorId, String authorName, String authorSurname, List<Book> bookList) {
        Author author = getAuthorWithoutBooks(authorId, authorName, authorSurname);
        author.setBookList(bookList);
        return author;
    }

    @NotNull
    public static Author getAuthorWithoutBooks(int authorId, String authorName, String authorSurname) {
        Author author = new Author();
        author.setId(authorId);
        author.setName(authorName);
        author.setSurname(authorSurname);
        return author;
    }

    @NotNull
    public static AuthorDTO getAuthorDTO(Author author) {
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setId(author.getId());
        authorDTO.setName(author.getName());
        authorDTO.setSurname(author.getSurname());
        authorDTO.setBookList(author.getBookList());
        return authorDTO;
    }

    @NotNull
    public static BookDTO getBookDTO(Book book) {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(book.getId());
        bookDTO.setTitle(book.getTitle());
        bookDTO.setGenre(book.getGenre());
        bookDTO.setAuthorList(book.getAuthorList());
        return bookDTO;
    }

    @NotNull
    public static GenreDTO getGenreDTO(Genre genre) {
        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setId(genre.getId());
        genreDTO.setName(genre.getName());
        return genreDTO;
    }
}
