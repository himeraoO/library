package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotDeletedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
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
import java.util.Collections;
import java.util.List;

import static com.github.himeraoo.library.util.TestUtils.getFullGenre;
import static com.github.himeraoo.library.util.TestUtils.getGenreDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

@Epic(value = "Тестирование слоя Service")
@Feature(value = "Тестирование GenreService")
@Execution(ExecutionMode.CONCURRENT)
class GenreServiceImplTest extends BaseServiceTest {

    @Test
    @DisplayName("Тест поиска жанра по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findById() throws SQLException, ElementHasNotFoundException {
        int genreID = 1;
        Genre genre = getFullGenre(genreID);
        GenreDTO genreDTO = getGenreDTO(genre);

        GenreDTO genreDTOFromBD = genreService.findById(genreID);

        Mockito.verify(genreRepository, Mockito.times(1)).findById(genreID);
        assertEquals(genreDTO, genreDTOFromBD);
    }

    @Test
    @DisplayName("Тест поиска жанра по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findByIdThrowException() throws SQLException, ElementHasNotFoundException {
        int genreID = 100;

        assertThrows(ElementHasNotFoundException.class, () -> {
            genreService.findById(genreID);
        });
    }

    @Test
    @DisplayName("Тест поиска всех жанров")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAll() throws SQLException, ElementHasNotFoundException {
        int genreID = 1;
        Genre genre = getFullGenre(genreID);
        GenreDTO genreDTO = getGenreDTO(genre);
        List<GenreDTO> expectedGenreDTOList = Collections.singletonList(genreDTO);

        List<GenreDTO> genreDTOList = genreService.findAll();

        Mockito.verify(genreRepository, Mockito.times(1)).findAll();
        assertEquals(expectedGenreDTOList, genreDTOList);
    }

    @Test
    @DisplayName("Тест поиска всех жанров")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAllThrowException() throws SQLException, ElementHasNotFoundException {
        lenient().when(genreRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ElementHasNotFoundException.class, () -> {
            genreService.findAll();
        });
    }

    @Test
    @DisplayName("Тест сохранения нового жанра")
    @Story(value = "Тестирование метода сохранения элемента")
    void save() throws ElementHasNotAddedException, SQLException {
        int genreID = 1;
        int expectedAddedID = 1;
        Genre genre = getFullGenre(genreID);
        GenreDTO genreDTO = getGenreDTO(genre);

        int addedId = genreService.save(genreDTO);

        Mockito.verify(genreRepository, Mockito.times(1)).save(genre);
        assertEquals(expectedAddedID, addedId);
    }

    @Test
    @DisplayName("Тест сохранения нового жанра")
    @Story(value = "Тестирование метода сохранения элемента")
    void saveThrowExceptionNotAdded() throws ElementHasNotAddedException, SQLException {
        int genreID = 0;
        Genre genreSaveNotAdded = getFullGenre(genreID, "NotAdded");
        GenreDTO genreDTO = getGenreDTO(genreSaveNotAdded);

        assertThrows(ElementHasNotAddedException.class, () -> {
            genreService.save(genreDTO);
        });
    }

    @Test
    @DisplayName("Тест сохранения нового жанра")
    @Story(value = "Тестирование метода сохранения элемента")
    void saveThrowExceptionNotAddedIsExist() throws ElementHasNotAddedException, SQLException {
        int genreID = 777;
        Genre genreSaveNotAddedIsExist = getFullGenre(genreID, "exist");
        GenreDTO genreDTO = getGenreDTO(genreSaveNotAddedIsExist);

        assertThrows(ElementHasNotAddedException.class, () -> {
            genreService.save(genreDTO);
        });
    }

    @Test
    @DisplayName("Тест обновления жанра")
    @Story(value = "Тестирование метода обновления элемента")
    void update() throws ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        int genreID = 1;
        int expectedUpdatedID = 1;
        Genre genre = getFullGenre(genreID, "genre1U");
        GenreDTO genreDTO = getGenreDTO(genre);

        int updatedId = genreService.update(genreDTO);

        Mockito.verify(genreRepository, Mockito.times(1)).update(genre);
        assertEquals(expectedUpdatedID, updatedId);
    }

    @Test
    @DisplayName("Тест обновления жанра")
    @Story(value = "Тестирование метода обновления элемента")
    void updateThrowExceptionNotFound() throws ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        int genreID = 100;
        Genre genreForUpdateNotFound = getFullGenre(genreID, "genre1");
        GenreDTO genreDTO = getGenreDTO(genreForUpdateNotFound);

        assertThrows(ElementHasNotFoundException.class, () -> {
            genreService.update(genreDTO);
        });
    }

    @Test
    @DisplayName("Тест обновления жанра")
    @Story(value = "Тестирование метода обновления элемента")
    void updateThrowExceptionNotUpdated() throws ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        int genreID = 0;
        Genre genreForUpdateNotUpdated = getFullGenre(genreID, "genre0");
        GenreDTO genreDTO = getGenreDTO(genreForUpdateNotUpdated);

        assertThrows(ElementHasNotUpdatedException.class, () -> {
            genreService.update(genreDTO);
        });
    }

    @Test
    @DisplayName("Тест удаления жанра по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteById() throws SQLException, ElementHasNotDeletedException, ElementHasNotFoundException {
        int genreID = 1;
        int expectedDeletedID = 1;

        int deletedId = genreService.deleteById(genreID);

        Mockito.verify(genreRepository, Mockito.times(1)).deleteById(genreID);
        assertEquals(expectedDeletedID, deletedId);
    }

    @Test
    @DisplayName("Тест удаления жанра по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteByIdThrowExceptionNotFound() throws SQLException, ElementHasNotDeletedException, ElementHasNotFoundException {
        int genreID = 100;
        assertThrows(ElementHasNotFoundException.class, () -> {
            genreService.deleteById(genreID);
        });
    }

    @Test
    @DisplayName("Тест удаления жанра по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteByIdThrowExceptionNotDeleted() throws SQLException, ElementHasNotDeletedException, ElementHasNotFoundException {
        int genreID = 0;
        assertThrows(ElementHasNotDeletedException.class, () -> {
            genreService.deleteById(genreID);
        });
    }
}