package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.dao.jdbc.SessionManager;
import com.github.himeraoo.library.models.Author;
import com.github.himeraoo.library.models.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


public class AuthorDAOImpl implements AuthorDAO{
    private final SessionManager sessionManager;
//    private final String QUERY_AuthorFindById = "select a.id as aid, a.name as aname, a.surname as asurname, b.id as bid, b.title as btitle from author as a inner join authors_books as ab on a.id=ab.author_id inner join book as b on b.id=ab.book_id where a.id = ?";
//    private final String QUERY_AuthorFindAll = "select a.id as aid, a.name as aname, a.surname as asurname, b.id as bid, b.title as btitle from author as a inner join authors_books as ab on a.id=ab.author_id inner join book as b on b.id=ab.book_id";
//    private final String QUERY_AuthorUpdateById = "update author set name = ?, surname = ? where id = ?";
//    private final String QUERY_AuthorDeleteById = "delete from author where id = ?";
//    private final String QUERY_AuthorSave = "insert into author (name, surname) VALUES (?, ?)";


    public AuthorDAOImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Optional<Author> findById(int id) throws SQLException {
        Author dbAuthor = new Author();
        sessionManager.beginSession();
        List<Book> books = new ArrayList<>();

        try (
             Connection connection = sessionManager.getCurrentSession();
             PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindById.QUERY)
        ) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                while(rs.next()){
                    dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
                    dbAuthor.setName(rs.getString("aname"));
                    dbAuthor.setSurname(rs.getString("asurname"));

                    Book book = new Book();
                    book.setId(Integer.parseInt(rs.getString("bid")));
                    book.setTitle(rs.getString("btitle"));
                    books.add(book);
                }
            }
            dbAuthor.setBookList(books);
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }

        return Optional.ofNullable(dbAuthor);
    }

    @Override
    public List<Author> findAll() throws SQLException {
        List<Author> authorList = null;

        HashMap<Integer, Author> integerAuthorHashMap = new HashMap<>();

        sessionManager.beginSession();

        try (
                Connection connection = sessionManager.getCurrentSession();
                PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorFindAll.QUERY)
        ) {
            try (ResultSet rs = pst.executeQuery()) {
                while(rs.next()){
                    Author dbAuthor = new Author();
                    dbAuthor.setId(Integer.parseInt(rs.getString("aid")));
                    dbAuthor.setName(rs.getString("aname"));
                    dbAuthor.setSurname(rs.getString("asurname"));

                    Book book = new Book();
                    book.setId(Integer.parseInt(rs.getString("bid")));
                    book.setTitle(rs.getString("btitle"));

                    if(integerAuthorHashMap.containsKey(dbAuthor.getId())){
                        integerAuthorHashMap.get(dbAuthor.getId()).getBookList().add(book);
                    }else {
                        List<Book> bookList = new ArrayList<>();
                        bookList.add(book);
                        dbAuthor.setBookList(bookList);
                        integerAuthorHashMap.put(dbAuthor.getId(), dbAuthor);
                    }
                }
            }
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
        authorList = new ArrayList<>(integerAuthorHashMap.values());
        return authorList;
    }

    @Override
    public int save(Author author) throws SQLException {
        sessionManager.beginSession();

        try (Connection connection = sessionManager.getCurrentSession();
             PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorSave.QUERY, Statement.RETURN_GENERATED_KEYS)
        ) {
            pst.setString(1, author.getName());
            pst.setString(2, author.getSurname());

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                rs.next();
                int id = rs.getInt(1);
                sessionManager.commitSession();

                return id;
            }
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public int update(Author author) throws SQLException {

        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (
                Connection connection = sessionManager.getCurrentSession();
                PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorUpdateById.QUERY)
        ) {
            pst.setString(1, author.getName());
            pst.setString(2, author.getSurname());
            pst.setInt(3, author.getId());

            rowsUpdated = pst.executeUpdate();
            sessionManager.commitSession();
            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }

    @Override
    public int deleteById(int id) throws SQLException {
        int rowsUpdated = 0;

        sessionManager.beginSession();

        try (
                Connection connection = sessionManager.getCurrentSession();
                PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_AuthorDeleteById.QUERY)
        ) {
            pst.setInt(1, id);

            rowsUpdated = pst.executeUpdate();
            sessionManager.commitSession();
            return rowsUpdated;
        } catch (SQLException ex) {
            sessionManager.rollbackSession();
            throw ex;
        }
    }
}
