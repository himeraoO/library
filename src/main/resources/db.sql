CREATE TABLE IF NOT EXISTS author (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    PRIMARY KEY (id))
    ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS genre (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id))
    ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS book (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (genre_id)
        REFERENCES genre (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS authors_books (
    author_id INT NOT NULL,
    book_id INT NOT NULL,
    PRIMARY KEY (author_id, book_id),
    FOREIGN KEY (author_id)
        REFERENCES author (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    FOREIGN KEY (book_id)
        REFERENCES book (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION)
    ENGINE = InnoDB;

insert into author (name, surname) values ("author_name1", "author_surname1");
insert into author (name, surname) values ("author_name2", "author_surname2");
insert into author (name, surname) values ("author_name3", "author_surname3");
insert into author (name, surname) values ("author_name4", "author_surname4");
insert into author (name, surname) values ("author_name5", "author_surname5");
insert into author (name, surname) values ("author_name6", "author_surname6");

insert into genre (name) values ("genre1");
insert into genre (name) values ("genre2");
insert into genre (name) values ("genre3");
insert into genre (name) values ("genre4");
insert into genre (name) values ("genre5");
insert into genre (name) values ("genre6");

insert into book (title, genre_id) values ("book1", 1);
insert into book (title, genre_id) values ("book2", 2);
insert into book (title, genre_id) values ("book3", 3);
insert into book (title, genre_id) values ("book4", 4);
insert into book (title, genre_id) values ("book5", 5);
insert into book (title, genre_id) values ("book6", 5);

insert into authors_books values (1, 1);
insert into authors_books values (1, 5);
insert into authors_books values (2, 2);
insert into authors_books values (2, 5);
insert into authors_books values (3, 1);
insert into authors_books values (3, 2);
insert into authors_books values (3, 3);
insert into authors_books values (4, 4);
insert into authors_books values (4, 5);
insert into authors_books values (5, 2);
insert into authors_books values (5, 5);
insert into authors_books values (5, 1);
