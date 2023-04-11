package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.jdbc.SessionManagerJDBC;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Epic(value = "Тестирование слоя DAO")
@Feature(value = "Тестирование BookDAO")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookDAOImplTest extends AbstractBaseClassWithBDTest {

    private SessionManager sessionManager;
    private BookDAO bookDAO;

    @BeforeEach
    void setUp() {
        mySQLContainer.start();
        sessionManager = new SessionManagerJDBC(
                mySQLContainer.getJdbcUrl(),
                mySQLContainer.getUsername(),
                mySQLContainer.getPassword(),
                mySQLContainer.getDriverClassName());

        bookDAO = new BookDAOImpl();
    }

    @AfterEach
    void tearDown() {
        mySQLContainer.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Тест поиска книги по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findBookById() {
        sessionManager.beginSession();

        int bookId = 1;
        Optional<Book> optionalBook = Optional.empty();
        Book bookDB = new Book();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalBook = bookDAO.findBookById(bookId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (optionalBook.isPresent()) {
            bookDB = optionalBook.get();
        }

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("book1");
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("genre1");
        book.setGenre(genre);

        Author author1 = new Author();
        author1.setName("author_name1");
        author1.setSurname("author_surname1");

        Author author2 = new Author();
        author2.setName("author_name3");
        author2.setSurname("author_surname3");

        Author author3 = new Author();
        author3.setName("author_name5");
        author3.setSurname("author_surname5");

        List<Author> authorList = new ArrayList<>();
        authorList.add(author1);
        authorList.add(author2);
        authorList.add(author3);

        book.setAuthorList(authorList);

        Book bookDBFinal = bookDB;

        List<Author> bookAuthorList = book.getAuthorList();
        List<Author> bookDBFinalAuthorList = bookDB.getAuthorList();

        Assertions.assertAll("Проверка получения книги по id",
                () -> assertEquals(bookId, bookDBFinal.getId(), "Должно быть значение: " + bookId),
                () -> assertEquals(book, bookDBFinal),
                () -> assertEquals(bookAuthorList, bookDBFinalAuthorList)
        );
    }

    @Test
    @Order(2)
    @DisplayName("Тест поиска всех книг")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAllBook() {
        sessionManager.beginSession();

        List<Book> bookList = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            bookList = bookDAO.findAllBook(connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        Assertions.assertEquals(6, bookList.size(), "Размер полученного списка должен быть 6");

    }

    @Test
    @Order(3)
    @DisplayName("Тест сохранения новой книги")
    @Story(value = "Тестирование метода сохранения элемента")
    void saveBook() {
        sessionManager.beginSession();

        Book book = new Book();
        book.setTitle("book7");
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("genre1");
        book.setGenre(genre);

        int expectedBookId = 7;
        int bookId = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            bookId = bookDAO.saveBook(book, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();

        Optional<Book> optionalBook = Optional.empty();
        Book bookDB = new Book();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalBook = bookDAO.findBookById(expectedBookId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (optionalBook.isPresent()) {
            bookDB = optionalBook.get();
        }

        Book bookDBFinal = bookDB;
        int finalBookId = bookId;

        Assertions.assertAll("Проверка сохранения книги",
                () -> assertEquals(expectedBookId, finalBookId, "Значение ID должно быть " + expectedBookId),
                () -> assertEquals(book, bookDBFinal),
                () -> assertEquals(book.getGenre(), bookDBFinal.getGenre())
        );
    }

    @Test
    @Order(4)
    @DisplayName("Тест обновления книги")
    @Story(value = "Тестирование метода обновления элемента")
    void updatedBook() {
        sessionManager.beginSession();

        int bookId = 1;

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("book7");
        Genre genre = new Genre();
        genre.setId(2);
        genre.setName("genre2");
        book.setGenre(genre);

        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            rowsUpdated = bookDAO.updatedBook(book, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();
        Optional<Book> optionalBook = Optional.empty();
        Book bookDB = new Book();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalBook = bookDAO.findBookById(bookId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (optionalBook.isPresent()) {
            bookDB = optionalBook.get();
        }

        Book bookDBFinal = bookDB;
        int finalRowUpdated = rowsUpdated;


        Assertions.assertAll("Проверка обновления книги",
                () -> assertEquals(1, finalRowUpdated, "Значение rowsUpdated должно быть 1"),
                () -> assertEquals(bookId, bookDBFinal.getId(), "Значение ID должно быть " + bookId),
                () -> assertEquals(book, bookDBFinal),
                () -> assertEquals(book.getGenre(), bookDBFinal.getGenre())
        );
    }

    @Test
    @Order(5)
    @DisplayName("Тест удаления книги по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteBook() {
        sessionManager.beginSession();

        int rowsUpdated = 0;
        int bookId = 6;
        try (Connection connection = sessionManager.getCurrentSession()) {
            rowsUpdated = bookDAO.deleteBook(bookId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();

        Optional<Book> optionalBook = Optional.empty();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalBook = bookDAO.findBookById(bookId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        int finalRowsUpdated = rowsUpdated;

        Optional<Book> finalOptionalBook = optionalBook;

        Assertions.assertAll("Проверка удаления автора",
                () -> assertEquals(1, finalRowsUpdated, "Значение rowsUpdated должно быть 1"),
                () -> assertFalse(finalOptionalBook.isPresent(), "Значение optionalBook должно быть false")
        );
    }

    @Test
    @Order(6)
    @DisplayName("Тест получения списка авторов книги по ID")
    @Story(value = "Тестирование метода получения связанных элементов по ID элемента владельца")
    void getAuthorListFromBDByBookId() {
        sessionManager.beginSession();

        int bookId = 1;
        List<Author> authorList = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            authorList = bookDAO.getAuthorListFromBDByBookId(bookId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assertions.assertEquals(3, authorList.size(), "Размер полученного списка должен быть 3");
    }

    @Test
    @Order(7)
    @DisplayName("Тест добавления связей авторов с книгой")
    @Story(value = "Тестирование метода добавления связи между элементом владельцем и связанных с ним элементов")
    void addRelationAuthorBook() {
        sessionManager.beginSession();

        int bookId = 1;
        int authorId = 6;

        try (Connection connection = sessionManager.getCurrentSession()) {
            bookDAO.addRelationAuthorBook(authorId, bookId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();

        List<Author> authorList = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            authorList = bookDAO.getAuthorListFromBDByBookId(bookId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assertions.assertEquals(4, authorList.size(), "Размер полученного списка должен быть 4");
    }

    @Test
    @Order(8)
    @DisplayName("Тест удаления связей авторов с книгой")
    @Story(value = "Тестирование метода удаления связи между элементом владельцем и связанных с ним элементов")
    void removeRelationAuthorBook() {
        sessionManager.beginSession();

        int authorId = 1;
        int bookId = 5;

        Author author = new Author();
        author.setId(authorId);
        author.setName("author_name1");
        author.setSurname("author_surname1");

        List<Author> authorList = new ArrayList<>();
        authorList.add(author);

        try (Connection connection = sessionManager.getCurrentSession()) {
            bookDAO.removeRelationAuthorBook(bookId, authorList, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();

        List<Author> authorListFromBD = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            authorListFromBD = bookDAO.getAuthorListFromBDByBookId(bookId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assertions.assertEquals(3, authorListFromBD.size(), "Размер полученного списка должен быть 3");
    }

    @Test
    @Order(9)
    @DisplayName("Тест подсчёта в БД количества книг найденных по ID жанра")
    @Story(value = "Тестирование метода посчёта элементов в БД по входным параметрам")
    void countBookByGenreId() {
        sessionManager.beginSession();

        int genreId = 5;
        int countBook = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            countBook = bookDAO.countBookByGenreId(genreId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assertions.assertEquals(2, countBook, "Количество найденных книг должно быть 2");
    }

    @Test
    @Order(10)
    @DisplayName("Тест подсчёта в БД количества книг найденных по названию")
    @Story(value = "Тестирование метода посчёта элементов в БД по входным параметрам")
    void countBookByTitle() {
        sessionManager.beginSession();

        String bookTitle = "book1";
        int countBook = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            countBook = bookDAO.countBookByTitle(bookTitle, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assertions.assertEquals(1, countBook, "Количество найденных книг должно быть 1");
    }
}