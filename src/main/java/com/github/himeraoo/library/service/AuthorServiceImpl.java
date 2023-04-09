package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.AuthorDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.repository.AuthorRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public AuthorDTO findById(int authorId) throws SQLException, ElementHasNotFoundException {
        Optional<Author> optionalAuthor = authorRepository.findById(authorId);
        if (optionalAuthor.isPresent()) {
            Author dbAuthor = optionalAuthor.get();
            return new AuthorDTO(dbAuthor.getId(), dbAuthor.getName(), dbAuthor.getSurname(), dbAuthor.getBookList());
        } else {
            throw new ElementHasNotFoundException("Элемент с id  = " + authorId + " не найден.");
        }
    }

    @Override
    public List<AuthorDTO> findAll() throws SQLException, ElementHasNotFoundException {
        List<Author> authorList = authorRepository.findAll();
        if (authorList.isEmpty()) {
            throw new ElementHasNotFoundException("Элементы не найдены.");
        } else {
            List<AuthorDTO> authorDTOList = new ArrayList<>();
            for (Author a : authorList) {
                AuthorDTO authorDTO = new AuthorDTO(a.getId(), a.getName(), a.getSurname(), a.getBookList());
                authorDTOList.add(authorDTO);
            }
            return authorDTOList;
        }
    }

    @Override
    public int save(AuthorDTO authorDTO) throws SQLException, ElementHasNotAddedException {
        Author author = new Author();
        author.setId(authorDTO.getId());
        author.setName(authorDTO.getName());
        author.setSurname(authorDTO.getSurname());
        author.setBookList(authorDTO.getBookList());
        int add = authorRepository.save(author);
        if (add == 0) {
            throw new ElementHasNotAddedException("Элемент не был добавлен.");
        }

        if (add == -1) {
            throw new ElementHasNotAddedException("Элемент не был добавлен, так как уже существуют записи с такими полями.");
        }

        return add;
    }

    @Override
    public int update(AuthorDTO authorDTO) throws SQLException, ElementHasNotFoundException, ElementHasNotUpdatedException {
        Author author = new Author();
        author.setId(authorDTO.getId());
        author.setName(authorDTO.getName());
        author.setSurname(authorDTO.getSurname());
        author.setBookList(authorDTO.getBookList());
        int upd = authorRepository.update(author);
        if (upd == 0) {
            throw new ElementHasNotFoundException("Элемент с id  = " + authorDTO.getId() + " не найден.");
        }

        if (upd == -1) {
            throw new ElementHasNotUpdatedException("Элемент с id  = " + authorDTO.getId() + " не может быть обновлен, так как с такими данными уже есть другие записи.");
        }

        return upd;
    }

    @Override
    public int deleteById(int authorId) throws SQLException, ElementHasNotFoundException {
        int del = authorRepository.deleteById(authorId);
        if (del == 0) {
            throw new ElementHasNotFoundException("Элемент с id  = " + authorId + " не найден.");
        }
        return del;
    }
}
