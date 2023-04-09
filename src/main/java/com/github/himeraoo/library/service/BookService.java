package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.BookDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;

import java.sql.SQLException;
import java.util.List;

public interface BookService {

    BookDTO findById(int bookId) throws SQLException, ElementHasNotFoundException;

    List<BookDTO> findAll() throws SQLException, ElementHasNotFoundException;

    int save(BookDTO bookDTO) throws SQLException, ElementHasNotAddedException;

    int update(BookDTO bookDTO) throws SQLException, ElementHasNotFoundException, ElementHasNotUpdatedException;

    int deleteById(int bookId) throws SQLException, ElementHasNotFoundException;
}
