package com.github.himeraoo.library.servlets;

import com.github.himeraoo.library.dao.*;
import com.github.himeraoo.library.repository.AuthorRepository;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.jdbc.SessionManagerJDBC;
import com.github.himeraoo.library.repository.AuthorRepositoryImpl;
import com.github.himeraoo.library.repository.BookRepository;
import com.github.himeraoo.library.repository.BookRepositoryImpl;
import com.github.himeraoo.library.service.AuthorService;
import com.github.himeraoo.library.service.AuthorServiceImpl;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener {

    private SessionManager sessionManager;
    private AuthorRepository authorRepository;
    private AuthorService authorService;
    private BookRepository bookRepository;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        final ServletContext servletContext = servletContextEvent.getServletContext();

        sessionManager = new SessionManagerJDBC();

        AuthorDAO authorDAO = new AuthorDAOImpl();
        GenreDAO genreDAO = new GenreDAOImpl();
        BookDAO bookDAO = new BookDAOImpl();
        AuthorsBooksDAO authorsBooksDAO = new AuthorsBooksDAOImpl(authorDAO, bookDAO, genreDAO);
        authorRepository = new AuthorRepositoryImpl(sessionManager, authorDAO, genreDAO, bookDAO);
        bookRepository = new BookRepositoryImpl(sessionManager, bookDAO, genreDAO, authorsBooksDAO);

        authorService = new AuthorServiceImpl(authorRepository);

        servletContext.setAttribute("JDBCSession", sessionManager);
        servletContext.setAttribute("authorRepository", authorRepository);
        servletContext.setAttribute("authorService", authorService);
        servletContext.setAttribute("bookRepository", bookRepository);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
