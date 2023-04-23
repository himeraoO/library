package com.github.himeraoo.library.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.himeraoo.library.dto.BookDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
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

import static com.github.himeraoo.library.util.TestUtils.getBookDTO;
import static com.github.himeraoo.library.util.TestUtils.getFullBook;
import static org.mockito.Mockito.lenient;

@Epic(value = "Тестирование слоя RESTServlet")
@Feature(value = "Тестирование BookRESTServlet")
@Execution(ExecutionMode.CONCURRENT)
class BookRESTServletTest extends BaseRestServletTest {

    @Test
    @DisplayName("Тест поиска всех книг")
    @Story(value = "Тестирование метода поиска всех элементов")
    void doGetAll() throws IOException, ServletException, SQLException, ElementHasNotFoundException {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        lenient().when(req.getRequestURI()).thenReturn("/api/rest/book/");
        lenient().when(resp.getWriter()).thenReturn(writer);

        List<BookDTO> expectedBookDTOList = Collections.singletonList(getBookDTO(getFullBook(1)));
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(expectedBookDTOList);

        bookRESTServlet.doGet(req, resp);

        Mockito.verify(bookService, Mockito.times(1)).findAll();
        Mockito.verify(resp.getWriter(), Mockito.times(1)).write(json);
    }

    @Test
    @DisplayName("Тест поиска книги по ID")
    @Story(value = "Тестирование метода поиска по ID")
    void doGetFindById() throws IOException, ServletException, SQLException, ElementHasNotFoundException {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        lenient().when(req.getRequestURI()).thenReturn("/api/rest/book/1");
        lenient().when(resp.getWriter()).thenReturn(writer);

        BookDTO expectedBookDTO = getBookDTO(getFullBook(1));
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(expectedBookDTO);

        bookRESTServlet.doGet(req, resp);

        Mockito.verify(bookService, Mockito.times(1)).findById(1);
        Mockito.verify(resp.getWriter(), Mockito.times(1)).write(json);
    }

    @Test
    @DisplayName("Тест удаления книги по ID")
    @Story(value = "Тестирование метода удаления элемента по ID")
    void doDelete() throws IOException, ServletException, SQLException, ElementHasNotFoundException {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        lenient().when(req.getRequestURI()).thenReturn("/api/rest/book/1");
        lenient().when(resp.getWriter()).thenReturn(writer);

        int deleted = 1;

        bookRESTServlet.doDelete(req, resp);

        Mockito.verify(bookService, Mockito.times(1)).deleteById(1);
        Mockito.verify(resp.getWriter(), Mockito.times(1)).write("Удалено записей " + deleted);
    }

    @Test
    @DisplayName("Тест обновления книги")
    @Story(value = "Тестирование метода обновления элемента")
    void doPut() throws IOException, ServletException, ElementHasNotUpdatedException, SQLException, ElementHasNotFoundException {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        BufferedReader reader = Mockito.mock(BufferedReader.class);

        lenient().when(req.getRequestURI()).thenReturn("/api/rest/book/1");
        lenient().when(resp.getWriter()).thenReturn(writer);
        lenient().when(req.getReader()).thenReturn(reader);
        String json = "{ \"id\": 1, \"title\": \"book1\", \"genre\": { \"id\": 1, \"name\": \"genre1\" }, \"authorList\": [ { \"id\": 1, \"name\": \"author_name1\", \"surname\": \"author_surname1\", \"bookList\": [] }, { \"id\": 0, \"name\": \"author_name5\", \"surname\": \"author_surname5\", \"bookList\": [] } ] }";
        lenient().when(reader.lines()).thenReturn(Stream.of(json));
        ObjectMapper mapper = new ObjectMapper();
        BookDTO bookDTO = mapper.readValue(json, BookDTO.class);

        int updated = 1;

        bookRESTServlet.doPut(req, resp);

        Mockito.verify(bookService, Mockito.times(1)).update(bookDTO);
        Mockito.verify(resp.getWriter(), Mockito.times(1)).write("Обновлено записей " + updated);
    }

    @Test
    @DisplayName("Тест сохранения новой книги")
    @Story(value = "Тестирование метода сохранения элемента")
    void doPost() throws IOException, ServletException, ElementHasNotAddedException, SQLException {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        BufferedReader reader = Mockito.mock(BufferedReader.class);

        lenient().when(req.getRequestURI()).thenReturn("/api/rest/book/");
        lenient().when(resp.getWriter()).thenReturn(writer);
        lenient().when(req.getReader()).thenReturn(reader);
        String json = "{ \"id\": 1, \"title\": \"book1\", \"genre\": { \"id\": 1, \"name\": \"genre1\" }, \"authorList\": [ { \"id\": 1, \"name\": \"author_name1\", \"surname\": \"author_surname1\", \"bookList\": [] }, { \"id\": 0, \"name\": \"author_name5\", \"surname\": \"author_surname5\", \"bookList\": [] } ] }";
        lenient().when(reader.lines()).thenReturn(Stream.of(json));
        ObjectMapper mapper = new ObjectMapper();
        BookDTO bookDTO = mapper.readValue(json, BookDTO.class);

        int added = 1;

        bookRESTServlet.doPost(req, resp);

        Mockito.verify(bookService, Mockito.times(1)).save(bookDTO);
        Mockito.verify(resp.getWriter(), Mockito.times(1)).write("Добавлена запись с id " + added);
    }
}