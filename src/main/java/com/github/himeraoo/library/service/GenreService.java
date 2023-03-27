package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.exception.ElementNotAddedException;
import com.github.himeraoo.library.exception.ElementNotFoundException;

import java.sql.SQLException;
import java.util.List;

public interface GenreService {

    GenreDTO findById(int genreId) throws SQLException, ElementNotFoundException;

    List<GenreDTO> findAll() throws SQLException;

    int save(GenreDTO genreDTO) throws SQLException, ElementNotAddedException;

    int update(GenreDTO genreDTO) throws SQLException, ElementNotFoundException;

    int deleteById(int genreId) throws SQLException, ElementNotFoundException;
}
