package com.github.himeraoo.library.service;

import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.exception.ElementNotAddedException;
import com.github.himeraoo.library.exception.ElementNotFoundException;
import com.github.himeraoo.library.models.Genre;
import com.github.himeraoo.library.repository.GenreRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    public GenreServiceImpl(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Override
    public GenreDTO findById(int genreId) throws SQLException, ElementNotFoundException {
        GenreDTO genreDTO;
        Optional<Genre> optionalGenre = genreRepository.findById(genreId);
        if (optionalGenre.isPresent()){
            Genre dbGenre = optionalGenre.get();
            genreDTO = new GenreDTO(dbGenre.getId(), dbGenre.getName());
        }
        else {
            throw new ElementNotFoundException("Элемент с id  = " + genreId + " не найден.");
        }
        return genreDTO;
    }

    @Override
    public List<GenreDTO> findAll() throws SQLException {
        List<Genre> genreList = genreRepository.findAll();
        List<GenreDTO> genreDTOList = new ArrayList<>();
        for (Genre g:genreList) {
            GenreDTO genreDTO = new GenreDTO(g.getId(), g.getName());
            genreDTOList.add(genreDTO);
        }
        return genreDTOList;
    }

    @Override
    public int save(GenreDTO genreDTO) throws SQLException, ElementNotAddedException {
        Genre genre = new Genre();
        genre.setId(genreDTO.getId());
        genre.setName(genreDTO.getName());
        int add = genreRepository.save(genre);
        if (add == 0){
            throw new ElementNotAddedException("Элемент не был добавлен.");
        }
        return add;
    }

    @Override
    public int update(GenreDTO genreDTO) throws SQLException, ElementNotFoundException {
        Genre genre = new Genre();
        genre.setId(genreDTO.getId());
        genre.setName(genreDTO.getName());
        int upd = genreRepository.update(genre);
        if (upd == 0){
            throw new ElementNotFoundException("Элемент с id  = " + genreDTO.getId() + " не найден.");
        }
        return upd;
    }

    @Override
    public int deleteById(int genreId) throws SQLException, ElementNotFoundException {
        int del = genreRepository.deleteById(genreId);
        if(del == 0){
            throw new ElementNotFoundException("Элемент с id  = " + genreId + " не найден.");
        }
        return del;
    }
}
