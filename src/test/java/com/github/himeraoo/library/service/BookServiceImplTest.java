package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.BookDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
import com.github.himeraoo.library.models.Book;
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

import static com.github.himeraoo.library.util.TestUtils.getBookDTO;
import static com.github.himeraoo.library.util.TestUtils.getBookWithoutAuthors;
import static com.github.himeraoo.library.util.TestUtils.getFullBook;
import static com.github.himeraoo.library.util.TestUtils.getFullGenre;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

@Epic(value = "Тестирование слоя Service")
@Feature(value = "Тестирование BookService")
@Execution(ExecutionMode.CONCURRENT)
class BookServiceImplTest extends BaseServiceTest {

    @Test
    @DisplayName("Тест поиска книги по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findById() throws SQLException, ElementHasNotFoundException {
        int bookID = 1;
        Book book = getFullBook(bookID);
        BookDTO bookDTO = getBookDTO(book);

        BookDTO bookDTOFromBD = bookService.findById(bookID);

        Mockito.verify(bookRepository, Mockito.times(1)).findById(bookID);
        assertAll("Проверка получаемого DTO", () -> assertEquals(bookDTO, bookDTOFromBD), () -> assertEquals(bookDTO.getAuthorList(), bookDTOFromBD.getAuthorList()));
    }

    @Test
    @DisplayName("Тест поиска книги по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findByIdThrowException() throws SQLException, ElementHasNotFoundException {
        int bookID = 100;

        assertThrows(ElementHasNotFoundException.class, () -> {
            bookService.findById(bookID);
        });
    }

    @Test
    @DisplayName("Тест поиска всех книг")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAll() throws SQLException, ElementHasNotFoundException {
        int bookID = 1;
        Book book = getFullBook(bookID);
        BookDTO bookDTO = getBookDTO(book);
        List<BookDTO> expectedBookDTOList = Collections.singletonList(bookDTO);

        List<BookDTO> bookDTOList = bookService.findAll();

        Mockito.verify(bookRepository, Mockito.times(1)).findAll();
        assertEquals(expectedBookDTOList, bookDTOList);
    }

    @Test
    @DisplayName("Тест поиска всех книг")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAllThrowException() throws SQLException, ElementHasNotFoundException {
        lenient().when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ElementHasNotFoundException.class, () -> {
            bookService.findAll();
        });
    }

    @Test
    @DisplayName("Тест сохранения новой книги")
    @Story(value = "Тестирование метода сохранения элемента")
    void save() throws ElementHasNotAddedException, SQLException {
        int bookID = 1;
        int expectedAddedID = 1;
        Book book = getFullBook(bookID);
        BookDTO bookDTO = getBookDTO(book);

        int addedId = bookService.save(bookDTO);

        Mockito.verify(bookRepository, Mockito.times(1)).save(book);
        assertEquals(expectedAddedID, addedId);
    }

    @Test
    @DisplayName("Тест сохранения новой книги")
    @Story(value = "Тестирование метода сохранения элемента")
    void saveThrowExceptionNotAddedIsExist() throws ElementHasNotAddedException, SQLException {
        int bookID = 1;
        int expectedAddedID = 1;
        Book book = getFullBook(bookID);
        BookDTO bookDTO = getBookDTO(book);

        int addedId = bookService.save(bookDTO);

        Mockito.verify(bookRepository, Mockito.times(1)).save(book);
        assertEquals(expectedAddedID, addedId);
    }

    @Test
    @DisplayName("Тест сохранения новой книги")
    @Story(value = "Тестирование метода сохранения элемента")
    void saveThrowExceptionNotAdded() throws ElementHasNotAddedException, SQLException {
        int bookID = 1;
        int expectedAddedID = 1;
        Book book = getFullBook(bookID);
        BookDTO bookDTO = getBookDTO(book);

        int addedId = bookService.save(bookDTO);

        Mockito.verify(bookRepository, Mockito.times(1)).save(book);
        assertEquals(expectedAddedID, addedId);
    }

    @Test
    @DisplayName("Тест обновления книги")
    @Story(value = "Тестирование метода обновления элемента")
    void update() throws ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        int bookID = 1;
        int expectedUpdatedID = 1;
        Book book = getFullBook(bookID, "book1U");
        BookDTO bookDTO = getBookDTO(book);

        int updatedId = bookService.update(bookDTO);

        Mockito.verify(bookRepository, Mockito.times(1)).update(book);
        assertEquals(expectedUpdatedID, updatedId);
    }

    @Test
    @DisplayName("Тест обновления книги")
    @Story(value = "Тестирование метода обновления элемента")
    void updateThrowExceptionNotFound() throws ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        int bookID = 100;
        Book bookForUpdateNotFound = getBookWithoutAuthors(bookID, "NotFound", getFullGenre(1));
        BookDTO bookDTO = getBookDTO(bookForUpdateNotFound);


        assertThrows(ElementHasNotFoundException.class, () -> {
            bookService.update(bookDTO);
        });
    }

    @Test
    @DisplayName("Тест обновления книги")
    @Story(value = "Тестирование метода обновления элемента")
    void updateThrowExceptionNotUpdated() throws ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        int bookID = 0;
        Book bookForUpdateNotUpdated = getBookWithoutAuthors(bookID, "NotUpdated", getFullGenre(1));
        BookDTO bookDTO = getBookDTO(bookForUpdateNotUpdated);

        assertThrows(ElementHasNotUpdatedException.class, () -> {
            bookService.update(bookDTO);
        });
    }

    @Test
    @DisplayName("Тест удаления книги по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteById() throws SQLException, ElementHasNotFoundException {
        int bookID = 1;
        int expectedDeletedID = 1;

        int deletedId = bookService.deleteById(bookID);

        Mockito.verify(bookRepository, Mockito.times(1)).deleteById(bookID);
        assertEquals(expectedDeletedID, deletedId);
    }

    @Test
    @DisplayName("Тест удаления книги по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteByIdThrowException() throws SQLException, ElementHasNotFoundException {
        int bookID = 100;

        assertThrows(ElementHasNotFoundException.class, () -> {
            bookService.deleteById(bookID);
        });
    }
}