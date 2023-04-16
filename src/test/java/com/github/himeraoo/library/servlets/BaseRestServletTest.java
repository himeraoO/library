package com.github.himeraoo.library.servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotDeletedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
import com.github.himeraoo.library.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;

import static com.github.himeraoo.library.util.TestUtils.*;
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

    @BeforeEach
    public void init() throws SQLException, ElementHasNotFoundException, ElementHasNotDeletedException, JsonProcessingException, ElementHasNotUpdatedException, ElementHasNotAddedException {

        authorRESTServlet = new AuthorRESTServlet();
        bookRESTServlet = new BookRESTServlet();
        genreRESTServlet = new GenreRESTServlet();

        authorRESTServlet.authorService = authorService;
        genreRESTServlet.genreService = genreService;
        bookRESTServlet.bookService = bookService;



//                .thenReturn(Collections.singletonList(getAuthorDTO(getFullAuthor(1))));

//        initAuthorRESTServletMock();
//        initBookRESTServletMock();
        initGenreRESTServletMock();
    }

    private void initGenreRESTServletMock() throws SQLException, ElementHasNotDeletedException, ElementHasNotFoundException, JsonProcessingException, ElementHasNotUpdatedException, ElementHasNotAddedException {
        lenient()
                .when(genreService.findAll())
                .thenReturn(Collections.singletonList(getGenreDTO(getFullGenre(1))));

        lenient()
                .when(genreService.findById(1))
                .thenReturn(getGenreDTO(getFullGenre(1)));

        lenient()
                .when(genreService.deleteById(1))
                .thenReturn(1);

        String json = "{\"id\": 1,\"name\": \"GENRE1\"}";
        ObjectMapper mapper = new ObjectMapper();
        GenreDTO genreDTO = mapper.readValue(json, GenreDTO.class);
        lenient()
                .when(genreService.update(genreDTO))
                .thenReturn(1);

        lenient()
                .when(genreService.save(genreDTO))
                .thenReturn(1);
    }
}
