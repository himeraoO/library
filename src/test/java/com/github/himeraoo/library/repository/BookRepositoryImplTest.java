package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic(value = "Тестирование слоя Repository")
@Feature(value = "Тестирование BookRepository")
@Execution(ExecutionMode.CONCURRENT)
class BookRepositoryImplTest extends BaseRepositoryTest {

    @Test
    @DisplayName("Тест поиска книги по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findById() throws SQLException {
        int bookId = 1;
        Genre genre = getGenre(1, "genre1");
        Book expectedBook = getBook(bookId, "book1", genre);

        Author author = getAuthor(1, "author_name1", "author_surname1", new ArrayList<>());
        expectedBook.getAuthorList().add(author);

        Optional<Book> optionalBook = bookRepository.findById(bookId);

        Mockito.verify(bookDAO, Mockito.times(1)).findBookById(bookId, connection);
        assertEquals(Optional.of(expectedBook), optionalBook);
    }

    @Test
    @DisplayName("Тест поиска всех книг")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAll() throws SQLException {
        int bookId = 1;
        Genre genre = getGenre(1, "genre1");
        Book oldBook = getBook(bookId, "book1", genre);

        Author author = getAuthor(1, "author_name1", "author_surname1", new ArrayList<>());
        oldBook.getAuthorList().add(author);

        int bookId5 = 5;
        Genre genre5 = getGenre(5, "genre5");
        Book oldBook5 = getBook(bookId5, "book5", genre5);

        List<Book> expectedBookList = new ArrayList<>();
        expectedBookList.add(oldBook);
        expectedBookList.add(oldBook5);

        List<Book> bookListFromBD = bookRepository.findAll();

        Mockito.verify(bookDAO, Mockito.times(1)).findAllBook(connection);
        assertEquals(expectedBookList, bookListFromBD);
    }

    @Test
    @DisplayName("Тест сохранения новой книги")
    @Story(value = "Тестирование метода сохранения элемента")
    void save() throws SQLException {
        int expectedAddedId = 1;

        int bookId = 1;
        Genre genre = getGenre(1, "genre1");
        Book book = getBook(bookId, "book1", genre);
        Author author = getAuthor(1, "author_name1", "author_surname1", new ArrayList<>());
        book.getAuthorList().add(author);

        int addedId = bookRepository.save(book);

        Mockito.verify(bookDAO, Mockito.times(1)).countBookByTitle(book.getTitle(), connection);
        Mockito.verify(genreDAO, Mockito.times(1)).findAllGenre(connection);
        Mockito.verify(bookDAO, Mockito.times(1)).saveBook(book, connection);
        Mockito.verify(bookDAO, Mockito.times(1)).addRelationAuthorBook(author.getId(), bookId, connection);
        assertEquals(expectedAddedId, addedId);
    }

    @Test
    @DisplayName("Тест обновления книги")
    @Story(value = "Тестирование метода обновления элемента")
    void update() throws SQLException {
        int bookId = 1;
        Genre genre = getGenre(1, "genre1");
        Book book = getBook(bookId, "book1U", genre);
        Author author = getAuthor(1, "author_name1", "author_surname1", new ArrayList<>());
        book.getAuthorList().add(author);

        int rowUpdatedExpected = 1;

        int rowUpdated = bookRepository.update(book);

        Mockito.verify(bookDAO, Mockito.times(1)).updatedBook(book, connection);
        Mockito.verify(bookDAO, Mockito.times(1)).findBookById(bookId, connection);
        Mockito.verify(bookDAO, Mockito.times(1)).countBookByTitle(book.getTitle(), connection);
        Mockito.verify(genreDAO, Mockito.times(1)).findAllGenre(connection);
        Mockito.verify(bookDAO, Mockito.times(1)).getAuthorListFromBDByBookId(bookId, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).findAllAuthor(connection);
        Mockito.verify(bookDAO, Mockito.times(1)).addRelationAuthorBook(author.getId(), bookId, connection);
        assertEquals(rowUpdatedExpected, rowUpdated);
    }

    @Test
    @DisplayName("Тест удаления книги по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteById() throws SQLException {
        int bookId = 1;
        Genre genre = getGenre(1, "genre1");
        Book book = getBook(bookId, "book1", genre);
        Author author = getAuthor(1, "author_name1", "author_surname1", new ArrayList<>());
        book.getAuthorList().add(author);

        int rowDeletedExpected = 1;

        int rowDeleted = bookRepository.deleteById(1);

        Mockito.verify(bookDAO, Mockito.times(1)).deleteBook(bookId, connection);
        Mockito.verify(bookDAO, Mockito.times(1)).findBookById(bookId, connection);
        Mockito.verify(bookDAO, Mockito.times(1)).removeRelationAuthorBook(bookId, book.getAuthorList(), connection);
        assertEquals(rowDeletedExpected, rowDeleted);
    }
}