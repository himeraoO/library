package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.AuthorDAO;
import com.github.himeraoo.library.dao.BookDAO;
import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class AuthorRepositoryImpl implements AuthorRepository {
    private final SessionManager sessionManager;
    private final AuthorDAO authorDAO;
    private final GenreDAO genreDAO;
    private final BookDAO bookDAO;


    public AuthorRepositoryImpl(SessionManager sessionManager, AuthorDAO authorDAO, GenreDAO genreDAO, BookDAO bookDAO) {
        this.sessionManager = sessionManager;
        this.authorDAO = authorDAO;
        this.genreDAO = genreDAO;
        this.bookDAO = bookDAO;
    }

    @Override
    public Optional<Author> findById(int authorId) throws SQLException {
        sessionManager.beginSession();

        Optional<Author> optionalAuthor;
        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalAuthor = authorDAO.findAuthorById(authorId, connection);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return optionalAuthor;
    }

    @Override
    public List<Author> findAll() throws SQLException {
        sessionManager.beginSession();

        List<Author> authorList;
        try (Connection connection = sessionManager.getCurrentSession()) {
            authorList = authorDAO.findAllAuthor(connection);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return authorList;
    }

    @Override
    public int save(Author author) throws SQLException {
        sessionManager.beginSession();
        int authorId = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            //проверяем наличие авторов с такими именами и фамилиями
            int authorCount = authorDAO.countAuthorByNameAndSurname(author.getName(), author.getSurname(), connection);
            //если нет, начинаем добавление
            if (authorCount == 0) {
                //сохранение автора
                authorId = authorDAO.saveAuthor(author, connection);
                //сохранение списка книг автора и добавление связей
                //получаем список книг автора
                List<Book> bookList = author.getBookList();
                //если список авторов у книги не пустой
                if (!bookList.isEmpty()) {
                    //получаем список книг из всей БД
                    List<Book> listBooksFromDB = bookDAO.findAllBook(connection);
                    //Общие книги между списком автора и тех что в БД. Для них надо добавить связи.
                    List<Book> commonElements = bookList
                            .stream()
                            .filter(listBooksFromDB::contains)
                            .collect(Collectors.toList());
                    //Новые книги, которых нет в БД. Их нужно сохранить и добавить связи.
                    List<Book> toAdd = bookList
                            .stream()
                            .filter(book -> !commonElements.contains(book))
                            .collect(Collectors.toList());
                    //Получаем список жанров из БД
                    List<Genre> genreList = genreDAO.findAllGenre(connection);
                    //Добавление новых книг в БД и добавление связей их с автором.
                    for (Book b : toAdd) {
                        //Получаем жанр из книги
                        Genre genre = b.getGenre();
                        //Проверяем наличие в БД
                        if (!genreList.contains(genre)) {
                            //если нет добавляем жанр и обновляем в книге
                            int addedGenre = genreDAO.saveGenre(genre, connection);
                            genre.setId(addedGenre);
                            b.setGenre(genre);
                        } else {
                            //если есть, получаем из списка БД и обновляем в книге
                            Genre genreDB = genreList.get(genreList.indexOf(genre));
                            b.setGenre(genreDB);
                        }
                        //Сохраняем в книгу
                        int bookId = bookDAO.saveBook(b, connection);
                        //добавляем связь с автором
                        authorDAO.addRelationAuthorBook(authorId, bookId, connection);
                    }
                    //Добавление связей между книгой и авторами, которые уже есть в БД
                    for (Book b : commonElements) {
                        int bookId = listBooksFromDB.get(listBooksFromDB.indexOf(b)).getId();
                        bookDAO.addRelationAuthorBook(authorId, bookId, connection);
                    }
                }
            } else {
                authorId = -1;
            }

            sessionManager.commitSession();
            sessionManager.finishTransaction();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return authorId;
    }

    @Override
    public int update(Author author) throws SQLException {
        int rowsUpdated = 0;
        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            //ищем автора в БД которого надо обновить
            Optional<Author> optionalAuthor = authorDAO.findAuthorById(author.getId(), connection);
            //Если автор есть, то с ним работаем, если ее нет - то не найден.
            if (optionalAuthor.isPresent()) {
                //Получаем автора из БД
                Author authorFromBD = optionalAuthor.get();
                //если name и surname одинаковые проверяем остальные поля
                if (author.getName().equals(authorFromBD.getName()) && author.getSurname().equals(authorFromBD.getSurname())) {
                    //сравнение списков книг
                    List<Book> bookList = author.getBookList();
                    List<Book> bookListFromDB = authorFromBD.getBookList();
                    List<Book> differenceBookListFromDB = bookListFromDB
                            .stream()
                            .filter(i -> !bookList.contains(i))
                            .collect(Collectors.toList());
                    List<Book> differenceBookList = bookList
                            .stream()
                            .filter(i -> !bookListFromDB.contains(i))
                            .collect(Collectors.toList());
                    //если списки книг не одинаковые, то проводим изменения
                    if ((!differenceBookListFromDB.isEmpty()) || (!differenceBookList.isEmpty()) || (bookList.size() != bookListFromDB.size())) {
                        updateRelationsOrSaveNewBooksWithGenre(author, connection);
                        rowsUpdated = 1;
                    }
                } else {
                    //если поля не одинаковые, проверяем конфликты возможных изменений
                    int authorCount = authorDAO.countAuthorByNameAndSurname(author.getName(), author.getSurname(), connection);
                    if (authorCount == 0) {
                        //если изменение возможно, сохраняем автора
                        rowsUpdated = authorDAO.updatedAuthor(author, connection);
                        //обновление связей с книгами и при необходимости создание новых с жанрами
                        updateRelationsOrSaveNewBooksWithGenre(author, connection);
                    } else {
                        //нельзя обновить, так как с такими name и surname уже существуют записи
                        rowsUpdated = -1;
                    }
                }
            }

            sessionManager.commitSession();
            sessionManager.finishTransaction();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return rowsUpdated;
    }

    private void updateRelationsOrSaveNewBooksWithGenre(Author author, Connection connection) throws SQLException {
        //получаем список книг автора
        List<Book> authorBookList = author.getBookList();
        //список книг связанных с автором
        List<Book> authorBookListFromBD = authorDAO.getBookListFromBDByAuthorId(author.getId(), connection);
        //если список книг у автора не пустой
        if (!authorBookList.isEmpty()) {
            //общие книги между переданным списком и тех что в БД. Им связи не меняем.
            List<Book> commonAuthorElements = authorBookList
                    .stream()
                    .filter(authorBookListFromBD::contains)
                    .collect(Collectors.toList());
            //новые книги которые надо проверить на наличие в БД, при необходимости добавить в БД и добавить связи с автором.
            List<Book> toAdd = authorBookList
                    .stream()
                    .filter(book -> !commonAuthorElements.contains(book))
                    .collect(Collectors.toList());
            //книги с которыми надо удалить связи
            List<Book> forRemoveRelation = authorBookListFromBD
                    .stream()
                    .filter(book -> !commonAuthorElements.contains(book))
                    .collect(Collectors.toList());
            //Проверка наличия книг с которыми добавляются связи вновь
            //получаем список книг из всей БД
            List<Book> listBooksFromDB = bookDAO.findAllBook(connection);
            //Общие книги между списком добавления автору и тех что в БД. Для них надо добавить связи.
            List<Book> commonElements = toAdd
                    .stream()
                    .filter(listBooksFromDB::contains)
                    .collect(Collectors.toList());
            //Новые книги, которых нет в БД. Их нужно сохранить и добавить связи.
            List<Book> newToAddInBD = toAdd
                    .stream()
                    .filter(book -> !commonElements.contains(book))
                    .collect(Collectors.toList());
            //Получение списка жанров из БД
            List<Genre> genreList = genreDAO.findAllGenre(connection);
            //Добавление новых книг в БД и добавление связей их с автором.
            for (Book b : newToAddInBD) {
                //Получаем жанр из книги
                Genre genre = b.getGenre();
                //Проверяем наличие в БД
                if (!genreList.contains(genre)) {
                    //если нет добавляем жанр и обновляем в книге
                    int addedGenre = genreDAO.saveGenre(genre, connection);
                    genre.setId(addedGenre);
                    b.setGenre(genre);
                } else {
                    //если есть, получаем из списка БД и обновляем в книге
                    Genre genreDB = genreList.get(genreList.indexOf(genre));
                    b.setGenre(genreDB);
                }
                //Сохраняем в книгу
                int bookId = bookDAO.saveBook(b, connection);
                //добавляем связь с автором
                authorDAO.addRelationAuthorBook(author.getId(), bookId, connection);
            }
            //Добавление связей между книгой и авторами, которые уже есть в БД
            for (Book b : commonElements) {
                int bookId = listBooksFromDB.get(listBooksFromDB.indexOf(b)).getId();
                authorDAO.addRelationAuthorBook(bookId, author.getId(), connection);
            }
            //удаление связей
            authorDAO.removeRelationBookAuthor(author.getId(), forRemoveRelation, connection);
        } else {
            //удаление связей
            authorDAO.removeRelationBookAuthor(author.getId(), authorBookListFromBD, connection);
        }
    }

    @Override
    public int deleteById(int authorId) throws SQLException {
        sessionManager.beginSession();
        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            Optional<Author> optionalAuthor = authorDAO.findAuthorById(authorId, connection);
            if (optionalAuthor.isPresent()) {
                Author dbAuthor = optionalAuthor.get();
                authorDAO.removeRelationBookAuthor(authorId, dbAuthor.getBookList(), connection);
                rowsUpdated = authorDAO.deleteAuthor(authorId, connection);
            }

            sessionManager.commitSession();
            sessionManager.finishTransaction();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return rowsUpdated;
    }
}
