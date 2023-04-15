package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.models.Genre;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenreRepositoryImplTest extends BaseRepositoryTest {

    @Test
    void findById() throws SQLException {
        int genreId = 1;

        Genre expectedGenre = getGenre(1, "genre1");

        Optional<Genre> optionalGenre = genreRepository.findById(genreId);

        Mockito.verify(genreDAO, Mockito.times(1)).findGenreById(genreId, connection);
        assertEquals(Optional.of(expectedGenre), optionalGenre);
    }

    @Test
    void findAll() throws SQLException {
        Genre oldGenre = getGenre(1, "genre1");
        List<Genre> expectedGenreList = new ArrayList<>();
        expectedGenreList.add(oldGenre);

        List<Genre> genreListFromBD = genreRepository.findAll();

        Mockito.verify(genreDAO, Mockito.times(1)).findAllGenre(connection);
        assertEquals(expectedGenreList, genreListFromBD);
    }

    @Test
    void save() throws SQLException {
        int genreId = 1;
        int expectedAddedId = 1;

        Genre genreForSave = getGenre(genreId, "genre1");
        int addedId = genreRepository.save(genreForSave);

        Mockito.verify(genreDAO, Mockito.times(1)).saveGenre(genreForSave, connection);
        Mockito.verify(genreDAO, Mockito.times(1)).countGenreByName(genreForSave.getName(), connection);
        assertEquals(expectedAddedId, addedId);
    }

    @Test
    void update() throws SQLException {
        int genreId = 1;
        int rowUpdatedExpected = 1;

        Genre genreForUpdate = getGenre(genreId, "genre1U");
        int rowUpdated = genreRepository.update(genreForUpdate);

        Mockito.verify(genreDAO, Mockito.times(1)).updatedGenre(genreForUpdate, connection);
        Mockito.verify(genreDAO, Mockito.times(1)).countGenreByName(genreForUpdate.getName(), connection);
        assertEquals(rowUpdatedExpected, rowUpdated);
    }

    @Test
    void deleteById() throws SQLException {
        int genreId = 1;
        int rowDeletedExpected = 1;

        int rowDeleted = genreRepository.deleteById(1);

        Mockito.verify(genreDAO, Mockito.times(1)).deleteGenre(genreId, connection);
        Mockito.verify(bookDAO, Mockito.times(1)).countBookByGenreId(genreId, connection);
        assertEquals(rowDeletedExpected, rowDeleted);
    }
}