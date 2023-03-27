package com.github.himeraoo.library.dao;

import com.github.himeraoo.library.models.Book;
import com.github.himeraoo.library.models.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreDAOImpl implements GenreDAO {

    @Override
    public Optional<Genre> findGenreById(int genreId, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindById.QUERY)) {
            pst.setInt(1, genreId);

             Genre genre = null;

            try (ResultSet rs = pst.executeQuery()) {
                Genre dbGenre = new Genre();
                rs.next();
                dbGenre.setId((Integer.parseInt(rs.getString("id"))));
                dbGenre.setName((rs.getString("name")));

                if(dbGenre.getId() != 0){
                    genre = dbGenre;
                }
            }
            return Optional.ofNullable(genre);
        }
    }

    @Override
    public List<Genre> findAllGenre(Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreFindAll.QUERY)) {

            List<Genre> genreList = new ArrayList<>();

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {

                    Genre dbGenre = new Genre();
                    dbGenre.setId((Integer.parseInt(rs.getString("id"))));
                    dbGenre.setName((rs.getString("name")));

                    genreList.add(dbGenre);
                }
            }
            return genreList;
        }
    }

    @Override
    public int saveGenre(Genre genre, Connection connection) throws SQLException {
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreSave.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, genre.getName());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public int updatedGenre(Genre genre, Connection connection) throws SQLException {
        int rowsUpdated;
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreUpdateById.QUERY)) {

            pst.setString(1, genre.getName());
            pst.setInt(2, genre.getId());

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }

    @Override
    public int deleteGenre(int genreId, Connection connection) throws SQLException {
        int rowsUpdated;
        try(PreparedStatement pst = connection.prepareStatement(SQLQuery.QUERY_GenreDeleteById.QUERY)) {
            pst.setInt(1, genreId);

            rowsUpdated = pst.executeUpdate();
        }
        return rowsUpdated;
    }

    @Override
    public int checkAddGenre(Genre genre, Connection connection) throws SQLException {
        Optional<Genre> optionalGenre = findGenreById(genre.getId(), connection);
        if (!optionalGenre.isPresent()){
            return saveGenre(genre, connection);
        }
        return 0;
    }

    @Override
    public List<Integer> checkAndAddGenreListFromBookList(List<Book> bookList, Connection connection) throws SQLException {
        List<Integer> list = new ArrayList<>();
        for (Book book:bookList) {
            int add = checkAddGenre(book.getGenre(), connection);
            if (add != 0){
                list.add(add);
            }
        }
        return list;
    }
}
