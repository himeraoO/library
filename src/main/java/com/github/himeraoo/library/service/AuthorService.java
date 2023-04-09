package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.AuthorDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;

import java.sql.SQLException;
import java.util.List;

public interface AuthorService {

    AuthorDTO findById(int authorId) throws SQLException, ElementHasNotFoundException;

    List<AuthorDTO> findAll() throws SQLException, ElementHasNotFoundException;

    int save(AuthorDTO authorDTO) throws SQLException, ElementHasNotAddedException;

    int update(AuthorDTO authorDTO) throws SQLException, ElementHasNotFoundException, ElementHasNotUpdatedException;

    int deleteById(int authorId) throws SQLException, ElementHasNotFoundException;
}
