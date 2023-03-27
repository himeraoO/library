package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.BookDTO;
import com.github.himeraoo.library.exception.ElementNotAddedException;
import com.github.himeraoo.library.exception.ElementNotFoundException;

import java.sql.SQLException;
import java.util.List;

public interface BookService {

    BookDTO findById(int bookId) throws SQLException, ElementNotFoundException;

    List<BookDTO> findAll() throws SQLException;

    int save(BookDTO bookDTO) throws SQLException, ElementNotAddedException;

    int update(BookDTO bookDTO) throws SQLException, ElementNotFoundException;

    int deleteById(int bookId) throws SQLException, ElementNotFoundException;
}
