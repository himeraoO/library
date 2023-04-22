package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotDeletedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
import com.github.himeraoo.library.models.Genre;
import com.github.himeraoo.library.dao.GenreDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreServiceImpl implements GenreService {

    private final GenreDAO genreDAO;

    public GenreServiceImpl(GenreDAO genreDAO) {
        this.genreDAO = genreDAO;
    }

    @Override
    public GenreDTO findById(int genreId) throws SQLException, ElementHasNotFoundException {
        Optional<Genre> optionalGenre = genreDAO.findById(genreId);
        if (optionalGenre.isPresent()) {
            Genre dbGenre = optionalGenre.get();
            return new GenreDTO(dbGenre.getId(), dbGenre.getName());
        } else {
            throw new ElementHasNotFoundException("Элемент с id  = " + genreId + " не найден.");
        }
    }

    @Override
    public List<GenreDTO> findAll() throws SQLException, ElementHasNotFoundException {
        List<Genre> genreList = genreDAO.findAll();
        if (genreList.isEmpty()) {
            throw new ElementHasNotFoundException("Элементы не найдены.");
        } else {
            List<GenreDTO> genreDTOList = new ArrayList<>();
            for (Genre g : genreList) {
                GenreDTO genreDTO = new GenreDTO(g.getId(), g.getName());
                genreDTOList.add(genreDTO);
            }
            return genreDTOList;
        }
    }

    @Override
    public int save(GenreDTO genreDTO) throws SQLException, ElementHasNotAddedException {
        Genre genre = new Genre();
        genre.setId(genreDTO.getId());
        genre.setName(genreDTO.getName());
        int add = genreDAO.save(genre);
        if (add == 0) {
            throw new ElementHasNotAddedException("Элемент не был добавлен.");
        }

        if (add == -1) {
            throw new ElementHasNotAddedException("Элемент не был добавлен, так как уже существуют записи с такими полями.");
        }

        return add;
    }

    @Override
    public int update(GenreDTO genreDTO) throws SQLException, ElementHasNotFoundException, ElementHasNotUpdatedException {
        Genre genre = new Genre();
        genre.setId(genreDTO.getId());
        genre.setName(genreDTO.getName());
        int upd = genreDAO.update(genre);
        if (upd == 0) {
            throw new ElementHasNotFoundException("Элемент с id  = " + genreDTO.getId() + " не найден.");
        }

        if (upd == -1) {
            throw new ElementHasNotUpdatedException("Элемент с id  = " + genreDTO.getId() + " не может быть обновлен, так как с такими данными уже есть другие записи.");
        }

        return upd;
    }

    @Override
    public int deleteById(int genreId) throws SQLException, ElementHasNotFoundException, ElementHasNotDeletedException {
        int del = genreDAO.deleteById(genreId);
        if (del == 0) {
            throw new ElementHasNotFoundException("Элемент с id  = " + genreId + " не найден.");
        }

        if (del == -1) {
            throw new ElementHasNotDeletedException("Элемент с id  = " + genreId + " не может быть удалён, так как на него ссылаются другие записи.");
        }
        return del;
    }
}
