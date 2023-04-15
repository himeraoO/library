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
@Feature(value = "Тестирование AuthorDAO")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorDAOImplTest extends BaseDAOWithBDTest {

    private SessionManager sessionManager;
    private AuthorDAO authorDAO;

    @BeforeEach
    void setUp() {
        mySQLContainer.start();
        sessionManager = new SessionManagerJDBC(
                mySQLContainer.getJdbcUrl(),
                mySQLContainer.getUsername(),
                mySQLContainer.getPassword(),
                mySQLContainer.getDriverClassName());

        authorDAO = new AuthorDAOImpl();
    }

    @AfterEach
    void tearDown() {
        mySQLContainer.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Тест поиска автора по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void FindAuthorById() {
        int authorId = 1;
        Optional<Author> optionalAuthor = Optional.empty();
        Author authorDB = new Author();
        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalAuthor = authorDAO.findAuthorById(authorId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (optionalAuthor.isPresent()) {
            authorDB = optionalAuthor.get();
        }

        Author author = new Author();
        author.setId(authorId);
        author.setName("author_name1");
        author.setSurname("author_surname1");

        Book book1 = new Book();
        book1.setTitle("book1");
        Genre genre1 = new Genre();
        genre1.setId(1);
        genre1.setName("genre1");
        book1.setGenre(genre1);
        author.getBookList().add(book1);

        Book book2 = new Book();
        book2.setTitle("book5");
        Genre genre2 = new Genre();
        genre2.setId(5);
        genre2.setName("genre5");
        book2.setGenre(genre2);
        author.getBookList().add(book2);

        Author authorDBFinal = authorDB;

        List<Book> authorBookList = author.getBookList();
        List<Book> authorDBFinalBookList = authorDB.getBookList();

        Assertions.assertAll("Проверка получения автора по id",
                () -> assertEquals(authorId, authorDBFinal.getId(), "Должно быть значение: " + authorId),
                () -> assertEquals(author, authorDBFinal),
                () -> assertEquals(authorBookList, authorDBFinalBookList)
        );
    }

    @Test
    @Order(2)
    @DisplayName("Тест поиска всех авторов")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAllAuthor() {
        sessionManager.beginSession();

        List<Author> authorList = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            authorList = authorDAO.findAllAuthor(connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        Assertions.assertEquals(6, authorList.size(), "Размер полученного списка должен быть 6");
    }

    @Test
    @Order(3)
    @DisplayName("Тест сохранения нового автора")
    @Story(value = "Тестирование метода сохранения элемента")
    void saveAuthor() {
        sessionManager.beginSession();

        Author author = new Author();
        author.setName("author_name7");
        author.setSurname("author_surname7");

        int expectedAuthorId = 7;
        int authorId = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            authorId = authorDAO.saveAuthor(author, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();

        Optional<Author> optionalAuthor = Optional.empty();
        Author authorDB = new Author();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalAuthor = authorDAO.findAuthorById(expectedAuthorId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (optionalAuthor.isPresent()) {
            authorDB = optionalAuthor.get();
        }

        Author authorDBFinal = authorDB;
        int finalAuthorId = authorId;

        Assertions.assertAll("Проверка сохранения автора",
                () -> assertEquals(expectedAuthorId, finalAuthorId, "Значение ID должно быть " + expectedAuthorId),
                () -> assertEquals(author, authorDBFinal)
        );
    }

    @Test
    @Order(4)
    @DisplayName("Тест обновления автора")
    @Story(value = "Тестирование метода обновления элемента")
    void updatedAuthor() {
        sessionManager.beginSession();

        int authorId = 1;

        Author author = new Author();
        author.setId(authorId);
        author.setName("author_name7");
        author.setSurname("author_surname7");

        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            rowsUpdated = authorDAO.updatedAuthor(author, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();
        Optional<Author> optionalAuthor = Optional.empty();
        Author authorDB = new Author();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalAuthor = authorDAO.findAuthorById(authorId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (optionalAuthor.isPresent()) {
            authorDB = optionalAuthor.get();
        }

        Author authorDBFinal = authorDB;
        int finalRowUpdated = rowsUpdated;


        Assertions.assertAll("Проверка обновления автора",
                () -> assertEquals(1, finalRowUpdated, "Значение rowsUpdated должно быть 1"),
                () -> assertEquals(authorId, authorDBFinal.getId(), "Значение ID должно быть " + authorId),
                () -> assertEquals(author, authorDBFinal)
        );
    }

    @Test
    @Order(5)
    @DisplayName("Тест удаления автора по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteAuthor() {
        sessionManager.beginSession();

        int rowsUpdated = 0;
        int authorId = 6;
        try (Connection connection = sessionManager.getCurrentSession()) {
            rowsUpdated = authorDAO.deleteAuthor(authorId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();

        Optional<Author> optionalAuthor = Optional.empty();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalAuthor = authorDAO.findAuthorById(authorId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        int finalRowsUpdated = rowsUpdated;

        Optional<Author> finalOptionalAuthor = optionalAuthor;

        Assertions.assertAll("Проверка удаления автора",
                () -> assertEquals(1, finalRowsUpdated, "Значение rowsUpdated должно быть 1"),
                () -> assertFalse(finalOptionalAuthor.isPresent(), "Значение optionalAuthor должно быть false")
        );
    }

    @Test
    @Order(6)
    @DisplayName("Тест получения списка книг автора по ID")
    @Story(value = "Тестирование метода получения связанных элементов по ID элемента владельца")
    void getBookListFromBDByAuthorId() {
        sessionManager.beginSession();

        int authorId = 1;
        List<Book> bookList = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            bookList = authorDAO.getBookListFromBDByAuthorId(authorId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assertions.assertEquals(2, bookList.size(), "Размер полученного списка должен быть 2");
    }

    @Test
    @Order(7)
    @DisplayName("Тест добавления связей книг с автором")
    @Story(value = "Тестирование метода добавления связи между элементом владельцем и связанных с ним элементов")
    void addRelationAuthorBook() {
        sessionManager.beginSession();

        int authorId = 1;
        int bookId = 6;

        try (Connection connection = sessionManager.getCurrentSession()) {
            authorDAO.addRelationAuthorBook(authorId, bookId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();

        List<Book> bookList = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            bookList = authorDAO.getBookListFromBDByAuthorId(authorId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assertions.assertEquals(3, bookList.size(), "Размер полученного списка должен быть 3");
    }

    @Test
    @Order(8)
    @DisplayName("Тест удаления связей книг с автором")
    @Story(value = "Тестирование метода удаления связи между элементом владельцем и связанных с ним элементов")
    void removeRelationBookAuthor() {
        sessionManager.beginSession();

        int authorId = 1;
        int bookId = 5;
        Book book = new Book();
        book.setId(bookId);
        book.setTitle("book5");
        Genre genre = new Genre();
        genre.setId(5);
        genre.setName("genre5");
        book.setGenre(genre);

        List<Book> bookList = new ArrayList<>();
        bookList.add(book);

        try (Connection connection = sessionManager.getCurrentSession()) {
            authorDAO.removeRelationBookAuthor(authorId, bookList, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();

        List<Book> bookListFromBD = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            bookListFromBD = authorDAO.getBookListFromBDByAuthorId(authorId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assertions.assertEquals(1, bookListFromBD.size(), "Размер полученного списка должен быть 1");
    }

    @Test
    @Order(9)
    @DisplayName("Тест подсчёта в БД количества авторов найденных по имени+фамилии")
    @Story(value = "Тестирование метода посчёта элементов в БД по входным параметрам")
    void countAuthorByNameAndSurname() {
        sessionManager.beginSession();

        String authorName = "author_name1";
        String authorSurname = "author_surname1";
        int countAuthor = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            countAuthor = authorDAO.countAuthorByNameAndSurname(authorName, authorSurname, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assertions.assertEquals(1, countAuthor, "Количество найденных авторов должно быть 1");
    }
}