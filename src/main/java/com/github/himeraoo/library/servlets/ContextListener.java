package com.github.himeraoo.library.servlets;

import com.github.himeraoo.library.dao.*;
import com.github.himeraoo.library.repository.*;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.jdbc.SessionManagerJDBC;
import com.github.himeraoo.library.service.*;
import com.github.himeraoo.library.util.PropertiesUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener {

    private AuthorService authorService;
    private BookService bookService;
    private GenreService genreService;


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        final ServletContext servletContext = servletContextEvent.getServletContext();

        SessionManager sessionManager = new SessionManagerJDBC(
                PropertiesUtil.getProperty("dbUrl"),
                PropertiesUtil.getProperty("dbUsername"),
                PropertiesUtil.getProperty("dbPassword")
        );

        AuthorDAO authorDAO = new AuthorDAOImpl();
        GenreDAO genreDAO = new GenreDAOImpl();
        BookDAO bookDAO = new BookDAOImpl();

        AuthorRepository authorRepository = new AuthorRepositoryImpl(sessionManager, authorDAO, genreDAO, bookDAO);
        BookRepository bookRepository = new BookRepositoryImpl(sessionManager, bookDAO, genreDAO, authorDAO);
        GenreRepository genreRepository = new GenreRepositoryImpl(sessionManager, genreDAO);

        authorService = new AuthorServiceImpl(authorRepository);
        bookService = new BookServiceImpl(bookRepository);
        genreService = new GenreServiceImpl(genreRepository);

        servletContext.setAttribute("authorService", authorService);
        servletContext.setAttribute("bookService", bookService);
        servletContext.setAttribute("genreService", genreService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
