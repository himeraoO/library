package com.github.himeraoo.library.repository;

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

import static com.github.himeraoo.library.util.TestUtils.getFullGenre;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic(value = "Тестирование слоя Repository")
@Feature(value = "Тестирование GenreRepository")
@Execution(ExecutionMode.CONCURRENT)
class GenreRepositoryImplTest extends BaseRepositoryTest {

    @Test
    @DisplayName("Тест поиска жанра по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findById() throws SQLException {
        int genreId = 1;

        Genre expectedGenre = getFullGenre(genreId);

        Optional<Genre> optionalGenre = genreRepository.findById(genreId);

        Mockito.verify(genreDAO, Mockito.times(1)).findGenreById(genreId, connection);
        assertEquals(Optional.of(expectedGenre), optionalGenre);
    }

    @Test
    @DisplayName("Тест поиска всех жанров")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAll() throws SQLException {
        Genre oldGenre = getFullGenre(1);
        List<Genre> expectedGenreList = new ArrayList<>();
        expectedGenreList.add(oldGenre);

        List<Genre> genreListFromBD = genreRepository.findAll();

        Mockito.verify(genreDAO, Mockito.times(1)).findAllGenre(connection);
        assertEquals(expectedGenreList, genreListFromBD);
    }

    @Test
    @DisplayName("Тест сохранения нового жанра")
    @Story(value = "Тестирование метода сохранения элемента")
    void save() throws SQLException {
        int genreId = 1;
        int expectedAddedId = 1;

        Genre genreForSave = getFullGenre(genreId);
        int addedId = genreRepository.save(genreForSave);

        Mockito.verify(genreDAO, Mockito.times(1)).saveGenre(genreForSave, connection);
        Mockito.verify(genreDAO, Mockito.times(1)).countGenreByName(genreForSave.getName(), connection);
        assertEquals(expectedAddedId, addedId);
    }

    @Test
    @DisplayName("Тест обновления жанра")
    @Story(value = "Тестирование метода обновления элемента")
    void update() throws SQLException {
        int genreId = 1;
        int rowUpdatedExpected = 1;

        Genre genreForUpdate = getFullGenre(genreId, "genre1U");
        int rowUpdated = genreRepository.update(genreForUpdate);

        Mockito.verify(genreDAO, Mockito.times(1)).updatedGenre(genreForUpdate, connection);
        Mockito.verify(genreDAO, Mockito.times(1)).countGenreByName(genreForUpdate.getName(), connection);
        assertEquals(rowUpdatedExpected, rowUpdated);
    }

    @Test
    @DisplayName("Тест удаления жанра по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteById() throws SQLException {
        int genreId = 1;
        int rowDeletedExpected = 1;

        int rowDeleted = genreRepository.deleteById(1);

        Mockito.verify(genreDAO, Mockito.times(1)).deleteGenre(genreId, connection);
        Mockito.verify(bookDAO, Mockito.times(1)).countBookByGenreId(genreId, connection);
        assertEquals(rowDeletedExpected, rowDeleted);
    }
}