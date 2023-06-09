package com.github.himeraoo.library.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.himeraoo.library.dto.AuthorDTO;
import com.github.himeraoo.library.exception.ElementHasNotAddedException;
import com.github.himeraoo.library.exception.ElementHasNotFoundException;
import com.github.himeraoo.library.exception.ElementHasNotUpdatedException;
import com.github.himeraoo.library.service.AuthorService;

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

@WebServlet("/api/rest/author/*")
public class AuthorRESTServlet extends HttpServlet {

    private AuthorService authorService;

    public void init() {
        final Object authorService = getServletContext().getAttribute("authorService");
        this.authorService = (AuthorService) authorService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestPath = req.getRequestURI();
        req.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        if (requestPath.matches("^/api/rest/author/$")) {
            try {
                List<AuthorDTO> allAuthorDTOs = authorService.findAll();
                json = mapper.writeValueAsString(allAuthorDTOs);
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
        } else if (requestPath.matches("^/api/rest/author/\\d+$")) {
            String[] parts = requestPath.split("/");
            String authorIdParam = parts[4];
            try {
                AuthorDTO authorDTO = authorService.findById(Integer.parseInt(authorIdParam));
                json = mapper.writeValueAsString(authorDTO);
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

        if (requestPath.matches("^/api/rest/author/\\d+$")) {
            String[] parts = requestPath.split("/");
            String authorIdParam = parts[4];
            try {
                int del = authorService.deleteById(Integer.parseInt(authorIdParam));

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

        AuthorDTO authorDTO = mapper.readValue(json, AuthorDTO.class);

        if (requestPath.matches("^/api/rest/author/\\d+$")) {
            String[] parts = requestPath.split("/");
            String authorIdParam = parts[4];
            try {
                authorDTO.setId(Integer.parseInt(authorIdParam));
                int upd = authorService.update(authorDTO);

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

        AuthorDTO authorDTO = mapper.readValue(json, AuthorDTO.class);

        if (requestPath.matches("^/api/rest/author/$")) {
            try {
                int add = authorService.save(authorDTO);

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