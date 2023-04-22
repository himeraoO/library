package com.github.himeraoo.library.dao;

public enum SQLQuery {

    QUERY_AuthorFindByIdWithBooks("select a.id as aid, a.name as aname, a.surname as asurname, b.id as bid, b.title as btitle, g.id as gid, g.name as gname from author as a inner join authors_books as ab on a.id=ab.author_id inner join book as b on b.id=ab.book_id inner join genre as g on b.genre_id = g.id where a.id = ?"),

    QUERY_AuthorFindByIdWithoutBooks("select a.id as aid, a.name as aname, a.surname as asurname from author as a where a.id = ?"),

    QUERY_AuthorFindAllWithBooks("select a.id as aid, a.name as aname, a.surname as asurname, b.id as bid, b.title as btitle, g.id as gid, g.name as gname from author as a inner join authors_books as ab on a.id=ab.author_id inner join book as b on b.id=ab.book_id inner join genre as g on b.genre_id = g.id"),

    QUERY_AuthorFindAllWithoutBooks("select a.id as aid, a.name as aname, a.surname as asurname from author as a"),

    QUERY_AuthorUpdateById("update author set name = ?, surname = ? where id = ?"),

    QUERY_AuthorDeleteById("delete from author where id = ?"),

    QUERY_AuthorSave("insert into author (name, surname) values (?, ?)"),

    QUERY_AllAuthorFindByBookId("select a.id as aid, a.name as aname, a.surname as asurname from author as a inner join authors_books as ab on a.id=ab.author_id inner join book as b on b.id=ab.book_id where b.id = ?"),

    QUERY_AllBookFindByAuthorId("select b.id as bid, b.title as btitle, g.id as gid, g.name as gname from book as b inner join authors_books as ab on b.id=ab.book_id inner join genre as g on b.genre_id = g.id inner join author as a on a.id=ab.author_id where a.id = ?"),

    QUERY_CountBookByGenreId("select Count(*) from book as b inner join genre as g on b.genre_id = g.id where g.id = ?"),

    QUERY_RemoveRelationAuthorsBooks("delete from authors_books where author_id = ? and book_id = ?"),

    QUERY_AddRelationAuthorsBooks("insert into authors_books (author_id, book_id) values (?, ?)"),

    QUERY_BookFindByIdWithAuthors("select b.id as bid, b.title as btitle, g.id as gid, g.name as gname, a.id as aid, a.name as aname, a.surname as asurname from book as b inner join genre as g on b.genre_id = g.id inner join authors_books as ab on b.id=ab.book_id inner join author as a on a.id=ab.author_id where b.id = ?"),

    QUERY_BookFindByIdWithoutAuthors("select b.id as bid, b.title as btitle, g.id as gid, g.name as gname from book as b inner join genre as g on b.genre_id = g.id where b.id = ?"),

    QUERY_BookFindAll_WithAuthors("select b.id as bid, b.title as btitle, g.id as gid, g.name as gname, a.id as aid, a.name as aname, a.surname as asurname from book as b inner join genre as g on b.genre_id = g.id inner join authors_books as ab on b.id=ab.book_id inner join author as a on a.id=ab.author_id"),

    QUERY_BookFindAll_WithoutAuthors("select b.id as bid, b.title as btitle, g.id as gid, g.name as gname from book as b inner join genre as g on b.genre_id = g.id"),

    QUERY_BookUpdateById("update book set title = ?, genre_id = ? where id = ?"),

    QUERY_BookDeleteById("delete from book where id = ?"),

    QUERY_BookSave("insert into book (title, genre_id) VALUES (?, ?)"),

    QUERY_FindBookById("select id, title, genre_id from book where id = ?"),

    QUERY_GenreFindById("select id, name from genre where id = ?"),

    QUERY_GenreSave("insert into genre (name) VALUES (?)"),

    QUERY_GenreFindAll("select id, name from genre"),

    QUERY_GenreUpdateById("update genre set name = ? where id = ?"),

    QUERY_GenreDeleteById("delete from genre where id = ?"),

    QUERY_CountBookByTitle("select Count(*) from book where title = ?"),

    QUERY_CountGenreByName("select Count(*) from genre where name = ?"),

    QUERY_CountAuthorByNameAndSurname("select Count(*) from author where name = ? AND surname = ?");

    public final String QUERY;

    SQLQuery(String QUERY) {
        this.QUERY = QUERY;
    }
}
