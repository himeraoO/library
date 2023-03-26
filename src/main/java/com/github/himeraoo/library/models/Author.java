package com.github.himeraoo.library.models;

import java.util.List;
import java.util.Objects;

public class Author {

    private int id;
    private String name;
    private String surname;
    private List<Book> bookList;

    public Author() {
    }

    public Author(int id, String name, String surname, List<Book> bookList) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.bookList = bookList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public void setBookList(List<Book> bookList) {
        this.bookList = bookList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return name.equals(author.name) && surname.equals(author.surname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Author{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", surename='").append(surname).append('\'');
        if(bookList.isEmpty()){
            sb.append(", bookList=empty");
        }else {
            sb.append(", bookList=");
            for (Book b:bookList) {
                sb.append("(Book: ").append(b.getTitle()).append(", ").append(b.getGenre()).append(")").append(", ");
            }
            sb.setLength(sb.length() - 2);
        }
        sb.append('}');

        return sb.toString();
    }
}
