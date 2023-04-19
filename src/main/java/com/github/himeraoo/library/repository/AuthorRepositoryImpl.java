package com.github.himeraoo.library.repository;

import com.github.himeraoo.library.dao.SQLQuery;
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


public class AuthorRepositoryImpl implements AuthorRepository {
    private final SessionManager sessionManager;

    public AuthorRepositoryImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Optional<Author> findById(int authorId) throws SQLException {
        sessionManager.beginSession();

        Author author = null;
        try (Connection connection = sessionManager.getCurrentSession()) {
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindByIdWithBooks.QUERY)) {
                pst.setInt(1, authorId);
                try (ResultSet rs = pst.executeQuery()) {
                    Author dbAuthor = parseAuthorWithBooks(rs);
                    if (dbAuthor.getId() != 0) {
                        author = dbAuthor;
                    }
                }
            }

            if (author == null) {
                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindByIdWithoutBooks.QUERY)) {
                    pst.setInt(1, authorId);
                    try (ResultSet rs = pst.executeQuery()) {
                        Author dbAuthor = parseAuthorWithoutBooks(rs);
                        if (dbAuthor.getId() != 0) {
                            author = dbAuthor;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return Optional.ofNullable(author);
    }

    private Author parseAuthorWithBooks(ResultSet rs) throws SQLException {
        Author dbAuthor = new Author();
        List<Book> books = new ArrayList<>();
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

    private Author parseAuthorWithoutBooks(ResultSet rs) throws SQLException {
        Author dbAuthor = new Author();
        while (rs.next()) {
            dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
            dbAuthor.setName(rs.getString("aname"));
            dbAuthor.setSurname(rs.getString("asurname"));
        }
        return dbAuthor;
    }

    @Override
    public List<Author> findAll() throws SQLException {
        sessionManager.beginSession();

        List<Author> authorList = new ArrayList<>();
        try (Connection connection = sessionManager.getCurrentSession()) {
            List<Author> authorsWithBooks = getAuthorsWithBooks(connection);
            List<Author> authorsWithoutBooks = getAuthorsWithoutBooks(connection);
            authorList.addAll(authorsWithBooks);
            authorList.addAll(authorsWithoutBooks.stream().filter(author -> !authorsWithBooks.contains(author)).collect(Collectors.toList()));
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sessionManager.rollbackSession();
            throw ex;
        }
        return authorList;
    }

    private List<Author> getAuthorsWithBooks(Connection connection) throws SQLException {
        HashMap<Integer, Author> integerAuthorHashMap = new HashMap<>();

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindAllWithBooks.QUERY)) {
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Author dbAuthor = new Author();
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

                    if (integerAuthorHashMap.containsKey(dbAuthor.getId())) {
                        integerAuthorHashMap.get(dbAuthor.getId()).getBookList().add(book);
                    } else {
                        List<Book> bookList = new ArrayList<>();
                        bookList.add(book);
                        dbAuthor.setBookList(bookList);
                        integerAuthorHashMap.put(dbAuthor.getId(), dbAuthor);
                    }
                }
            }
        }
        return new ArrayList<>(integerAuthorHashMap.values());
    }

    private List<Author> getAuthorsWithoutBooks(Connection connection) throws SQLException {
        List<Author> authorListWithoutBook = new ArrayList<>();
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindAllWithoutBooks.QUERY)) {
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Author dbAuthor = parseAuthorFromResultSetWithoutBooks(rs);
                    authorListWithoutBook.add(dbAuthor);
                }
            }
        }
        return authorListWithoutBook;
    }

    private Author parseAuthorFromResultSetWithoutBooks(ResultSet rs) throws SQLException {
        Author dbAuthor = new Author();
        dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
        dbAuthor.setName(rs.getString("aname"));
        dbAuthor.setSurname(rs.getString("asurname"));
        return dbAuthor;
    }

    @Override
    public int save(Author author) throws SQLException {
        sessionManager.beginSession();
        int authorId = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            //проверяем наличие авторов с такими именами и фамилиями
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
            //если нет, начинаем добавление
            if (authorCount == 0) {
                //сохранение автора
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
                //сохранение списка книг автора и добавление связей
                //получаем список книг автора
                List<Book> bookList = author.getBookList();
                //если список авторов у книги не пустой
                if (!bookList.isEmpty()) {
                    //получаем список книг из всей БД
                    List<Book> listBooksFromDB = new ArrayList<>();
                    List<Book> booksWithAuthors = getBooksWithAuthors(connection);
                    List<Book> booksWithoutAuthors = getBooksWithoutAuthors(connection);
                    listBooksFromDB.addAll(booksWithAuthors);
                    listBooksFromDB.addAll(booksWithoutAuthors.stream().filter(book -> !booksWithAuthors.contains(book)).collect(Collectors.toList()));
                    //Общие книги между списком автора и тех что в БД. Для них надо добавить связи.
                    List<Book> commonElements = bookList.stream().filter(listBooksFromDB::contains).collect(Collectors.toList());
                    //Новые книги, которых нет в БД. Их нужно сохранить и добавить связи.
                    List<Book> toAdd = bookList.stream().filter(book -> !commonElements.contains(book)).collect(Collectors.toList());
                    //Получаем список жанров из БД
                    List<Genre> genreList = new ArrayList<>();
                    try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindAll.QUERY)) {
                        try (ResultSet rs = pst.executeQuery()) {
                            while (rs.next()) {
                                Genre dbGenre = new Genre();
                                dbGenre.setId((Integer.parseInt(rs.getString("id"))));
                                dbGenre.setName((rs.getString("name")));
                                genreList.add(dbGenre);
                            }
                        }
                    }
                    //Добавление новых книг в БД и добавление связей их с автором.
                    for (Book book : toAdd) {
                        //Получаем жанр из книги
                        Genre genre = book.getGenre();
                        //Проверяем наличие в БД
                        if (!genreList.contains(genre)) {
                            //если нет добавляем жанр и обновляем в книге
                            int addedGenre = 0;
                            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
                                pst.setString(1, genre.getName());
                                pst.executeUpdate();

                                try (ResultSet rs = pst.getGeneratedKeys()) {
                                    if (rs.next()) {
                                        addedGenre = rs.getInt(1);
                                    }
                                }
                            }
                            genre.setId(addedGenre);
                            book.setGenre(genre);
                        } else {
                            //если есть, получаем из списка БД и обновляем в книге
                            Genre genreDB = genreList.get(genreList.indexOf(genre));
                            book.setGenre(genreDB);
                        }
                        //Сохраняем в книгу
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

    private List<Book> getBooksWithAuthors(Connection connection) throws SQLException {
        HashMap<Integer, Book> integerBookHashMap = new HashMap<>();

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithAuthors.QUERY)) {
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
        }
        return new ArrayList<>(integerBookHashMap.values());
    }

    private List<Book> getBooksWithoutAuthors(Connection connection) throws SQLException {
        List<Book> bookListWithoutAuthor = new ArrayList<>();
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_BookFindAll_WithoutAuthors.QUERY)) {
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Book dbBook = parseBookFromResultSetWithoutAuthors(rs);
                    bookListWithoutAuthor.add(dbBook);
                }
            }
        }
        return bookListWithoutAuthor;
    }

    private Book parseBookFromResultSetWithoutAuthors(ResultSet rs) throws SQLException {
        Book dbBook = new Book();
        dbBook.setId(Integer.parseInt(rs.getString("bid")));
        dbBook.setTitle(rs.getString("btitle"));

        Genre genre = new Genre();
        genre.setId((Integer.parseInt(rs.getString("gid"))));
        genre.setName((rs.getString("gname")));

        dbBook.setGenre(genre);
        return dbBook;
    }

    @Override
    public int update(Author author) throws SQLException {
        int rowsUpdated = 0;
        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            //ищем автора в БД которого надо обновить
            Author authorFromBD = null;
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindByIdWithBooks.QUERY)) {
                pst.setInt(1, author.getId());
                try (ResultSet rs = pst.executeQuery()) {
                    Author dbAuthor = parseAuthorWithBooks(rs);
                    if (dbAuthor.getId() != 0) {
                        authorFromBD = dbAuthor;
                    }
                }
            }

            if (authorFromBD == null) {
                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindByIdWithoutBooks.QUERY)) {
                    pst.setInt(1, author.getId());
                    try (ResultSet rs = pst.executeQuery()) {
                        Author dbAuthor = parseAuthorWithoutBooks(rs);
                        if (dbAuthor.getId() != 0) {
                            authorFromBD = dbAuthor;
                        }
                    }
                }
            }
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
                        updateRelationsOrSaveNewBooksWithGenre(author, connection);
                        rowsUpdated = 1;
                    }
                } else {
                    //если поля не одинаковые, проверяем конфликты возможных изменений
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
                    if (authorCount == 0) {
                        //если изменение возможно, сохраняем автора
                        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorUpdateById.QUERY)) {
                            pst.setString(1, author.getName());
                            pst.setString(2, author.getSurname());
                            pst.setInt(3, author.getId());

                            rowsUpdated = pst.executeUpdate();
                        }
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
        List<Book> authorBookListFromBD = new ArrayList<>();

        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AllBookFindByAuthorId.QUERY)) {
            pst.setInt(1, author.getId());

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
            List<Book> booksWithAuthors = getBooksWithAuthors(connection);
            List<Book> booksWithoutAuthors = getBooksWithoutAuthors(connection);
            listBooksFromDB.addAll(booksWithAuthors);
            listBooksFromDB.addAll(booksWithoutAuthors.stream().filter(book -> !booksWithAuthors.contains(book)).collect(Collectors.toList()));
            //Общие книги между списком добавления автору и тех что в БД. Для них надо добавить связи.
            List<Book> commonElements = toAdd.stream().filter(listBooksFromDB::contains).collect(Collectors.toList());
            //Новые книги, которых нет в БД. Их нужно сохранить и добавить связи.
            List<Book> newToAddInBD = toAdd.stream().filter(book -> !commonElements.contains(book)).collect(Collectors.toList());
            //Получение списка жанров из БД
            List<Genre> genreList = new ArrayList<>();
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindAll.QUERY)) {
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Genre dbGenre = new Genre();
                        dbGenre.setId((Integer.parseInt(rs.getString("id"))));
                        dbGenre.setName((rs.getString("name")));
                        genreList.add(dbGenre);
                    }
                }
            }
            //Добавление новых книг в БД и добавление связей их с автором.
            for (Book book : newToAddInBD) {
                //Получаем жанр из книги
                Genre genre = book.getGenre();
                //Проверяем наличие в БД
                if (!genreList.contains(genre)) {
                    //если нет добавляем жанр и обновляем в книге
                    int addedGenre = 0;
                    try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
                        pst.setString(1, genre.getName());
                        pst.executeUpdate();

                        try (ResultSet rs = pst.getGeneratedKeys()) {
                            if (rs.next()) {
                                addedGenre = rs.getInt(1);
                            }
                        }
                    }
                    genre.setId(addedGenre);
                    book.setGenre(genre);
                } else {
                    //если есть, получаем из списка БД и обновляем в книге
                    Genre genreDB = genreList.get(genreList.indexOf(genre));
                    book.setGenre(genreDB);
                }
                //Сохраняем в книгу
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
            if (!forRemoveRelation.isEmpty()) {
                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {
                    for (Book book : forRemoveRelation) {
                        pst.setInt(1, author.getId());
                        pst.setInt(2, book.getId());
                        pst.executeUpdate();
                    }
                }
            }
        } else {
            //удаление связей
            if (!authorBookListFromBD.isEmpty()) {
                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {
                    for (Book book : authorBookListFromBD) {
                        pst.setInt(1, author.getId());
                        pst.setInt(2, book.getId());
                        pst.executeUpdate();
                    }
                }
            }
        }
    }

    @Override
    public int deleteById(int authorId) throws SQLException {
        sessionManager.beginSession();
        int rowsUpdated = 0;

        try (Connection connection = sessionManager.getCurrentSession()) {
            sessionManager.startTransaction();

            Author authorFromBD = null;
            try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindByIdWithBooks.QUERY)) {
                pst.setInt(1, authorId);
                try (ResultSet rs = pst.executeQuery()) {
                    Author authorDB = parseAuthorWithBooks(rs);
                    if (authorDB.getId() != 0) {
                        authorFromBD = authorDB;
                    }
                }
            }

            if (authorFromBD == null) {
                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindByIdWithoutBooks.QUERY)) {
                    pst.setInt(1, authorId);
                    try (ResultSet rs = pst.executeQuery()) {
                        Author authorDB = parseAuthorWithoutBooks(rs);
                        if (authorDB.getId() != 0) {
                            authorFromBD = authorDB;
                        }
                    }
                }
            }

            if (authorFromBD != null) {
                List<Book> forRemoveRelation = authorFromBD.getBookList();
                if (!forRemoveRelation.isEmpty()) {
                    try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_RemoveRelationAuthorsBooks.QUERY)) {
                        for (Book book : forRemoveRelation) {
                            pst.setInt(1, authorId);
                            pst.setInt(2, book.getId());
                            pst.executeUpdate();
                        }
                    }
                }
                try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorDeleteById.QUERY)) {
                    pst.setInt(1, authorId);

                    rowsUpdated = pst.executeUpdate();
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
}
