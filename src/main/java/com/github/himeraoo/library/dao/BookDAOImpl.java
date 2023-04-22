package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.himeraoo.library.dao.AuthorDAOImpl.getAuthorsWithBooks;
import static com.github.himeraoo.library.dao.AuthorDAOImpl.getAuthorsWithoutBooks;
import static com.github.himeraoo.library.dao.AuthorDAOImpl.saveNewAuthor;
import static com.github.himeraoo.library.dao.GenreDAOImpl.findAllGenre;
import static com.github.himeraoo.library.dao.GenreDAOImpl.saveNewGenre;

public class BookDAOImpl implements BookDAO {

    private final SessionManager sessionManager;

    public BookDAOImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    protected static int saveNewBook(Book book, Connection connection) throws SQLException {
        int bookId = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, book.getTitle());
            pst.setInt(2, book.getGenre().getId());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    bookId = rs.getInt(1);
                }
            }
        }
        return bookId;
    }

    protected static List<Book> getBooksWithoutAuthors(PreparedStatement pst) throws SQLException {
        List<Book> booksWithoutAuthors = new ArrayList<>();
        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Book dbBook = new Book();
                dbBook.setId(Integer.parseInt(rs.getString("bid")));
                dbBook.setTitle(rs.getString("btitle"));

                Genre genre = new Genre();
                genre.setId((Integer.parseInt(rs.getString("gid"))));
                genre.setName((rs.getString("gname")));

                dbBook.setGenre(genre);
                booksWithoutAuthors.add(dbBook);
            }
        }
        return booksWithoutAuthors;
    }

    protected static List<Book> getBooksWithAuthors(PreparedStatement pst) throws SQLException {
        HashMap<Integer, Book> integerBookHashMap = new HashMap<>();

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Book dbBook = new Book();
                dbBook.setId(Integer.parseInt(rs.getString("bid")));
                dbBook.setTitle(rs.getString("btitle"));

                Genre genre = new Genre();
                genre.setId((Integer.parseInt(rs.getString("gid"))));
                genre.setName((rs.getString("gname")));

                dbBook.setGenre(genre);

                Author author = new Author();
                author.setId(Integer.parseInt(rs.getString("aid")));
                author.setName(rs.getString("aname"));
                author.setSurname(rs.getString("asurname"));

                if (integerBookHashMap.containsKey(dbBook.getId())) {
                    integerBookHashMap.get(dbBook.getId()).getAuthorList().add(author);
                } else {
                    dbBook.getAuthorList().add(author);
                    integerBookHashMap.put(dbBook.getId(), dbBook);
                }
            }
        }
        return new ArrayList<>(integerBookHashMap.values());
    }

    @Override
    public Optional<Book> findById(int bookId) throws SQLException {
        sessionManager.beginSession();

        Book book = null;
        try (Connection connection = sessionManager.getCurrentSession()) {
            book = getBookFromDB(bookId, connection);

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() throws SQLException {
        sessionManager.beginSession();
        List<Book> bookList = new ArrayList<>();
        List<Book> booksWithAuthors;
        List<Book> booksWithoutAuthors;
        try (Connection connection = sessionManager.getCurrentSession()) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithAuthors.QUERY)) {
                booksWithAuthors = getBooksWithAuthors(pst);
            }
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithoutAuthors.QUERY)) {
                booksWithoutAuthors = getBooksWithoutAuthors(pst);
            }
            bookList.addAll(booksWithAuthors);
            bookList.addAll(booksWithoutAuthors.stream().filter(book -> !booksWithAuthors.contains(book)).collect(Collectors.toList()));
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return bookList;
    }

    @Override
    public int save(Book book) throws SQLException {
        sessionManager.beginSession();
        int bookId = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            //проверяем наличие книг с такими названиями
            int bookCount = getBookCount(book, connection);
            //если нет, начинаем добавление
            if (bookCount == 0) {
                //проверка наличия жанра, при необходимости сохранение нового
                checkAndSaveGenre(book, connection);

                //сохранение книги
                bookId = saveNewBook(book, connection);
                //сохранение списка авторов книги и добавление связей
                //получаем список авторов книги
                List<Author> authorList = book.getAuthorList();
                //если список авторов у книги не пустой
                checkAndSaveAuthorsListFromBookOnSave(bookId, authorList, connection);
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
            Book bookFromBD = getBookFromDB(book.getId(), connection);
            //Если книга есть, то с ней работаем, если ее нет - то не найдена.
            if (bookFromBD != null) {
                //Получаем книгу из БД
                //если title одинаковые проверяем остальные поля
                if (book.getTitle().equals(bookFromBD.getTitle())) {
                    //если genre одинаковые - проверяем список авторов
                    if (book.getGenre().equals(bookFromBD.getGenre())) {
                        //сравнение списков авторов
                        List<Author> authorList = book.getAuthorList();
                        List<Author> authorListFromDB = bookFromBD.getAuthorList();
                        List<Author> differenceAuthorListFromDB = authorListFromDB.stream().filter(i -> !authorList.contains(i)).collect(Collectors.toList());
                        List<Author> differenceAuthorList = authorList.stream().filter(i -> !authorListFromDB.contains(i)).collect(Collectors.toList());
                        //если списки авторов не одинаковые, то проводим изменения
                        if ((!differenceAuthorListFromDB.isEmpty()) || (!differenceAuthorList.isEmpty()) || (authorList.size() != authorListFromDB.size())) {
                            checkAndSaveAuthorsListFromBookOnUpdate(book, connection);
                            rowsUpdated = 1;
                        }
                    } else {
                        //если genre разные обновляем и сохраняем всё
                        //если изменение возможно, проверяем genre и при необходимости сохраняем новый
                        checkAndSaveGenre(book, connection);
                        //сохранение обновленной книги
                        rowsUpdated = updateBook(book, connection);
                        //обновление связей с авторами и при необходимости создание новых
                        checkAndSaveAuthorsListFromBookOnUpdate(book, connection);
                    }
                } else {
                    //если поля не одинаковые, проверяем конфликты возможных изменений
                    int bookCount = getBookCount(book, connection);
                    if (bookCount == 0) {
                        //если изменение возможно, проверяем genre и при необходимости сохраняем новый
                        checkAndSaveGenre(book, connection);
                        //сохранение обновленной книги
                        rowsUpdated = updateBook(book, connection);
                        //обновление связей с авторами и при необходимости создание новых
                        checkAndSaveAuthorsListFromBookOnUpdate(book, connection);
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

    @Override
    public int deleteById(int bookId) throws SQLException {
        sessionManager.beginSession();
        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            Book bookFromDB = getBookFromDB(bookId, connection);
            if (bookFromDB != null) {
                removeRelationAuthorsBook(bookFromDB.getAuthorList(), bookId, connection);
                rowsUpdated = deleteBook(bookId, connection);
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

    private int deleteBook(int bookId, Connection connection) throws SQLException {
        int rowsUpdated = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookDeleteById.QUERY)) {
            pst.setInt(1, bookId);

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }

    private Book getBookFromDB(int bookId, Connection connection) throws SQLException {
        Book book = null;

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindByIdWithAuthors.QUERY)) {
            pst.setInt(1, bookId);
            try (ResultSet rs = pst.executeQuery()) {
                Book dbBook = getBookWithAuthor(rs);
                if (dbBook.getId() != 0) {
                    book = dbBook;
                }
            }
        }

        if (book == null) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindByIdWithoutAuthors.QUERY)) {
                pst.setInt(1, bookId);
                try (ResultSet rs = pst.executeQuery()) {
                    Book dbBook = getBookWithoutAuthor(rs);
                    if (dbBook.getId() != 0) {
                        book = dbBook;
                    }
                }
            }
        }
        return book;
    }

    private Book getBookWithoutAuthor(ResultSet rs) throws SQLException {
        Book dbBook = new Book();
        Genre genre = new Genre();
        while (rs.next()) {
            dbBook.setId(Integer.parseInt(rs.getString("bid")));
            dbBook.setTitle(rs.getString("btitle"));
            genre.setId((Integer.parseInt(rs.getString("gid"))));
            genre.setName((rs.getString("gname")));
        }
        dbBook.setGenre(genre);
        return dbBook;
    }

    private Book getBookWithAuthor(ResultSet rs) throws SQLException {
        Book dbBook = new Book();
        Genre genre = new Genre();
        List<Author> authors = new ArrayList<>();
        while (rs.next()) {
            dbBook.setId(Integer.parseInt(rs.getString("bid")));
            dbBook.setTitle(rs.getString("btitle"));

            genre.setId((Integer.parseInt(rs.getString("gid"))));
            genre.setName((rs.getString("gname")));

            Author author = new Author();
            author.setId(Integer.parseInt(rs.getString("aid")));
            author.setName(rs.getString("aname"));
            author.setSurname(rs.getString("asurname"));
            author.setBookList(new ArrayList<>());
            authors.add(author);
        }
        dbBook.setGenre(genre);
        dbBook.setAuthorList(authors);
        return dbBook;
    }

    private void checkAndSaveAuthorsListFromBookOnSave(int bookId, List<Author> authorList, Connection connection) throws SQLException {
        if (!authorList.isEmpty()) {
            //получаем список авторов из всей БД
            List<Author> listAuthorsFromDB = new ArrayList<>();
            List<Author> authorsWithBooks = getAuthorsWithBooks(connection);

            List<Author> authorsWithoutBooks = getAuthorsWithoutBooks(connection);

            listAuthorsFromDB.addAll(authorsWithBooks);
            listAuthorsFromDB.addAll(authorsWithoutBooks.stream().filter(author -> !authorsWithBooks.contains(author)).collect(Collectors.toList()));
            //Общие авторы между списком в книге и тех что в БД. Для них надо добавить связи.
            List<Author> commonElements = authorList.stream().filter(listAuthorsFromDB::contains).collect(Collectors.toList());
            //Новые авторы, которых нет в БД. Их нужно сохранить и добавить связи.
            List<Author> toAdd = authorList.stream().filter(author -> !commonElements.contains(author)).collect(Collectors.toList());
            //Добавление новых авторов в БД и добавление связей их с книгой.
            for (Author author : toAdd) {
                int added = saveNewAuthor(author, connection);

                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                    pst.setInt(1, added);
                    pst.setInt(2, bookId);
                    pst.executeUpdate();
                }
            }
            //Добавление связей между книгой и авторами, которые уже есть в БД
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                for (Author a : commonElements) {
                    int authorId = listAuthorsFromDB.get(listAuthorsFromDB.indexOf(a)).getId();
                    pst.setInt(1, authorId);
                    pst.setInt(2, bookId);
                    pst.executeUpdate();
                }
            }
        }
    }

    private void checkAndSaveGenre(Book book, Connection connection) throws SQLException {
        List<Genre> genreList = new ArrayList<>();
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindAll.QUERY)) {
            genreList = findAllGenre(pst);
        }
        Genre genre = book.getGenre();
        if (!genreList.contains(genre)) {
            int addedGenre = saveNewGenre(genre, connection);
            genre.setId(addedGenre);
            book.setGenre(genre);
        } else {
            Genre genreDB = genreList.get(genreList.indexOf(genre));
            book.setGenre(genreDB);
        }
    }

    private List<Author> getBookAuthorListFromBD(int bookId, Connection connection) throws SQLException {
        List<Author> bookAuthorListFromBD = new ArrayList<>();
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AllAuthorFindByBookId.QUERY)) {
            pst.setInt(1, bookId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Author author = new Author();
                    author.setId(Integer.parseInt(rs.getString("aid")));
                    author.setName(rs.getString("aname"));
                    author.setSurname(rs.getString("asurname"));
                    author.setBookList(new ArrayList<>());
                    bookAuthorListFromBD.add(author);
                }
            }
        }
        return bookAuthorListFromBD;
    }

    private int updateBook(Book book, Connection connection) throws SQLException {
        int rowsUpdated = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookUpdateById.QUERY)) {

            pst.setString(1, book.getTitle());
            pst.setInt(2, book.getGenre().getId());
            pst.setInt(3, book.getId());

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }

    private void checkAndSaveAuthorsListFromBookOnUpdate(Book book, Connection connection) throws SQLException {
        //получаем список авторов книги
        List<Author> bookAuthorList = book.getAuthorList();
        //список авторов связанных с книгой
        List<Author> bookAuthorListFromBD = getBookAuthorListFromBD(book.getId(), connection);
        //если список авторов у книги не пустой
        if (!bookAuthorList.isEmpty()) {
            //Общие авторы книги между переданным списком и тех что у книги в БД. Им связи не меняем.
            List<Author> commonBookElements = bookAuthorList.stream().filter(bookAuthorListFromBD::contains).collect(Collectors.toList());
            //новые авторы которые надо проверить на наличие в БД, при необходимости добавить в БД и добавить связи с книгой.
            List<Author> toAdd = bookAuthorList.stream().filter(author -> !commonBookElements.contains(author)).collect(Collectors.toList());
            //авторы с которыми надо удалить связи
            List<Author> forRemoveRelation = bookAuthorListFromBD.stream().filter(author -> !commonBookElements.contains(author)).collect(Collectors.toList());
            //Проверка наличия авторов с которыми добавляются связи вновь
            //получаем список авторов из всей БД
            List<Author> listAuthorsFromDB = new ArrayList<>();

            List<Author> authorsWithBooks = getAuthorsWithBooks(connection);

            List<Author> authorsWithoutBooks = getAuthorsWithoutBooks(connection);

            listAuthorsFromDB.addAll(authorsWithBooks);
            listAuthorsFromDB.addAll(authorsWithoutBooks.stream().filter(author -> !authorsWithBooks.contains(author)).collect(Collectors.toList()));
            //Общие авторы между списком добавления в книгу и тех что в БД. Для них надо добавить связи.
            List<Author> commonElements = toAdd.stream().filter(listAuthorsFromDB::contains).collect(Collectors.toList());
            //Новые авторы, которых нет в БД. Их нужно сохранить и добавить связи.
            List<Author> newToAddInBD = toAdd.stream().filter(author -> !commonElements.contains(author)).collect(Collectors.toList());
            //Добавление новых авторов в БД и добавление связей их с книгой.
            List<Author> newAuthorInDB = new ArrayList<>();
            for (Author author : newToAddInBD) {
                int added = saveNewAuthor(author, connection);
                author.setId(added);
                newAuthorInDB.add(author);
            }
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                for (Author author : newAuthorInDB) {
                    pst.setInt(1, author.getId());
                    pst.setInt(2, book.getId());
                    pst.executeUpdate();
                }
            }
            //Добавление связей между книгой и авторами, которые уже есть в БД
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                for (Author author : commonElements) {
                    int authorId = listAuthorsFromDB.get(listAuthorsFromDB.indexOf(author)).getId();
                    pst.setInt(1, authorId);
                    pst.setInt(2, book.getId());
                    pst.executeUpdate();
                }
            }
            //удаление связей
            removeRelationAuthorsBook(forRemoveRelation, book.getId(), connection);
        } else {
            //удаление связей
            removeRelationAuthorsBook(bookAuthorListFromBD, book.getId(), connection);
        }
    }

    private void removeRelationAuthorsBook(List<Author> authorList, int book, Connection connection) throws SQLException {
        if (!authorList.isEmpty()) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {
                for (Author author : authorList) {
                    pst.setInt(1, author.getId());
                    pst.setInt(2, book);
                    pst.executeUpdate();
                }
            }
        }
    }

    private int getBookCount(Book book, Connection connection) throws SQLException {
        int bookCount = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_CountBookByTitle.QUERY)) {
            pst.setString(1, book.getTitle());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    bookCount = rs.getInt("Count(*)");
                }
            }
        }
        return bookCount;
    }
}
