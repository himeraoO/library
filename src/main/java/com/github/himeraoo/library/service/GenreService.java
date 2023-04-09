package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotDeletedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;

import java.sql.SQLException;
import java.util.List;

public interface GenreService {

    GenreDTO findById(int genreId) throws SQLException, ElementHasNotFoundException;

    List<GenreDTO> findAll() throws SQLException, ElementHasNotFoundException;

    int save(GenreDTO genreDTO) throws SQLException, ElementHasNotAddedException;

    int update(GenreDTO genreDTO) throws SQLException, ElementHasNotFoundException, ElementHasNotUpdatedException;

    int deleteById(int genreId) throws SQLException, ElementHasNotFoundException, ElementHasNotDeletedException;
}
