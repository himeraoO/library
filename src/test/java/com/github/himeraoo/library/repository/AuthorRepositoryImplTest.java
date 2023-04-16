package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.models.Author;
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

import static com.github.himeraoo.library.util.TestUtils.getFullAuthor;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic(value = "Тестирование слоя Repository")
@Feature(value = "Тестирование AuthorRepository")
@Execution(ExecutionMode.CONCURRENT)
class AuthorRepositoryImplTest extends BaseRepositoryTest {

    @Test
    @DisplayName("Тест поиска автора по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void findById() throws SQLException {
        int authorId = 1;
        Author expectedAuthor = getFullAuthor(authorId);

        Optional<Author> optionalAuthor = authorRepository.findById(authorId);

        Mockito.verify(authorDAO, Mockito.times(1)).findAuthorById(authorId, connection);
        assertEquals(Optional.of(expectedAuthor), optionalAuthor);
    }

    @Test
    @DisplayName("Тест поиска всех авторов")
    @Story(value = "Тестирование метода поиска всех элементов")
    void findAll() throws SQLException {
        Author oldAuthor = getFullAuthor(1);
        List<Author> expectedAuthorList = new ArrayList<>();
        expectedAuthorList.add(oldAuthor);

        List<Author> authorListFromBD = authorRepository.findAll();

        Mockito.verify(authorDAO, Mockito.times(1)).findAllAuthor(connection);
        assertEquals(expectedAuthorList, authorListFromBD);
    }

    @Test
    @DisplayName("Тест сохранения нового автора")
    @Story(value = "Тестирование метода сохранения элемента")
    void save() throws SQLException {
        int authorId = 1;
        int expectedAddedId = 1;

        Author authorForSave = getFullAuthor(authorId);
        int addedId = authorRepository.save(authorForSave);

        Mockito.verify(authorDAO, Mockito.times(1)).saveAuthor(authorForSave, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).countAuthorByNameAndSurname(authorForSave.getName(), authorForSave.getSurname(), connection);
        Mockito.verify(bookDAO, Mockito.times(1)).findAllBook(connection);
        Mockito.verify(genreDAO, Mockito.times(1)).findAllGenre(connection);
        Mockito.verify(authorDAO, Mockito.times(1)).addRelationAuthorBook(authorId, authorForSave.getBookList().get(0).getId(), connection);
        Mockito.verify(authorDAO, Mockito.times(1)).addRelationAuthorBook(authorId, authorForSave.getBookList().get(1).getId(), connection);
        assertEquals(expectedAddedId, addedId);
    }

    @Test
    @DisplayName("Тест обновления автора")
    @Story(value = "Тестирование метода обновления элемента")
    void update() throws SQLException {
        int authorId = 1;
        int rowUpdatedExpected = 1;
        Author author = getFullAuthor(authorId, "author_name1U", "author_surname1U");

        int rowUpdated = authorRepository.update(author);

        Mockito.verify(authorDAO, Mockito.times(1)).updatedAuthor(author, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).findAuthorById(authorId, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).countAuthorByNameAndSurname(author.getName(), author.getSurname(), connection);
        Mockito.verify(authorDAO, Mockito.times(1)).getBookListFromBDByAuthorId(author.getId(), connection);
        Mockito.verify(bookDAO, Mockito.times(1)).findAllBook(connection);
        Mockito.verify(genreDAO, Mockito.times(1)).findAllGenre(connection);
        assertEquals(rowUpdatedExpected, rowUpdated);
    }

    @Test
    @DisplayName("Тест удаления автора по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void deleteById() throws SQLException {
        int authorId = 1;
        Author author = getFullAuthor(authorId);
        int rowDeletedExpected = 1;

        int rowDeleted = authorRepository.deleteById(authorId);

        Mockito.verify(authorDAO, Mockito.times(1)).deleteAuthor(authorId, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).findAuthorById(authorId, connection);
        Mockito.verify(authorDAO, Mockito.times(1)).removeRelationBookAuthor(authorId, author.getBookList(), connection);
        assertEquals(rowDeletedExpected, rowDeleted);
    }
}