package com.github.himeraoo.library.dto;

import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDTO bookDTO = (BookDTO) o;
        return Objects.equals(title, bookDTO.title) && Objects.equals(genre, bookDTO.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, genre);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("BookDTO{");
        sb.append("id=").append(id);
        sb.append(", title='").append(title).append('\'');
        sb.append(", genre=").append(genre);
        if (authorList.isEmpty()) {
            sb.append(", authorList=empty");
        } else {
            sb.append(", authorList=");
            for (Author a : authorList) {
                sb.append("Author: ").append(a.getSurname()).append(" ").append(a.getName()).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }
        sb.append('}');

        return sb.toString();
    }
}
