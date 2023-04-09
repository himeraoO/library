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

public class BookRepositoryImpl implements BookRepository {

    private final SessionManager sessionManager;
    private final BookDAO bookDAO;
    private final GenreDAO genreDAO;
    private final AuthorDAO authorDAO;

    public BookRepositoryImpl(SessionManager sessionManager, BookDAO bookDAO, GenreDAO genreDAO, AuthorDAO authorDAO) {
        this.sessionManager = sessionManager;
        this.bookDAO = bookDAO;
        this.genreDAO = genreDAO;
        this.authorDAO = authorDAO;
    }

    @Override
    public Optional<Book> findById(int bookId) throws SQLException {
        sessionManager.beginSession();

        Optional<Book> optionalBook;
        try (Connection connection = sessionManager.getCurrentSession()) {
            optionalBook = bookDAO.findBookById(bookId, connection);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return optionalBook;
    }

    @Override
    public List<Book> findAll() throws SQLException {
        sessionManager.beginSession();

        List<Book> bookList;
        try (Connection connection = sessionManager.getCurrentSession()) {
            bookList = bookDAO.findAllBook(connection);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return bookList;
    }

    private void updateOrSaveNewGenreForBook(Book book, Connection connection) throws SQLException {
        List<Genre> genreList = genreDAO.findAllGenre(connection);
        Genre genre = book.getGenre();
        if (!genreList.contains(genre)) {
            int addedGenre = genreDAO.saveGenre(genre, connection);
            genre.setId(addedGenre);
            book.setGenre(genre);
        } else {
            Genre genreDB = genreList.get(genreList.indexOf(genre));
            book.setGenre(genreDB);
        }
    }

    @Override
    public int save(Book book) throws SQLException {
        sessionManager.beginSession();
        int bookId = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            //проверяем наличие книг с такими названиями
            int bookCount = bookDAO.countBookByTitle(book.getTitle(), connection);
            //если нет, начинаем добавление
            if (bookCount == 0) {
                //проверка наличия жанра, при необходимости сохранение нового
                updateOrSaveNewGenreForBook(book, connection);
                //сохранение книги
                bookId = bookDAO.saveBook(book, connection);
                //сохранение списка авторов книги и добавление связей
                //получаем список авторов книги
                List<Author> authorList = book.getAuthorList();
                //если список авторов у книги не пустой
                if (!authorList.isEmpty()) {
                    //получаем список авторов из всей БД
                    List<Author> listAuthorsFromDB = authorDAO.findAllAuthor(connection);
                    //Общие авторы между списком в книге и тех что в БД. Для них надо добавить связи.
                    List<Author> commonElements = authorList
                            .stream()
                            .filter(listAuthorsFromDB::contains)
                            .collect(Collectors.toList());
                    //Новые авторы, которых нет в БД. Их нужно сохранить и добавить связи.
                    List<Author> toAdd = authorList
                            .stream()
                            .filter(author -> !commonElements.contains(author))
                            .collect(Collectors.toList());
                    //Добавление новых авторов в БД и добавление связей их с книгой.
                    for (Author a : toAdd) {
                        int added = authorDAO.saveAuthor(a, connection);
                        bookDAO.addRelationAuthorBook(added, bookId, connection);
                    }
                    //Добавление связей между книгой и авторами, которые уже есть в БД
                    for (Author a : commonElements) {
                        int authorId = listAuthorsFromDB.get(listAuthorsFromDB.indexOf(a)).getId();
                        bookDAO.addRelationAuthorBook(authorId, bookId, connection);
                    }
                }
            } else {
                bookId = -1;
            }

            sessionManager.commitSession();
            sessionManager.finishTransaction();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return bookId;
    }

    @Override
    public int update(Book book) throws SQLException {
        sessionManager.beginSession();
        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            //ищем книгу в БД которую надо обновить
            Optional<Book> optionalBook = bookDAO.findBookById(book.getId(), connection);
            //Если книга есть, то с ней работаем, если ее нет - то не найдена.
            if (optionalBook.isPresent()) {
                //Получаем книгу из БД
                Book bookFromBD = optionalBook.get();
                //если title одинаковые проверяем остальные поля
                if (book.getTitle().equals(bookFromBD.getTitle())) {
                    //если genre одинаковые - проверяем список авторов
                    if (book.getGenre().equals(bookFromBD.getGenre())) {
                        //сравнение списков авторов
                        List<Author> authorList = book.getAuthorList();
                        List<Author> authorListFromDB = bookFromBD.getAuthorList();
                        List<Author> differenceAuthorListFromDB = authorListFromDB
                                .stream()
                                .filter(i -> !authorList.contains(i))
                                .collect(Collectors.toList());
                        List<Author> differenceAuthorList = authorList
                                .stream()
                                .filter(i -> !authorListFromDB.contains(i))
                                .collect(Collectors.toList());
                        //если списки авторов не одинаковые, то проводим изменения
                        if ((!differenceAuthorListFromDB.isEmpty()) || (!differenceAuthorList.isEmpty()) || (authorList.size() != authorListFromDB.size())) {
                            updateRelationsOrSaveNewAuthors(book, connection);
                            rowsUpdated = 1;
                        }
                    } else {
                        //если genre разные обновляем и сохраняем всё
                        //если изменение возможно, проверяем genre и при необходимости сохраняем новый
                        updateOrSaveNewGenreForBook(book, connection);
                        //сохранение обновленной книги
                        rowsUpdated = bookDAO.updatedBook(book, connection);
                        //обновление связей с авторами и при необходимости создание новых
                        updateRelationsOrSaveNewAuthors(book, connection);
                    }
                } else {
                    //если поля не одинаковые, проверяем конфликты возможных изменений
                    int bookCount = bookDAO.countBookByTitle(book.getTitle(), connection);
                    if (bookCount == 0) {
                        //если изменение возможно, проверяем genre и при необходимости сохраняем новый
                        updateOrSaveNewGenreForBook(book, connection);
                        //сохранение обновленной книги
                        rowsUpdated = bookDAO.updatedBook(book, connection);
                        //обновление связей с авторами и при необходимости создание новых
                        updateRelationsOrSaveNewAuthors(book, connection);
                    } else {
                        //нельзя обновить, так как с таким title уже существуют записи
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

    private void updateRelationsOrSaveNewAuthors(Book book, Connection connection) throws SQLException {
        //получаем список авторов книги
        List<Author> bookAuthorList = book.getAuthorList();
        //список авторов связанных с книгой
        List<Author> bookAuthorListFromBD = bookDAO.getAuthorListFromBDByBookId(book.getId(), connection);
        //если список авторов у книги не пустой
        if (!bookAuthorList.isEmpty()) {
            //Общие авторы книги между переданным списком и тех что у книги в БД. Им связи не меняем.
            List<Author> commonBookElements = bookAuthorList
                    .stream()
                    .filter(bookAuthorListFromBD::contains)
                    .collect(Collectors.toList());
            //новые авторы которые надо проверить на наличие в БД, при необходимости добавить в БД и добавить связи с книгой.
            List<Author> toAdd = bookAuthorList
                    .stream()
                    .filter(author -> !commonBookElements.contains(author))
                    .collect(Collectors.toList());
            //авторы с которыми надо удалить связи
            List<Author> forRemoveRelation = bookAuthorListFromBD
                    .stream()
                    .filter(author -> !commonBookElements.contains(author))
                    .collect(Collectors.toList());
            //Проверка наличия авторов с которыми добавляются связи вновь
            //получаем список авторов из всей БД
            List<Author> listAuthorsFromDB = authorDAO.findAllAuthor(connection);
            //Общие авторы между списком добавления в книгу и тех что в БД. Для них надо добавить связи.
            List<Author> commonElements = toAdd
                    .stream()
                    .filter(listAuthorsFromDB::contains)
                    .collect(Collectors.toList());
            //Новые авторы, которых нет в БД. Их нужно сохранить и добавить связи.
            List<Author> newToAddInBD = toAdd
                    .stream()
                    .filter(author -> !commonElements.contains(author))
                    .collect(Collectors.toList());
            //Добавление новых авторов в БД и добавление связей их с книгой.
            for (Author a : newToAddInBD) {
                int added = authorDAO.saveAuthor(a, connection);
                bookDAO.addRelationAuthorBook(added, book.getId(), connection);
            }
            //Добавление связей между книгой и авторами, которые уже есть в БД
            for (Author a : commonElements) {
                int authorId = listAuthorsFromDB.get(listAuthorsFromDB.indexOf(a)).getId();
                bookDAO.addRelationAuthorBook(authorId, book.getId(), connection);
            }
            //удаление связей
            bookDAO.removeRelationAuthorBook(book.getId(), forRemoveRelation, connection);
        } else {
            //удаление связей
            bookDAO.removeRelationAuthorBook(book.getId(), bookAuthorListFromBD, connection);
        }
    }

    @Override
    public int deleteById(int bookId) throws SQLException {
        sessionManager.beginSession();
        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            Optional<Book> optionalBook = bookDAO.findBookById(bookId, connection);
            if (optionalBook.isPresent()) {
                Book dbBook = optionalBook.get();
                bookDAO.removeRelationAuthorBook(bookId, dbBook.getAuthorList(), connection);
                rowsUpdated = bookDAO.deleteBook(bookId, connection);
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
