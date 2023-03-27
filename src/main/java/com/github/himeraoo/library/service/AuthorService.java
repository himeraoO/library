package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.AuthorDTO;
import com.github.himeraoo.library.exception.ElementNotAddedException;
import com.github.himeraoo.library.exception.ElementNotFoundException;

import java.sql.SQLException;
import java.util.List;

public interface AuthorService {

    AuthorDTO findById(int authorId) throws SQLException, ElementNotFoundException;

    List<AuthorDTO> findAll() throws SQLException;

    int save(AuthorDTO authorDTO) throws SQLException, ElementNotAddedException;

    int update(AuthorDTO authorDTO) throws SQLException, ElementNotFoundException;

    int deleteById(int authorId) throws SQLException, ElementNotFoundException;
}
