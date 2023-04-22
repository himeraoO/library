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

import static com.github.himeraoo.library.dao.BookDAOImpl.getBooksWithAuthors;
import static com.github.himeraoo.library.dao.BookDAOImpl.getBooksWithoutAuthors;
import static com.github.himeraoo.library.dao.BookDAOImpl.saveNewBook;
import static com.github.himeraoo.library.dao.GenreDAOImpl.findAllGenre;
import static com.github.himeraoo.library.dao.GenreDAOImpl.saveNewGenre;


public class AuthorDAOImpl implements AuthorDAO {
    private final SessionManager sessionManager;

    public AuthorDAOImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    protected static List<Author> getAuthorsWithoutBooks(Connection connection) throws SQLException {
        List<Author> authorsWithoutBooks;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindAllWithoutBooks.QUERY)) {
            try (ResultSet rs = pst.executeQuery()) {
                authorsWithoutBooks = new ArrayList<>();
                while (rs.next()) {
                    Author dbAuthor = new Author();
                    dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
                    dbAuthor.setName(rs.getString("aname"));
                    dbAuthor.setSurname(rs.getString("asurname"));
                    authorsWithoutBooks.add(dbAuthor);
                }
            }
        }
        return authorsWithoutBooks;
    }

    protected static List<Author> getAuthorsWithBooks(Connection connection) throws SQLException {
        List<Author> authorsWithBooks;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindAllWithBooks.QUERY)) {
            try (ResultSet rs = pst.executeQuery()) {
                HashMap<Integer, Author> integerAuthorHashMap = new HashMap<>();
                while (rs.next()) {
                    Author dbAuthor = new Author();
                    dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
                    dbAuthor.setName(rs.getString("aname"));
                    dbAuthor.setSurname(rs.getString("asurname"));

                    Book authorBook = new Book();
                    authorBook.setId(Integer.parseInt(rs.getString("bid")));
                    authorBook.setTitle(rs.getString("btitle"));
                    authorBook.setAuthorList(new ArrayList<>());

                    Genre bookGenre = new Genre();
                    bookGenre.setId((Integer.parseInt(rs.getString("gid"))));
                    bookGenre.setName((rs.getString("gname")));

                    authorBook.setGenre(bookGenre);

                    if (integerAuthorHashMap.containsKey(dbAuthor.getId())) {
                        integerAuthorHashMap.get(dbAuthor.getId()).getBookList().add(authorBook);
                    } else {
                        List<Book> bookList = new ArrayList<>();
                        bookList.add(authorBook);
                        dbAuthor.setBookList(bookList);
                        integerAuthorHashMap.put(dbAuthor.getId(), dbAuthor);
                    }
                }
                authorsWithBooks = new ArrayList<>(integerAuthorHashMap.values());
            }
        }
        return authorsWithBooks;
    }

    protected static int saveNewAuthor(Author author, Connection connection) throws SQLException {
        int authorId = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, author.getName());
            pst.setString(2, author.getSurname());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    authorId = rs.getInt(1);
                }
            }
        }
        return authorId;
    }

    @Override
    public Optional<Author> findById(int authorId) throws SQLException {
        sessionManager.beginSession();

        Author author = null;
        try (Connection connection = sessionManager.getCurrentSession()) {
            author = getAuthorFromBD(authorId, connection);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return Optional.ofNullable(author);
    }

    @Override
    public List<Author> findAll() throws SQLException {
        sessionManager.beginSession();
        List<Author> authorList = new ArrayList<>();
        List<Author> authorsWithBooks;
        List<Author> authorsWithoutBooks;
        try (Connection connection = sessionManager.getCurrentSession()) {
            authorsWithBooks = getAuthorsWithBooks(connection);
            authorsWithoutBooks = getAuthorsWithoutBooks(connection);
            authorList.addAll(authorsWithBooks);
            authorList.addAll(authorsWithoutBooks.stream().filter(author -> !authorsWithBooks.contains(author)).collect(Collectors.toList()));
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
            int authorCount = getAuthorCount(author, connection);
            //если нет, начинаем добавление
            if (authorCount == 0) {
                //сохранение автора
                authorId = saveNewAuthor(author, connection);
                //сохранение списка книг автора и добавление связей
                //получаем список книг автора
                List<Book> bookList = author.getBookList();
                //если список авторов у книги не пустой
                checkAndSaveBooksListWithGenreFromAuthorOnSave(authorId, connection, bookList);
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
            Author authorFromBD = getAuthorFromBD(author.getId(), connection);
            //Если автор есть, то с ним работаем, если ее нет - то не найден.
            if (authorFromBD != null) {
                //если name и surname одинаковые проверяем остальные поля
                if (author.getName().equals(authorFromBD.getName()) && author.getSurname().equals(authorFromBD.getSurname())) {
                    //сравнение списков книг
                    List<Book> bookList = author.getBookList();
                    List<Book> bookListFromDB = authorFromBD.getBookList();
                    List<Book> differenceBookListFromDB = bookListFromDB.stream().filter(i -> !bookList.contains(i)).collect(Collectors.toList());
                    List<Book> differenceBookList = bookList.stream().filter(i -> !bookListFromDB.contains(i)).collect(Collectors.toList());
                    //если списки книг не одинаковые, то проводим изменения
                    if ((!differenceBookListFromDB.isEmpty()) || (!differenceBookList.isEmpty()) || (bookList.size() != bookListFromDB.size())) {
                        //получаем список книг автора
                        List<Book> authorBookList = author.getBookList();
                        //список книг связанных с автором
                        List<Book> authorBookListFromBD = getAuthorBookListFromBD(author.getId(), connection);
                        //если список книг у автора не пустой
                        if (!authorBookList.isEmpty()) {
                            //общие книги между переданным списком и тех что в БД. Им связи не меняем.
                            List<Book> commonAuthorElements = authorBookList.stream().filter(authorBookListFromBD::contains).collect(Collectors.toList());
                            //новые книги которые надо проверить на наличие в БД, при необходимости добавить в БД и добавить связи с автором.
                            List<Book> toAdd = authorBookList.stream().filter(book -> !commonAuthorElements.contains(book)).collect(Collectors.toList());
                            //книги с которыми надо удалить связи
                            List<Book> forRemoveRelation = authorBookListFromBD.stream().filter(book -> !commonAuthorElements.contains(book)).collect(Collectors.toList());
                            //Проверка наличия книг с которыми добавляются связи вновь
                            //получаем список книг из всей БД
                            List<Book> listBooksFromDB = new ArrayList<>();
                            List<Book> booksWithAuthors;
                            List<Book> booksWithoutAuthors;

                            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithAuthors.QUERY)) {
                                booksWithAuthors = getBooksWithAuthors(pst);
                            }
                            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithoutAuthors.QUERY)) {
                                booksWithoutAuthors = getBooksWithoutAuthors(pst);
                            }
                            listBooksFromDB.addAll(booksWithAuthors);
                            listBooksFromDB.addAll(booksWithoutAuthors.stream().filter(book -> !booksWithAuthors.contains(book)).collect(Collectors.toList()));
                            //Общие книги между списком добавления автору и тех что в БД. Для них надо добавить связи.
                            List<Book> commonElements = toAdd.stream().filter(listBooksFromDB::contains).collect(Collectors.toList());
                            //Новые книги, которых нет в БД. Их нужно сохранить и добавить связи.
                            List<Book> newToAddInBD = toAdd.stream().filter(book -> !commonElements.contains(book)).collect(Collectors.toList());
                            //Получение списка жанров из БД
                            List<Genre> genreList = new ArrayList<>();
                            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindAll.QUERY)) {
                                genreList = findAllGenre(pst);
                            }
                            //Добавление новых книг в БД и добавление связей их с автором.
                            for (Book book : newToAddInBD) {
                                //Получаем жанр из книги
                                Genre genre = book.getGenre();
                                //Проверяем наличие в БД
                                if (!genreList.contains(genre)) {
                                    //если нет добавляем жанр и обновляем в книге
                                    int addedGenre = saveNewGenre(genre, connection);
                                    genre.setId(addedGenre);
                                    book.setGenre(genre);
                                } else {
                                    //если есть, получаем из списка БД и обновляем в книге
                                    Genre genreDB = genreList.get(genreList.indexOf(genre));
                                    book.setGenre(genreDB);
                                }
                                //Сохраняем в книгу
                                int bookId = 0;
                                bookId = saveNewBook(book, connection);
                                //добавляем связь с автором
                                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                                    pst.setInt(1, author.getId());
                                    pst.setInt(2, bookId);
                                    pst.executeUpdate();
                                }
                            }
                            //Добавление связей между книгой и авторами, которые уже есть в БД
                            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                                for (Book book : commonElements) {
                                    int bookId = listBooksFromDB.get(listBooksFromDB.indexOf(book)).getId();
                                    pst.setInt(1, author.getId());
                                    pst.setInt(2, bookId);
                                    pst.executeUpdate();
                                }
                            }
                            //удаление связей
                            removeRelationAuthorBooks(author.getId(), forRemoveRelation, connection);
                        } else {
                            //удаление связей
                            removeRelationAuthorBooks(author.getId(), authorBookListFromBD, connection);
                        }
                        rowsUpdated = 1;
                    }
                } else {
                    //если поля не одинаковые, проверяем конфликты возможных изменений
                    int authorCount = getAuthorCount(author, connection);
                    if (authorCount == 0) {
                        //если изменение возможно, сохраняем автора
                        rowsUpdated = updateAuthor(author, connection);
                        //обновление связей с книгами и при необходимости создание новых с жанрами
                        //получаем список книг автора
                        List<Book> authorBookList = author.getBookList();
                        //список книг связанных с автором
                        List<Book> authorBookListFromBD = getAuthorBookListFromBD(author.getId(), connection);
                        //если список книг у автора не пустой
                        if (!authorBookList.isEmpty()) {
                            //общие книги между переданным списком и тех что в БД. Им связи не меняем.
                            List<Book> commonAuthorElements = authorBookList.stream().filter(authorBookListFromBD::contains).collect(Collectors.toList());
                            //новые книги которые надо проверить на наличие в БД, при необходимости добавить в БД и добавить связи с автором.
                            List<Book> toAdd = authorBookList.stream().filter(book -> !commonAuthorElements.contains(book)).collect(Collectors.toList());
                            //книги с которыми надо удалить связи
                            List<Book> forRemoveRelation = authorBookListFromBD.stream().filter(book -> !commonAuthorElements.contains(book)).collect(Collectors.toList());
                            //Проверка наличия книг с которыми добавляются связи вновь
                            //получаем список книг из всей БД
                            List<Book> listBooksFromDB = new ArrayList<>();
                            List<Book> booksWithAuthors;
                            List<Book> booksWithoutAuthors;
                            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithAuthors.QUERY)) {
                                booksWithAuthors = getBooksWithAuthors(pst);
                            }
                            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithoutAuthors.QUERY)) {
                                booksWithoutAuthors = getBooksWithoutAuthors(pst);
                            }
                            listBooksFromDB.addAll(booksWithAuthors);
                            listBooksFromDB.addAll(booksWithoutAuthors.stream().filter(book -> !booksWithAuthors.contains(book)).collect(Collectors.toList()));
                            //Общие книги между списком добавления автору и тех что в БД. Для них надо добавить связи.
                            List<Book> commonElements = toAdd.stream().filter(listBooksFromDB::contains).collect(Collectors.toList());
                            //Новые книги, которых нет в БД. Их нужно сохранить и добавить связи.
                            List<Book> newToAddInBD = toAdd.stream().filter(book -> !commonElements.contains(book)).collect(Collectors.toList());
                            //Получение списка жанров из БД
                            List<Genre> genreList;
                            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindAll.QUERY)) {
                                genreList = findAllGenre(pst);
                            }
                            //Добавление новых книг в БД и добавление связей их с автором.
                            for (Book book : newToAddInBD) {
                                //Получаем жанр из книги
                                Genre genre = book.getGenre();
                                //Проверяем наличие в БД
                                if (!genreList.contains(genre)) {
                                    //если нет добавляем жанр и обновляем в книге
                                    int addedGenre = saveNewGenre(genre, connection);
                                    genre.setId(addedGenre);
                                    book.setGenre(genre);
                                } else {
                                    //если есть, получаем из списка БД и обновляем в книге
                                    Genre genreDB = genreList.get(genreList.indexOf(genre));
                                    book.setGenre(genreDB);
                                }
                                //Сохраняем в книгу
                                int bookId = 0;
                                bookId = saveNewBook(book, connection);
                                //добавляем связь с автором
                                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                                    pst.setInt(1, author.getId());
                                    pst.setInt(2, bookId);
                                    pst.executeUpdate();
                                }
                            }
                            //Добавление связей между книгой и авторами, которые уже есть в БД
                            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                                for (Book book : commonElements) {
                                    int bookId = listBooksFromDB.get(listBooksFromDB.indexOf(book)).getId();
                                    pst.setInt(1, author.getId());
                                    pst.setInt(2, bookId);
                                    pst.executeUpdate();
                                }
                            }
                            //удаление связей
                            removeRelationAuthorBooks(author.getId(), forRemoveRelation, connection);
                        } else {
                            //удаление связей
                            removeRelationAuthorBooks(author.getId(), authorBookListFromBD, connection);
                        }
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

    @Override
    public int deleteById(int authorId) throws SQLException {
        sessionManager.beginSession();
        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            Author authorFromBD = getAuthorFromBD(authorId, connection);
            if (authorFromBD != null) {
                removeRelationAuthorBooks(authorId, authorFromBD.getBookList(), connection);
                rowsUpdated = deleteAuthor(authorId, connection);
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

    private Author getAuthorFromBD(int authorId, Connection connection) throws SQLException {
        Author author = null;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindByIdWithBooks.QUERY)) {
            pst.setInt(1, authorId);
            try (ResultSet rs = pst.executeQuery()) {
                Author dbAuthor = getAuthorWithBooks(rs);
                if (dbAuthor.getId() != 0) {
                    author = dbAuthor;
                }
            }
        }

        if (author == null) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindByIdWithoutBooks.QUERY)) {
                pst.setInt(1, authorId);
                try (ResultSet rs = pst.executeQuery()) {
                    Author dbAuthor = getAuthorWithoutBooks(rs);
                    if (dbAuthor.getId() != 0) {
                        author = dbAuthor;
                    }
                }
            }
        }
        return author;
    }

    private Author getAuthorWithBooks(ResultSet rs) throws SQLException {
        List<Book> books = new ArrayList<>();
        Author dbAuthor = new Author();
        while (rs.next()) {
            dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
            dbAuthor.setName(rs.getString("aname"));
            dbAuthor.setSurname(rs.getString("asurname"));

            Book book = new Book();
            book.setId(Integer.parseInt(rs.getString("bid")));
            book.setTitle(rs.getString("btitle"));
            book.setAuthorList(new ArrayList<>());

            Genre genre = new Genre();
            genre.setId((Integer.parseInt(rs.getString("gid"))));
            genre.setName((rs.getString("gname")));

            book.setGenre(genre);
            books.add(book);
        }
        dbAuthor.setBookList(books);
        return dbAuthor;
    }

    private Author getAuthorWithoutBooks(ResultSet rs) throws SQLException {
        Author dbAuthor = new Author();
        while (rs.next()) {
            dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
            dbAuthor.setName(rs.getString("aname"));
            dbAuthor.setSurname(rs.getString("asurname"));
        }
        return dbAuthor;
    }

    private void checkAndSaveBooksListWithGenreFromAuthorOnSave(int authorId, Connection connection, List<Book> bookList) throws SQLException {
        if (!bookList.isEmpty()) {
            //получаем список книг из всей БД
            List<Book> listBooksFromDB = new ArrayList<>();
            List<Book> booksWithAuthors;
            List<Book> booksWithoutAuthors;
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithAuthors.QUERY)) {
                booksWithAuthors = getBooksWithAuthors(pst);
            }
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithoutAuthors.QUERY)) {
                booksWithoutAuthors = getBooksWithoutAuthors(pst);
            }
            listBooksFromDB.addAll(booksWithAuthors);
            listBooksFromDB.addAll(booksWithoutAuthors.stream().filter(book -> !booksWithAuthors.contains(book)).collect(Collectors.toList()));
            //Общие книги между списком автора и тех что в БД. Для них надо добавить связи.
            List<Book> commonElements = bookList.stream().filter(listBooksFromDB::contains).collect(Collectors.toList());
            //Новые книги, которых нет в БД. Их нужно сохранить и добавить связи.
            List<Book> toAdd = bookList.stream().filter(book -> !commonElements.contains(book)).collect(Collectors.toList());
            //Получаем список жанров из БД
            List<Genre> genreList;
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindAll.QUERY)) {
                genreList = findAllGenre(pst);
            }
            //Добавление новых книг в БД и добавление связей их с автором.
            for (Book book : toAdd) {
                //Получаем жанр из книги
                Genre genre = book.getGenre();
                //Проверяем наличие в БД
                if (!genreList.contains(genre)) {
                    //если нет добавляем жанр и обновляем в книге
                    int addedGenre = saveNewGenre(genre, connection);
                    genre.setId(addedGenre);
                    book.setGenre(genre);
                } else {
                    //если есть, получаем из списка БД и обновляем в книге
                    Genre genreDB = genreList.get(genreList.indexOf(genre));
                    book.setGenre(genreDB);
                }
                //Сохраняем в книгу
                int bookId = 0;
                bookId = saveNewBook(book, connection);
                //добавляем связь с автором
                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                    pst.setInt(1, authorId);
                    pst.setInt(2, bookId);
                    pst.executeUpdate();
                }
            }
            //Добавление связей между книгой и авторами, которые уже есть в БД
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AddRelationAuthorsBooks.QUERY)) {
                for (Book b : commonElements) {
                    int bookId = listBooksFromDB.get(listBooksFromDB.indexOf(b)).getId();
                    pst.setInt(1, authorId);
                    pst.setInt(2, bookId);
                    pst.executeUpdate();
                }
            }
        }
    }

    private int getAuthorCount(Author author, Connection connection) throws SQLException {
        int authorCount = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_CountAuthorByNameAndSurname.QUERY)) {
            pst.setString(1, author.getName());
            pst.setString(2, author.getSurname());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    authorCount = rs.getInt("Count(*)");
                }
            }
        }
        return authorCount;
    }

    private int updateAuthor(Author author, Connection connection) throws SQLException {
        int rowsUpdated = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorUpdateById.QUERY)) {
            pst.setString(1, author.getName());
            pst.setString(2, author.getSurname());
            pst.setInt(3, author.getId());

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }

    private void removeRelationAuthorBooks(int authorId, List<Book> bookList, Connection connection) throws SQLException {
        if (!bookList.isEmpty()) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {
                for (Book book : bookList) {
                    pst.setInt(1, authorId);
                    pst.setInt(2, book.getId());
                    pst.executeUpdate();
                }
            }
        }
    }

    private List<Book> getAuthorBookListFromBD(int authorId, Connection connection) throws SQLException {
        List<Book> authorBookListFromBD = new ArrayList<>();

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AllBookFindByAuthorId.QUERY)) {
            pst.setInt(1, authorId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(Integer.parseInt(rs.getString("bid")));
                    book.setTitle(rs.getString("btitle"));
                    book.setAuthorList(new ArrayList<>());

                    Genre genre = new Genre();
                    genre.setId(Integer.parseInt(rs.getString("gid")));
                    genre.setName(rs.getString("gname"));

                    book.setGenre(genre);

                    authorBookListFromBD.add(book);
                }
            }
        }
        return authorBookListFromBD;
    }

    private int deleteAuthor(int authorId, Connection connection) throws SQLException {
        int rowsUpdated = 0;
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorDeleteById.QUERY)) {
            pst.setInt(1, authorId);

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }
}
