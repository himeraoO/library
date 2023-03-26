package com.github.himeraoo.library.servlets;

import com.github.himeraoo.library.dao.AuthorDAO;
import com.github.himeraoo.library.dao.AuthorDAOImpl;
import com.github.himeraoo.library.dao.jdbc.SessionManager;
import com.github.himeraoo.library.dao.jdbc.SessionManagerJDBC;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener {

    private SessionManager sessionManager;
    private AuthorDAO authorDAO;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        final ServletContext servletContext = servletContextEvent.getServletContext();

        sessionManager = new SessionManagerJDBC();
        authorDAO = new AuthorDAOImpl(sessionManager);

        servletContext.setAttribute("JDBCSession", sessionManager);
        servletContext.setAttribute("authorDao", authorDAO);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
