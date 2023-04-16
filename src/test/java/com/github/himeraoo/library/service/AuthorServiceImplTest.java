package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.AuthorDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
import com.github.himeraoo.library.models.Author;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static com.github.himeraoo.library.util.TestUtils.getAuthorDTO;
import static com.github.himeraoo.library.util.TestUtils.getAuthorWithoutBooks;
import static com.github.himeraoo.library.util.TestUtils.getFullAuthor;
import static org.mockito.Mockito.lenient;

@Epic(value = "Тестирование слоя Service")
@Feature(value = "Тестирование AuthorService")
@Execution(ExecutionMode.CONCURRENT)
class AuthorServiceImplTest extends BaseServiceTest {

    @Test
    @DisplayName("Тест поиска автора по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findById() throws SQLException, ElementHasNotFoundException {
        int authorID = 1;
        Author author = getFullAuthor(authorID);
        AuthorDTO authorDTO = getAuthorDTO(author);

        AuthorDTO authorDTOFromBD = authorService.findById(authorID);

        Mockito.verify(authorRepository, Mockito.times(1)).findById(authorID);
        Assertions.assertAll("Проверка получаемого DTO", () -> Assertions.assertEquals(authorDTO, authorDTOFromBD), () -> Assertions.assertEquals(authorDTO.getBookList(), authorDTOFromBD.getBookList()));
    }

    @Test
    @DisplayName("Тест поиска автора по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findByIdThrowException() throws SQLException, ElementHasNotFoundException {
        int authorID = 100;

        Assertions.assertThrows(ElementHasNotFoundException.class, () -> {
            authorService.findById(authorID);
        });
    }

    @Test
    @DisplayName("Тест поиска всех авторов")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAll() throws SQLException, ElementHasNotFoundException {
        int authorID = 1;
        Author author = getFullAuthor(authorID);
        AuthorDTO authorDTO = getAuthorDTO(author);
        List<AuthorDTO> expectedAuthorDTOList = Collections.singletonList(authorDTO);

        List<AuthorDTO> authorDTOList = authorService.findAll();

        Mockito.verify(authorRepository, Mockito.times(1)).findAll();
        Assertions.assertEquals(expectedAuthorDTOList, authorDTOList);
    }

    @Test
    @DisplayName("Тест поиска всех авторов")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAllThrowException() throws SQLException, ElementHasNotFoundException {
        lenient().when(authorRepository.findAll()).thenReturn(Collections.emptyList());

        Assertions.assertThrows(ElementHasNotFoundException.class, () -> {
            authorService.findAll();
        });
    }

    @Test
    @DisplayName("Тест сохранения нового автора")
    @Story(value = "Тестирование метода сохранения элемента")
    void save() throws ElementHasNotAddedException, SQLException {
        int authorID = 1;
        int expectedAddedID = 1;
        Author author = getFullAuthor(authorID);
        AuthorDTO authorDTO = getAuthorDTO(author);

        int addedId = authorService.save(authorDTO);

        Mockito.verify(authorRepository, Mockito.times(1)).save(author);
        Assertions.assertEquals(expectedAddedID, addedId);
    }

    @Test
    @DisplayName("Тест сохранения нового автора")
    @Story(value = "Тестирование метода сохранения элемента")
    void saveThrowExceptionNotAdded() throws ElementHasNotAddedException, SQLException {
        int authorID = 404;
        Author authorSaveNotAdded = getAuthorWithoutBooks(authorID, "NotAdded", "NotAdded");

        AuthorDTO authorDTO = getAuthorDTO(authorSaveNotAdded);

        Assertions.assertThrows(ElementHasNotAddedException.class, () -> {
            authorService.save(authorDTO);
        });
    }

    @Test
    @DisplayName("Тест сохранения нового автора")
    @Story(value = "Тестирование метода сохранения элемента")
    void saveThrowExceptionNotAddedIsExist() throws ElementHasNotAddedException, SQLException {
        int authorID = 400;
        Author authorBadSave = getAuthorWithoutBooks(authorID, "exist", "exist");

        AuthorDTO authorDTO = getAuthorDTO(authorBadSave);

        Assertions.assertThrows(ElementHasNotAddedException.class, () -> {
            authorService.save(authorDTO);
        });
    }

    @Test
    @DisplayName("Тест обновления автора")
    @Story(value = "Тестирование метода обновления элемента")
    void update() throws ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        int authorID = 1;
        int expectedUpdatedID = 1;
        Author author = getFullAuthor(authorID, "author_name1U", "author_surname1U");
        AuthorDTO authorDTO = getAuthorDTO(author);

        int updatedId = authorService.update(authorDTO);

        Mockito.verify(authorRepository, Mockito.times(1)).update(author);
        Assertions.assertEquals(expectedUpdatedID, updatedId);
    }

    @Test
    @DisplayName("Тест обновления автора")
    @Story(value = "Тестирование метода обновления элемента")
    void updateThrowExceptionNotFound() throws ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        int authorID = 1;
        Author author = getFullAuthor(authorID, "author_name1", "author_surname1");
        AuthorDTO authorDTO = getAuthorDTO(author);

        Assertions.assertThrows(ElementHasNotFoundException.class, () -> {
            authorService.update(authorDTO);
        });
    }

    @Test
    @DisplayName("Тест обновления автора")
    @Story(value = "Тестирование метода обновления элемента")
    void updateThrowExceptionNotUpdated() throws ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        int authorID = 1;
        Author author = getFullAuthor(authorID, "author_name11", "author_surname11");
        AuthorDTO authorDTO = getAuthorDTO(author);
        lenient().when(authorRepository.update(author)).thenReturn(-1);

        Assertions.assertThrows(ElementHasNotUpdatedException.class, () -> {
            authorService.update(authorDTO);
        });
    }

    @Test
    @DisplayName("Тест удаления автора по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteById() throws SQLException, ElementHasNotFoundException {
        int authorID = 1;
        int expectedDeletedID = 1;

        int deletedId = authorService.deleteById(authorID);

        Mockito.verify(authorRepository, Mockito.times(1)).deleteById(authorID);
        Assertions.assertEquals(expectedDeletedID, deletedId);
    }

    @Test
    @DisplayName("Тест удаления автора по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteByIdThrowException() throws SQLException, ElementHasNotFoundException {
        int authorID = 100;

        Assertions.assertThrows(ElementHasNotFoundException.class, () -> {
            authorService.deleteById(authorID);
        });
    }
}