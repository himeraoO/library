package com.github.himeraoo.library.servlets;

import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.jdbc.SessionManagerJDBC;
import com.github.himeraoo.library.repository.AuthorRepository;
import com.github.himeraoo.library.repository.AuthorRepositoryImpl;
import com.github.himeraoo.library.repository.BookRepository;
import com.github.himeraoo.library.repository.BookRepositoryImpl;
import com.github.himeraoo.library.repository.GenreRepository;
import com.github.himeraoo.library.repository.GenreRepositoryImpl;
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

            try {
                properties.load(inStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sessionManager = new SessionManagerJDBC(properties.getProperty("dbUrl"), properties.getProperty("dbUsername"), properties.getProperty("dbPassword"), properties.getProperty("dbDriver"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        AuthorRepository authorRepository = new AuthorRepositoryImpl(sessionManager);
        BookRepository bookRepository = new BookRepositoryImpl(sessionManager);
        GenreRepository genreRepository = new GenreRepositoryImpl(sessionManager);

        AuthorService authorService = new AuthorServiceImpl(authorRepository);
        BookService bookService = new BookServiceImpl(bookRepository);
        GenreService genreService = new GenreServiceImpl(genreRepository);

        servletContext.setAttribute("authorService", authorService);
        servletContext.setAttribute("bookService", bookService);
        servletContext.setAttribute("genreService", genreService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
