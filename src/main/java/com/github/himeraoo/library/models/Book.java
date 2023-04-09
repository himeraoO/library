package com.github.himeraoo.library.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Book {
    private int id;
    private String title = "";
    private Genre genre = new Genre();
    private List<Author> authorList = new ArrayList<>();

    public Book() {
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

    public List<Author> getAuthorList() {
        return authorList;
    }

    public void setAuthorList(List<Author> authorList) {
        this.authorList = authorList;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(title, book.title) && Objects.equals(genre, book.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, genre);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("Book{");
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
