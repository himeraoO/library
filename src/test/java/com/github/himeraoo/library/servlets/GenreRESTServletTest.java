package com.github.himeraoo.library.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotDeletedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.github.himeraoo.library.util.TestUtils.getFullGenre;
import static com.github.himeraoo.library.util.TestUtils.getGenreDTO;
import static org.mockito.Mockito.lenient;

@Epic(value = "Тестирование слоя RESTServlet")
@Feature(value = "Тестирование GenreRESTServlet")
@Execution(ExecutionMode.CONCURRENT)
class GenreRESTServletTest extends BaseRestServletTest {

    @Test
    @DisplayName("Тест поиска всех жанров")
    @Story(value = "Тестирование метода поиска всех элементов")
    void doGetAll() throws SQLException, ElementHasNotFoundException, ServletException, IOException {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        lenient().when(req.getRequestURI()).thenReturn("/api/rest/genre/");
        lenient().when(resp.getWriter()).thenReturn(writer);

        List<GenreDTO> expectedGenreDTOList = Collections.singletonList(getGenreDTO(getFullGenre(1)));
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(expectedGenreDTOList);

        genreRESTServlet.doGet(req, resp);

        Mockito.verify(genreService, Mockito.times(1)).findAll();
        Mockito.verify(resp.getWriter(), Mockito.times(1)).write(json);
    }

    @Test
    @DisplayName("Тест поиска жанра по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void doGetFindById() throws SQLException, ElementHasNotFoundException, ServletException, IOException {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        lenient().when(req.getRequestURI()).thenReturn("/api/rest/genre/1");
        lenient().when(resp.getWriter()).thenReturn(writer);

        GenreDTO expectedGenreDTO = getGenreDTO(getFullGenre(1));
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(expectedGenreDTO);

        genreRESTServlet.doGet(req, resp);

        Mockito.verify(genreService, Mockito.times(1)).findById(1);
        Mockito.verify(resp.getWriter(), Mockito.times(1)).write(json);
    }

    @Test
    @DisplayName("Тест удаления жанра по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void doDelete() throws IOException, ServletException, SQLException, ElementHasNotDeletedException, ElementHasNotFoundException {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        lenient().when(req.getRequestURI()).thenReturn("/api/rest/genre/1");
        lenient().when(resp.getWriter()).thenReturn(writer);

        int deleted = 1;

        genreRESTServlet.doDelete(req, resp);

        Mockito.verify(genreService, Mockito.times(1)).deleteById(1);
        Mockito.verify(resp.getWriter(), Mockito.times(1)).write("Удалено записей " + deleted);
    }

    @Test
    @DisplayName("Тест обновления жанра")
    @Story(value = "Тестирование метода обновления элемента")
    void doPut() throws ServletException, IOException, ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        BufferedReader reader = Mockito.mock(BufferedReader.class);

        lenient().when(req.getRequestURI()).thenReturn("/api/rest/genre/1");
        lenient().when(resp.getWriter()).thenReturn(writer);
        lenient().when(req.getReader()).thenReturn(reader);
        String json = "{\"id\": 1,\"name\": \"GENRE1\"}";
        lenient().when(reader.lines()).thenReturn(Stream.of(json));
        ObjectMapper mapper = new ObjectMapper();
        GenreDTO genreDTO = mapper.readValue(json, GenreDTO.class);

        int updated = 1;

        genreRESTServlet.doPut(req, resp);

        Mockito.verify(genreService, Mockito.times(1)).update(genreDTO);
        Mockito.verify(resp.getWriter(), Mockito.times(1)).write("Обновлено записей " + updated);
    }

    @Test
    @DisplayName("Тест сохранения нового жанра")
    @Story(value = "Тестирование метода сохранения элемента")
    void doPost() throws ElementHasNotAddedException, SQLException, IOException, ServletException {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        BufferedReader reader = Mockito.mock(BufferedReader.class);

        lenient().when(req.getRequestURI()).thenReturn("/api/rest/genre/");
        lenient().when(resp.getWriter()).thenReturn(writer);
        lenient().when(req.getReader()).thenReturn(reader);
        String json = "{\"id\": 1,\"name\": \"GENRE1\"}";
        lenient().when(reader.lines()).thenReturn(Stream.of(json));
        ObjectMapper mapper = new ObjectMapper();
        GenreDTO genreDTO = mapper.readValue(json, GenreDTO.class);

        int added = 1;

        genreRESTServlet.doPost(req, resp);

        Mockito.verify(genreService, Mockito.times(1)).save(genreDTO);
        Mockito.verify(resp.getWriter(), Mockito.times(1)).write("Добавлена запись с id " + added);
    }
}