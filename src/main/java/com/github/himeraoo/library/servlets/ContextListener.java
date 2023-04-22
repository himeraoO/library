package com.github.himeraoo.library.servlets;

import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.jdbc.SessionManagerJDBC;
import com.github.himeraoo.library.dao.AuthorDAO;
import com.github.himeraoo.library.dao.AuthorDAOImpl;
import com.github.himeraoo.library.dao.BookDAO;
import com.github.himeraoo.library.dao.BookDAOImpl;
import com.github.himeraoo.library.dao.GenreDAO;
import com.github.himeraoo.library.dao.GenreDAOImpl;
import com.github.himeraoo.library.service.AuthorService;
import com.github.himeraoo.library.service.AuthorServiceImpl;
import com.github.himeraoo.library.service.BookService;
import com.github.himeraoo.library.service.BookServiceImpl;
import com.github.himeraoo.library.service.GenreService;
import com.github.himeraoo.library.service.GenreServiceImpl;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@WebListener
public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        final ServletContext servletContext = servletContextEvent.getServletContext();

        SessionManager sessionManager = null;

        try (InputStream inStream = servletContext.getResourceAsStream("/WEB-INF/resources/app.properties")) {
            Properties properties = new Properties();
            properties.load(inStream);

            sessionManager = new SessionManagerJDBC(
                    properties.getProperty("dbUrl"),
                    properties.getProperty("dbUsername"),
                    properties.getProperty("dbPassword"),
                    properties.getProperty("dbDriver")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        AuthorDAO authorDAO = new AuthorDAOImpl(sessionManager);
        BookDAO bookDAO = new BookDAOImpl(sessionManager);
        GenreDAO genreDAO = new GenreDAOImpl(sessionManager);

        AuthorService authorService = new AuthorServiceImpl(authorDAO);
        BookService bookService = new BookServiceImpl(bookDAO);
        GenreService genreService = new GenreServiceImpl(genreDAO);

        servletContext.setAttribute("authorService", authorService);
        servletContext.setAttribute("bookService", bookService);
        servletContext.setAttribute("genreService", genreService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
