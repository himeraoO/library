package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.jdbc.SessionManagerJDBC;
import com.github.himeraoo.library.models.Author;
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

import static com.github.himeraoo.library.util.TestUtils.getAuthor;
import static com.github.himeraoo.library.util.TestUtils.getFullAuthor;
import static com.github.himeraoo.library.util.TestUtils.getFullBook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic(value = "Тестирование слоя DAO)")
@Feature(value = "Тестирование AuthorDAO")
class AuthorDAOImplTest extends BaseDAOTest {

    private AuthorDAO authorDAO;

    @BeforeEach
    void setUp() {
        SessionManager sessionManager = new SessionManagerJDBC(
                mySQLContainer.getJdbcUrl(),
                mySQLContainer.getUsername(),
                mySQLContainer.getPassword(),
                mySQLContainer.getDriverClassName()
        );

        authorDAO = new AuthorDAOImpl(sessionManager);
    }

    @Test
    @Order(1)
    @DisplayName("Тест поиска автора по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findById() throws SQLException {
        int authorId = 1;
        Author expectedAuthor = getFullAuthor(authorId);

        Optional<Author> optionalAuthor = authorDAO.findById(authorId);

        Author authorDB = optionalAuthor.get();

        Assertions.assertAll("Проверка получения автора по id",
                () -> assertNotNull(authorDB),
                () -> assertEquals(authorId, authorDB.getId(),
                        "Должно быть значение: " + authorId),
                () -> assertEquals(expectedAuthor, authorDB),
                () -> assertEquals(expectedAuthor.getBookList(), authorDB.getBookList())
        );
    }

    @Test
    @Order(2)
    @DisplayName("Тест поиска всех авторов")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAll() throws SQLException {
        List<Author> authorListFromBD = authorDAO.findAll();

        assertEquals(6, authorListFromBD.size(),
                "Размер полученного списка должен быть 6");
    }

    @Test
    @Order(3)
    @DisplayName("Тест сохранения нового автора")
    @Story(value = "Тестирование метода сохранения элемента")
    void save() throws SQLException {
        int expectedAddedId = 7;
        int authorId = 7;
        String authorName = "save_author_name";
        String authorSurname = "save_author_surname";
        Author authorForSave = getAuthor(authorId, authorName, authorSurname, Collections.singletonList(getFullBook(1)));
        List<Author> authorListFromBDBefore = authorDAO.findAll();

        int addedId = authorDAO.save(authorForSave);

        Optional<Author> optionalAuthor = authorDAO.findById(7);
        Author authorDB = optionalAuthor.get();
        List<Author> authorListFromBDAfter = authorDAO.findAll();

        Assertions.assertAll("Проверка сохранения автора",
                () -> assertEquals(6, authorListFromBDBefore.size(),
                        "Размер начального списка должен быть 6"),
                () -> assertNotNull(authorDB),
                () -> assertEquals(expectedAddedId, addedId),
                () -> assertEquals(authorId, authorDB.getId(),
                        "Должно быть значение: " + authorId),
                () -> assertEquals(authorForSave, authorDB),
                () -> assertEquals(authorForSave.getBookList(), authorDB.getBookList()),
                () -> assertEquals(7, authorListFromBDAfter.size(),
                        "Размер конечного списка должен быть 7")
        );
    }

    @Test
    @Order(4)
    @DisplayName("Тест обновления автора")
    @Story(value = "Тестирование метода обновления элемента")
    void update() throws SQLException {
        int authorId = 7;
        int rowUpdatedExpected = 1;
        String authorName = "update_author_name";
        String authorSurname = "update_author_surname";
        Author authorForUpdate = getAuthor(authorId, authorName, authorSurname, Collections.singletonList(getFullBook(1)));

        int rowUpdated = authorDAO.update(authorForUpdate);

        Optional<Author> optionalAuthor = authorDAO.findById(7);
        Author authorDB = optionalAuthor.get();

        Assertions.assertAll("Проверка обновления автора",
                () -> assertNotNull(authorDB),
                () -> assertEquals(rowUpdatedExpected, rowUpdated),
                () -> assertEquals(authorId, authorDB.getId(),
                        "Должно быть значение: " + authorId),
                () -> assertEquals(authorForUpdate, authorDB),
                () -> assertEquals(authorForUpdate.getBookList(), authorDB.getBookList())
        );
    }

    @Test
    @Order(5)
    @DisplayName("Тест удаления автора по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteById() throws SQLException {
        int authorId = 7;
        int rowDeletedExpected = 1;
        List<Author> authorListFromBDBefore = authorDAO.findAll();

        int rowDeleted = authorDAO.deleteById(authorId);

        List<Author> authorListFromBDAfter = authorDAO.findAll();

        assertEquals(rowDeletedExpected, rowDeleted);
        Assertions.assertAll("Проверка удаления автора",
                () -> assertEquals(7, authorListFromBDBefore.size(),
                        "Размер полученного списка должен быть 7"),
                () -> assertEquals(rowDeletedExpected, rowDeleted),
                () -> assertEquals(6, authorListFromBDAfter.size(),
                        "Размер полученного списка должен быть 6")
        );
    }
}