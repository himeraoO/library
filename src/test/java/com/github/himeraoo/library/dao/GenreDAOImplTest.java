package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.jdbc.SessionManagerJDBC;
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
@Feature(value = "Тестирование GenreDAO")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenreDAOImplTest extends BaseDAOWithBDTest {

    private SessionManager sessionManager;
    private GenreDAO genreDAO;

    @BeforeEach
    void setUp() {
        mySQLContainer.start();
        sessionManager = new SessionManagerJDBC(
                mySQLContainer.getJdbcUrl(),
                mySQLContainer.getUsername(),
                mySQLContainer.getPassword(),
                mySQLContainer.getDriverClassName());

        genreDAO = new GenreDAOImpl();
    }

    @AfterEach
    void tearDown() {
        mySQLContainer.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Тест поиска жанра по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findGenreById() {
        sessionManager.beginSession();

        int genreId = 1;
        Optional<Genre> optionalGenre = Optional.empty();
        Genre genreDB = new Genre();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalGenre = genreDAO.findGenreById(genreId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (optionalGenre.isPresent()) {
            genreDB = optionalGenre.get();
        }

        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("genre1");

        Genre genreDBFinal = genreDB;

        Assertions.assertAll("Проверка получения жанра по id",
                () -> assertEquals(genreId, genreDBFinal.getId(), "Должно быть значение: " + genreId),
                () -> assertEquals(genre, genreDBFinal)
        );
    }

    @Test
    @Order(2)
    @DisplayName("Тест поиска всех жанров")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAllGenre() {
        sessionManager.beginSession();

        List<Genre> genreList = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            genreList = genreDAO.findAllGenre(connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        Assertions.assertEquals(6, genreList.size(), "Размер полученного списка должен быть 6");
    }

    @Test
    @Order(3)
    @DisplayName("Тест сохранения нового жанра")
    @Story(value = "Тестирование метода сохранения элемента")
    void saveGenre() {
        sessionManager.beginSession();

        Genre genre = new Genre();
        genre.setName("genre7");

        int expectedGenreId = 7;
        int genreId = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            genreId = genreDAO.saveGenre(genre, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();

        Optional<Genre> optionalGenre = Optional.empty();
        Genre genreDB = new Genre();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalGenre = genreDAO.findGenreById(expectedGenreId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (optionalGenre.isPresent()) {
            genreDB = optionalGenre.get();
        }

        Genre genreDBFinal = genreDB;
        int finalGenreId = genreId;

        Assertions.assertAll("Проверка сохранения жанра",
                () -> assertEquals(expectedGenreId, finalGenreId, "Значение ID должно быть " + expectedGenreId),
                () -> assertEquals(genre, genreDBFinal)
        );
    }

    @Test
    @Order(4)
    @DisplayName("Тест обновления жанра")
    @Story(value = "Тестирование метода обновления элемента")
    void updatedGenre() {
        sessionManager.beginSession();

        int genreId = 1;

        Genre genre = new Genre();
        genre.setId(genreId);
        genre.setName("genre7");

        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            rowsUpdated = genreDAO.updatedGenre(genre, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();
        Optional<Genre> optionalGenre = Optional.empty();
        Genre genreDB = new Genre();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalGenre = genreDAO.findGenreById(genreId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (optionalGenre.isPresent()) {
            genreDB = optionalGenre.get();
        }

        Genre genreDBFinal = genreDB;
        int finalRowUpdated = rowsUpdated;


        Assertions.assertAll("Проверка обновления жанра",
                () -> assertEquals(1, finalRowUpdated, "Значение rowsUpdated должно быть 1"),
                () -> assertEquals(genreId, genreDBFinal.getId(), "Значение ID должно быть " + genreId),
                () -> assertEquals(genre, genreDBFinal)
        );
    }

    @Test
    @Order(5)
    @DisplayName("Тест удаления жанра по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteGenre() {
        sessionManager.beginSession();

        int rowsUpdated = 0;
        int genreId = 6;
        try (Connection connection = sessionManager.getCurrentSession()) {
            rowsUpdated = genreDAO.deleteGenre(genreId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sessionManager.beginSession();

        Optional<Genre> optionalGenre = Optional.empty();

        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalGenre = genreDAO.findGenreById(genreId, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        int finalRowsUpdated = rowsUpdated;

        Optional<Genre> finalOptionalGenre = optionalGenre;

        Assertions.assertAll("Проверка удаления жанра",
                () -> assertEquals(1, finalRowsUpdated, "Значение rowsUpdated должно быть 1"),
                () -> assertFalse(finalOptionalGenre.isPresent(), "Значение optionalBook должно быть false")
        );
    }

    @Test
    @Order(6)
    @DisplayName("Тест подсчёта в БД количества жанра найденных по названию")
    @Story(value = "Тестирование метода посчёта элементов в БД по входным параметрам")
    void countGenreByName() {
        sessionManager.beginSession();

        String genreName = "genre1";
        int countGenre = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            countGenre = genreDAO.countGenreByName(genreName, connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assertions.assertEquals(1, countGenre, "Количество найденных жанров должно быть 1");
    }
}