package com.github.himeraoo.library.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.himeraoo.library.dto.GenreDTO;
import com.github.himeraoo.library.exception.ElementNotAddedException;
import com.github.himeraoo.library.exception.ElementNotFoundException;
import com.github.himeraoo.library.service.GenreService;

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

@WebServlet("/genre/*")
public class GenreRESTServlet extends HttpServlet {

    GenreService genreService;

    public void init() {
        final Object genreService = getServletContext().getAttribute("genreService");
        this.genreService = (GenreService) genreService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestPath = req.getRequestURI();
        req.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        if (requestPath.matches("^/genre/$")) {
            try {
                List<GenreDTO> allGenreDTOs = genreService.findAll();
                json = mapper.writeValueAsString(allGenreDTOs);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(json);
                resp.setStatus(200);
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Произошла неизвестная ошибка");
                resp.setStatus(404);
            }
        }

        if (requestPath.matches("^/genre/\\d+$")) {
            String[] parts = requestPath.split("/");
            String genreIdParam = parts[2];

            try {
                GenreDTO genreDTO = genreService.findById(Integer.parseInt(genreIdParam));
                json = mapper.writeValueAsString(genreDTO);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(json);
                resp.setStatus(200);
            } catch (SQLException | ElementNotFoundException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Не найдено жанров с таким ID=" + genreIdParam);
                resp.setStatus(404);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestPath = req.getRequestURI();
        req.setCharacterEncoding("UTF-8");

        if (requestPath.matches("^/genre/\\d+$")) {
            String[] parts = requestPath.split("/");
            String genreIdParam = parts[2];
            try {
                int del = genreService.deleteById(Integer.parseInt(genreIdParam));

                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Удалено записей " + del);
                resp.setStatus(200);

            } catch (SQLException | ElementNotFoundException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Не найдено жанров с таким ID=" + genreIdParam);
                resp.setStatus(404);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestPath = req.getRequestURI();
        req.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        String json = req.getReader().lines().collect(Collectors.joining());

        GenreDTO genreDTO = mapper.readValue(json, GenreDTO.class);

        if (requestPath.matches("^/genre/\\d+$")) {
            String[] parts = requestPath.split("/");
            String genreIdParam = parts[2];
            try {
                genreDTO.setId(Integer.parseInt(genreIdParam));
                int upd = genreService.update(genreDTO);

                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Обновлено записей " + upd);
                resp.setStatus(200);

            } catch (SQLException | ElementNotFoundException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Ошибка обновления жанра с таким ID=" + genreIdParam);
                resp.setStatus(404);
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestPath = req.getRequestURI();
        req.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        String json = req.getReader().lines().collect(Collectors.joining());

        GenreDTO genreDTO = mapper.readValue(json, GenreDTO.class);

        if (requestPath.matches("^/genre/$")) {
            try {
                int add = genreService.save(genreDTO);

                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Добавлена запись с id " + add);
                resp.setStatus(200);

            } catch (SQLException | ElementNotAddedException e) {
                e.printStackTrace();
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write("Ошибка добавления нового жанра");
                resp.setStatus(404);
            }
        }
    }
}
