package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.AuthorDTO;
import com.github.himeraoo.library.exception.ElementNotAddedException;
import com.github.himeraoo.library.exception.ElementNotFoundException;
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
    public AuthorDTO findById(int authorId) throws SQLException, ElementNotFoundException{
        Optional<Author> optionalAuthor = authorRepository.findById(authorId);
        if (optionalAuthor.isPresent()){
            Author dbAuthor = optionalAuthor.get();
            return new AuthorDTO(dbAuthor.getId(), dbAuthor.getName(), dbAuthor.getSurname(), dbAuthor.getBookList());
        } else {
            throw new ElementNotFoundException("Элемент с id  = " + authorId + " не найден.");
        }
    }

    @Override
    public List<AuthorDTO> findAll() throws SQLException, ElementNotFoundException {
        List<Author> authorList = authorRepository.findAll();
        if (authorList.isEmpty()){
            throw new ElementNotFoundException("Элементы не найдены.");
        } else {
            List<AuthorDTO> authorDTOList = new ArrayList<>();
            for (Author a:authorList) {
                AuthorDTO authorDTO = new AuthorDTO(a.getId(), a.getName(), a.getSurname(), a.getBookList());
                authorDTOList.add(authorDTO);
            }
            return authorDTOList;
        }
    }

    @Override
    public int save(AuthorDTO authorDTO) throws SQLException, ElementNotAddedException {
        Author author = new Author();
        author.setId(authorDTO.getId());
        author.setName(authorDTO.getName());
        author.setSurname(authorDTO.getSurname());
        author.setBookList(authorDTO.getBookList());
        int add = authorRepository.save(author);
        if (add == 0){
            throw new ElementNotAddedException("Элемент не был добавлен.");
        }
        return add;
    }

    @Override
    public int update(AuthorDTO authorDTO) throws SQLException, ElementNotFoundException {
        Author author = new Author();
        author.setId(authorDTO.getId());
        author.setName(authorDTO.getName());
        author.setSurname(authorDTO.getSurname());
        author.setBookList(authorDTO.getBookList());
        int upd = authorRepository.update(author);
        if (upd == 0){
            throw new ElementNotFoundException("Элемент с id  = " + authorDTO.getId() + " не найден.");
        }
        return upd;
    }

    @Override
    public int deleteById(int authorId) throws SQLException, ElementNotFoundException {
        int del = authorRepository.deleteById(authorId);
        if(del == 0){
            throw new ElementNotFoundException("Элемент с id  = " + authorId + " не найден.");
        }
        return del;
    }
}
