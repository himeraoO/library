package com.github.himeraoo.library.servlets;

import com.github.himeraoo.library.repository.AuthorRepository;
import com.github.himeraoo.library.jdbc.SessionManager;
import com.github.himeraoo.library.jdbc.SessionManagerJDBC;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener {

    private SessionManager sessionManager;
    private AuthorRepository authorRepository;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        final ServletContext servletContext = servletContextEvent.getServletContext();

        sessionManager = new SessionManagerJDBC();
//        authorRepository = new AuthorRepositoryImpl(sessionManager, authorDAO);

        servletContext.setAttribute("JDBCSession", sessionManager);
//        servletContext.setAttribute("authorDao", authorRepository);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
