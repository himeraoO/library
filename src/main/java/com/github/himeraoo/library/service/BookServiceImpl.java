package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.BookDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.repository.BookRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public BookDTO findById(int bookId) throws SQLException, ElementHasNotFoundException {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isPresent()) {
            Book dbBook = optionalBook.get();
            return new BookDTO(dbBook.getId(), dbBook.getTitle(), dbBook.getGenre(), dbBook.getAuthorList());
        } else {
            throw new ElementHasNotFoundException("Элемент с id  = " + bookId + " не найден.");
        }
    }

    @Override
    public List<BookDTO> findAll() throws SQLException, ElementHasNotFoundException {
        List<Book> bookList = bookRepository.findAll();
        if (bookList.isEmpty()) {
            throw new ElementHasNotFoundException("Элементы не найдены.");
        } else {
            List<BookDTO> bookDTOList = new ArrayList<>();
            for (Book b : bookList) {
                BookDTO bookDTO = new BookDTO(b.getId(), b.getTitle(), b.getGenre(), b.getAuthorList());
                bookDTOList.add(bookDTO);
            }
            return bookDTOList;
        }
    }

    @Override
    public int save(BookDTO bookDTO) throws SQLException, ElementHasNotAddedException {
        Book book = new Book();
        book.setId(bookDTO.getId());
        book.setTitle(bookDTO.getTitle());
        book.setGenre(bookDTO.getGenre());
        book.setAuthorList(bookDTO.getAuthorList());
        int add = bookRepository.save(book);
        if (add == 0) {
            throw new ElementHasNotAddedException("Элемент не был добавлен.");
        }

        if (add == -1) {
            throw new ElementHasNotAddedException("Элемент не был добавлен, так как уже существуют записи с такими полями.");
        }

        return add;
    }

    @Override
    public int update(BookDTO bookDTO) throws SQLException, ElementHasNotFoundException, ElementHasNotUpdatedException {
        Book book = new Book();
        book.setId(bookDTO.getId());
        book.setTitle(bookDTO.getTitle());
        book.setGenre(bookDTO.getGenre());
        book.setAuthorList(bookDTO.getAuthorList());
        int upd = bookRepository.update(book);
        if (upd == 0) {
            throw new ElementHasNotFoundException("Элемент с id  = " + bookDTO.getId() + " не найден.");
        }

        if (upd == -1) {
            throw new ElementHasNotUpdatedException("Элемент с id  = " + bookDTO.getId() + " не может быть обновлен, так как с такими данными уже есть другие записи.");
        }

        return upd;
    }

    @Override
    public int deleteById(int bookId) throws SQLException, ElementHasNotFoundException {
        int del = bookRepository.deleteById(bookId);
        if (del == 0) {
            throw new ElementHasNotFoundException("Элемент с id  = " + bookId + " не найден.");
        }
        return del;
    }
}
