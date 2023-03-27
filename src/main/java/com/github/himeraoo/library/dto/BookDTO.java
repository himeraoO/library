package com.github.himeraoo.library.dto;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Genre;

import java.util.ArrayList;
import java.util.List;

public class BookDTO {

    private int id;
    private String title;
    private Genre genre;
    private List<Author> authorList = new ArrayList<>();

    public BookDTO() {
    }

    public BookDTO(int id, String title, Genre genre, List<Author> authorList) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.authorList = authorList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public List<Author> getAuthorList() {
        return authorList;
    }

    public void setAuthorList(List<Author> authorList) {
        this.authorList = authorList;
    }
}
