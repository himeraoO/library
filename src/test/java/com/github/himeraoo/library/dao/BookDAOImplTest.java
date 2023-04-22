package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.jdbc.SessionManagerJDBC;
import com.github.himeraoo.library.models.Book;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.himeraoo.library.util.TestUtils.getBook;
import static com.github.himeraoo.library.util.TestUtils.getFullAuthor;
import static com.github.himeraoo.library.util.TestUtils.getFullBook;
import static com.github.himeraoo.library.util.TestUtils.getFullGenre;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic(value = "Тестирование слоя DAO")
@Feature(value = "Тестирование BookDAO")
class BookDAOImplTest extends BaseDAOTest {

    private BookDAO bookDAO;

    @BeforeEach
    void setUp() {
        SessionManager sessionManager = new SessionManagerJDBC(
                mySQLContainer.getJdbcUrl(),
                mySQLContainer.getUsername(),
                mySQLContainer.getPassword(),
                mySQLContainer.getDriverClassName()
        );

        bookDAO = new BookDAOImpl(sessionManager);
    }

    @Test
    @Order(1)
    @DisplayName("Тест поиска книги по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findById() throws SQLException {
        int bookId = 1;
        Book expectedBook = getFullBook(bookId);

        Optional<Book> optionalBook = bookDAO.findById(bookId);

        Book bookDB = optionalBook.get();

        Assertions.assertAll("Проверка получения книги по id",
                () -> assertNotNull(bookDB),
                () -> assertEquals(bookId, bookDB.getId(),
                        "Должно быть значение: " + bookId),
                () -> assertEquals(expectedBook, bookDB),
                () -> assertEquals(expectedBook.getAuthorList(), bookDB.getAuthorList())
        );
    }

    @Test
    @Order(2)
    @DisplayName("Тест поиска всех книг")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAll() throws SQLException {
        List<Book> bookListFromBD = bookDAO.findAll();

        assertEquals(6, bookListFromBD.size(),
                "Размер полученного списка должен быть 6");
    }

    @Test
    @Order(3)
    @DisplayName("Тест сохранения новой книги")
    @Story(value = "Тестирование метода сохранения элемента")
    void save() throws SQLException {
        int expectedAddedId = 7;
        int bookId = 7;
        String bookTitle = "save_book_title";
        Book bookForSave = getBook(bookId, bookTitle, getFullGenre(1), Collections.singletonList(getFullAuthor(1)));
        List<Book> bookListFromBDBefore = bookDAO.findAll();

        int addedId = bookDAO.save(bookForSave);

        Optional<Book> optionalBook = bookDAO.findById(7);
        Book bookDB = optionalBook.get();
        List<Book> bookListFromBDAfter = bookDAO.findAll();

        Assertions.assertAll("Проверка сохранения книги",
                () -> assertEquals(6, bookListFromBDBefore.size(),
                        "Размер начального списка должен быть 6"),
                () -> assertNotNull(bookDB),
                () -> assertEquals(expectedAddedId, addedId),
                () -> assertEquals(bookId, bookDB.getId(),
                        "Должно быть значение: " + bookId),
                () -> assertEquals(bookForSave, bookDB),
                () -> assertEquals(bookForSave.getAuthorList(), bookDB.getAuthorList()),
                () -> assertEquals(7, bookListFromBDAfter.size(),
                        "Размер конечного списка должен быть 7")
        );
    }

    @Test
    @Order(4)
    @DisplayName("Тест обновления книги")
    @Story(value = "Тестирование метода обновления элемента")
    void update() throws SQLException {
        int bookId = 7;
        int rowUpdatedExpected = 1;
        String bookTitle = "update_book_title";
        Book bookForUpdate = getBook(bookId, bookTitle, getFullGenre(1), Collections.singletonList(getFullAuthor(1)));

        int rowUpdated = bookDAO.update(bookForUpdate);

        Optional<Book> optionalBook = bookDAO.findById(7);
        Book bookDB = optionalBook.get();

        Assertions.assertAll("Проверка обновления книги",
                () -> assertNotNull(bookDB),
                () -> assertEquals(rowUpdatedExpected, rowUpdated),
                () -> assertEquals(bookId, bookDB.getId(),
                        "Должно быть значение: " + bookId),
                () -> assertEquals(bookForUpdate, bookDB),
                () -> assertEquals(bookForUpdate.getAuthorList(), bookDB.getAuthorList())
        );
    }

    @Test
    @Order(5)
    @DisplayName("Тест удаления книги по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteById() throws SQLException {
        int bookId = 7;
        int rowDeletedExpected = 1;
        List<Book> bookListFromBDBefore = bookDAO.findAll();

        int rowDeleted = bookDAO.deleteById(bookId);

        List<Book> bookListFromBDAfter = bookDAO.findAll();

        assertEquals(rowDeletedExpected, rowDeleted);
        Assertions.assertAll("Проверка удаления книги",
                () -> assertEquals(7, bookListFromBDBefore.size(),
                        "Размер полученного списка должен быть 7"),
                () -> assertEquals(rowDeletedExpected, rowDeleted),
                () -> assertEquals(6, bookListFromBDAfter.size(),
                        "Размер полученного списка должен быть 6")
        );
    }
}