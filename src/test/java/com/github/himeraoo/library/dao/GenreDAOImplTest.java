package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.jdbc.SessionManagerJDBC;
import com.github.himeraoo.library.models.Genre;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.github.himeraoo.library.util.TestUtils.getFullGenre;
import static com.github.himeraoo.library.util.TestUtils.getGenre;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic(value = "Тестирование слоя DAO")
@Feature(value = "Тестирование GenreDAO")
class GenreDAOImplTest extends BaseDAOTest {

    private GenreDAO genreDAO;

    @BeforeEach
    void setUp() {
        SessionManager sessionManager = new SessionManagerJDBC(
                mySQLContainer.getJdbcUrl(),
                mySQLContainer.getUsername(),
                mySQLContainer.getPassword(),
                mySQLContainer.getDriverClassName()
        );

        genreDAO = new GenreDAOImpl(sessionManager);
    }

    @Test
    @Order(1)
    @DisplayName("Тест поиска жанра по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findById() throws SQLException {
        int genreId = 1;
        Genre expectedGenre = getFullGenre(genreId);

        Optional<Genre> optionalGenre = genreDAO.findById(genreId);

        Genre genreDB = optionalGenre.get();

        Assertions.assertAll("Проверка получения жанра по id",
                () -> assertNotNull(genreDB),
                () -> assertEquals(genreId, genreDB.getId(), "Должно быть значение: " + genreId),
                () -> assertEquals(expectedGenre, genreDB)
        );
    }

    @Test
    @Order(2)
    @DisplayName("Тест поиска всех жанров")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAll() throws SQLException {
        List<Genre> genreListFromBD = genreDAO.findAll();

        assertEquals(6, genreListFromBD.size(),
                "Размер полученного списка должен быть 6");
    }

    @Test
    @Order(3)
    @DisplayName("Тест сохранения нового жанра")
    @Story(value = "Тестирование метода сохранения элемента")
    void save() throws SQLException {
        int expectedAddedId = 7;
        int genreId = 7;
        String genreName = "save_genre_name";
        Genre genreForSave = getGenre(genreId, genreName);
        List<Genre> genreListFromBDBefore = genreDAO.findAll();

        int addedId = genreDAO.save(genreForSave);

        Optional<Genre> optionalGenre = genreDAO.findById(7);
        Genre genreDB = optionalGenre.get();
        List<Genre> genreListFromBDAfter = genreDAO.findAll();

        Assertions.assertAll("Проверка сохранения жанра",
                () -> assertEquals(6, genreListFromBDBefore.size(),
                        "Размер начального списка должен быть 6"),
                () -> assertNotNull(genreDB),
                () -> assertEquals(expectedAddedId, addedId),
                () -> assertEquals(genreId, genreDB.getId(),
                        "Должно быть значение: " + genreId),
                () -> assertEquals(genreForSave, genreDB),
                () -> assertEquals(7, genreListFromBDAfter.size(),
                        "Размер конечного списка должен быть 7")
        );
    }

    @Test
    @Order(4)
    @DisplayName("Тест обновления жанра")
    @Story(value = "Тестирование метода обновления элемента")
    void update() throws SQLException {
        int genreId = 7;
        int rowUpdatedExpected = 1;
        String genreName = "update_genre_name";
        Genre genreForUpdate = getGenre(genreId, genreName);

        int rowUpdated = genreDAO.update(genreForUpdate);

        Optional<Genre> optionalGenre = genreDAO.findById(7);
        Genre genreDB = optionalGenre.get();

        Assertions.assertAll("Проверка обновления жанра по id",
                () -> assertNotNull(genreDB),
                () -> assertEquals(rowUpdatedExpected, rowUpdated),
                () -> assertEquals(genreId, genreDB.getId(),
                        "Должно быть значение: " + genreId),
                () -> assertEquals(genreForUpdate, genreDB)
        );
    }

    @Test
    @Order(5)
    @DisplayName("Тест удаления жанра по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteById() throws SQLException {
        int genreId = 7;
        int rowDeletedExpected = 1;
        List<Genre> genreListFromBDBefore = genreDAO.findAll();

        int rowDeleted = genreDAO.deleteById(genreId);

        List<Genre> genreListFromBDAfter = genreDAO.findAll();

        Assertions.assertAll("Проверка удаления жанра",
                () -> assertEquals(7, genreListFromBDBefore.size(),
                        "Размер начального списка должен быть 7"),
                () -> assertEquals(rowDeletedExpected, rowDeleted),
                () -> assertEquals(6, genreListFromBDAfter.size(),
                        "Размер конечного списка должен быть 6")
        );
    }
}