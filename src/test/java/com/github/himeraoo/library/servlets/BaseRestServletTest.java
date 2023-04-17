package com.github.himeraoo.library.servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.himeraoo.library.dto.AuthorDTO;
import com.github.himeraoo.library.dto.BookDTO;
import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotDeletedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
import com.github.himeraoo.library.service.AuthorService;
import com.github.himeraoo.library.service.BookService;
import com.github.himeraoo.library.service.GenreService;
import com.github.himeraoo.library.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletContext;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class BaseRestServletTest {
    protected AuthorRESTServlet authorRESTServlet;
    protected BookRESTServlet bookRESTServlet;
    protected GenreRESTServlet genreRESTServlet;

    @Mock
    protected AuthorService authorService;
    @Mock
    protected BookService bookService;
    @Mock
    protected GenreService genreService;
    @Mock
    protected ServletContext servletContext;

    @BeforeEach
    public void init() throws SQLException, ElementHasNotFoundException, ElementHasNotDeletedException, JsonProcessingException, ElementHasNotUpdatedException, ElementHasNotAddedException {

        authorRESTServlet = Mockito.spy(new AuthorRESTServlet());
        bookRESTServlet = Mockito.spy(new BookRESTServlet());
        genreRESTServlet = Mockito.spy(new GenreRESTServlet());

        lenient().when(servletContext.getAttribute("authorService")).thenReturn(authorService);
        lenient().when(servletContext.getAttribute("bookService")).thenReturn(bookService);
        lenient().when(servletContext.getAttribute("genreService")).thenReturn(genreService);
        Mockito.doReturn(servletContext).when(authorRESTServlet).getServletContext();
        Mockito.doReturn(servletContext).when(bookRESTServlet).getServletContext();
        Mockito.doReturn(servletContext).when(genreRESTServlet).getServletContext();

        authorRESTServlet.init();
        bookRESTServlet.init();
        genreRESTServlet.init();

        initAuthorRESTServletMock();
        initBookRESTServletMock();
        initGenreRESTServletMock();
    }

    private void initAuthorRESTServletMock() throws SQLException, ElementHasNotFoundException, JsonProcessingException, ElementHasNotUpdatedException, ElementHasNotAddedException {
        lenient().when(authorService.findAll()).thenReturn(Collections.singletonList(TestUtils.getAuthorDTO(TestUtils.getFullAuthor(1))));
        lenient().when(authorService.findById(1)).thenReturn(TestUtils.getAuthorDTO(TestUtils.getFullAuthor(1)));
        lenient().when(authorService.deleteById(1)).thenReturn(1);

        String json = "{ \"id\": 1, \"name\": \"author_name1\", \"surname\": \"author_surname1\", \"bookList\": [ { \"id\": 1, \"title\": \"book1\", \"genre\": { \"id\": 1, \"name\": \"genre1\" }, \"authorList\": [] }, { \"id\": 5, \"title\": \"book5\", \"genre\": { \"id\": 5, \"name\": \"genre5\" }, \"authorList\": [] } ] }";
        ObjectMapper mapper = new ObjectMapper();
        AuthorDTO authorDTO = mapper.readValue(json, AuthorDTO.class);

        lenient().when(authorService.update(authorDTO)).thenReturn(1);
        lenient().when(authorService.save(authorDTO)).thenReturn(1);
    }

    private void initBookRESTServletMock() throws SQLException, ElementHasNotFoundException, JsonProcessingException, ElementHasNotUpdatedException, ElementHasNotAddedException {
        lenient().when(bookService.findAll()).thenReturn(Collections.singletonList(TestUtils.getBookDTO(TestUtils.getFullBook(1))));
        lenient().when(bookService.findById(1)).thenReturn(TestUtils.getBookDTO(TestUtils.getFullBook(1)));
        lenient().when(bookService.deleteById(1)).thenReturn(1);

        String json = "{ \"id\": 1, \"title\": \"book1\", \"genre\": { \"id\": 1, \"name\": \"genre1\" }, \"authorList\": [ { \"id\": 1, \"name\": \"author_name1\", \"surname\": \"author_surname1\", \"bookList\": [] }, { \"id\": 0, \"name\": \"author_name5\", \"surname\": \"author_surname5\", \"bookList\": [] } ] }";
        ObjectMapper mapper = new ObjectMapper();
        BookDTO bookDTO = mapper.readValue(json, BookDTO.class);

        lenient().when(bookService.update(bookDTO)).thenReturn(1);
        lenient().when(bookService.save(bookDTO)).thenReturn(1);
    }

    private void initGenreRESTServletMock() throws SQLException, ElementHasNotDeletedException, ElementHasNotFoundException, JsonProcessingException, ElementHasNotUpdatedException, ElementHasNotAddedException {
        lenient().when(genreService.findAll()).thenReturn(Collections.singletonList(TestUtils.getGenreDTO(TestUtils.getFullGenre(1))));
        lenient().when(genreService.findById(1)).thenReturn(TestUtils.getGenreDTO(TestUtils.getFullGenre(1)));
        lenient().when(genreService.deleteById(1)).thenReturn(1);

        String json = "{\"id\": 1,\"name\": \"GENRE1\"}";
        ObjectMapper mapper = new ObjectMapper();
        GenreDTO genreDTO = mapper.readValue(json, GenreDTO.class);

        lenient().when(genreService.update(genreDTO)).thenReturn(1);
        lenient().when(genreService.save(genreDTO)).thenReturn(1);
    }
}
