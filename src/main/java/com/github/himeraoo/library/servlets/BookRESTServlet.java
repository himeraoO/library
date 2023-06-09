package com.github.himeraoo.library.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.himeraoo.library.dto.BookDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
import com.github.himeraoo.library.service.BookService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/rest/book/*")
public class BookRESTServlet extends HttpServlet {

    private BookService bookService;

    public void init() {
        final Object bookService = getServletContext().getAttribute("bookService");
        this.bookService = (BookService) bookService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestPath = req.getRequestURI();
        req.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        if (requestPath.matches("^/api/rest/book/$")) {
            try {
                List<BookDTO> allBookDTOs = bookService.findAll();
                json = mapper.writeValueAsString(allBookDTOs);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(json);
                resp.setStatus(200);
            } catch (ElementHasNotFoundException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(404);
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(500);
            }
        } else if (requestPath.matches("^/api/rest/book/\\d+$")) {
            String[] parts = requestPath.split("/");
            String bookIdParam = parts[4];
            try {
                BookDTO bookDTO = bookService.findById(Integer.parseInt(bookIdParam));
                json = mapper.writeValueAsString(bookDTO);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(json);
                resp.setStatus(200);
            } catch (ElementHasNotFoundException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(404);
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(500);
            }
        } else {
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter out = resp.getWriter();
            out.write("Неправильный запрос");
            resp.setStatus(400);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestPath = req.getRequestURI();
        req.setCharacterEncoding("UTF-8");

        if (requestPath.matches("^/api/rest/book/\\d+$")) {
            String[] parts = requestPath.split("/");
            String bookIdParam = parts[4];
            try {
                int del = bookService.deleteById(Integer.parseInt(bookIdParam));

                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Удалено записей " + del);
                resp.setStatus(200);

            } catch (ElementHasNotFoundException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(404);
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(500);
            }
        } else {
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter out = resp.getWriter();
            out.write("Неправильный запрос");
            resp.setStatus(400);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestPath = req.getRequestURI();
        req.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        String json = req.getReader().lines().collect(Collectors.joining());

        BookDTO bookDTO = mapper.readValue(json, BookDTO.class);

        if (requestPath.matches("^/api/rest/book/\\d+$")) {
            String[] parts = requestPath.split("/");
            String bookIdParam = parts[4];
            try {
                bookDTO.setId(Integer.parseInt(bookIdParam));
                int upd = bookService.update(bookDTO);

                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Обновлено записей " + upd);
                resp.setStatus(200);

            } catch (ElementHasNotFoundException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(404);
            } catch (ElementHasNotUpdatedException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(400);
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(500);
            }
        } else {
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter out = resp.getWriter();
            out.write("Неправильный запрос");
            resp.setStatus(400);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestPath = req.getRequestURI();
        req.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        String json = req.getReader().lines().collect(Collectors.joining());

        BookDTO bookDTO = mapper.readValue(json, BookDTO.class);

        if (requestPath.matches("^/api/rest/book/$")) {
            try {
                int add = bookService.save(bookDTO);

                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Добавлена запись с id " + add);
                resp.setStatus(200);

            } catch (ElementHasNotAddedException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(404);
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(e.getMessage());
                resp.setStatus(500);
            }
        } else {
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter out = resp.getWriter();
            out.write("Неправильный запрос");
            resp.setStatus(400);
        }
    }
}
