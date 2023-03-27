package com.github.himeraoo.library.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.himeraoo.library.dto.AuthorDTO;
import com.github.himeraoo.library.exception.ElementNotFoundException;
import com.github.himeraoo.library.service.AuthorService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

//@WebServlet(name = "getAlAuthors", urlPatterns = "/author/*")
public class GetAuthorByIdServlet extends HttpServlet {

    AuthorService authorService;

    public void init() {
        final Object authorService = getServletContext().getAttribute("authorService");
        this.authorService = (AuthorService) authorService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        String requestPath = req.getRequestURI();
//        String uri = req.getRequestURI();
//        int id = Integer.parseInt(uri.substring("/author/".length()));
        System.out.println("//////////////////////////////////////");
//        System.out.println(id);
        System.out.println("//////////////////////////////////////");


        if (requestPath.matches("^/author/\\d+$")) {
            String[] parts = requestPath.split("/");
            String authorIdParam = parts[2];
            System.out.println("//////////////////////////////////////");
            System.out.println(authorIdParam);
            System.out.println("//////////////////////////////////////");


//            try {
////                AuthorDTO authorDTO = authorService.findById(Integer.parseInt(authorIdParam));
////                json = mapper.writeValueAsString(authorDTO);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
        }





        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }
}
/*
	 Exception Report
	java.sql.SQLException: Can&#39;t call commit when autocommit=true
	 The server encountered an unexpected condition that prevented it from fulfilling the request.

	Exception
	com.github.himeraoo.library.jdbc.SessionManagerException: java.sql.SQLException: Can&#39;t call commit when autocommit=true
	com.github.himeraoo.library.jdbc.SessionManagerJDBC.commitSession(SessionManagerJDBC.java:60)
	com.github.himeraoo.library.repository.AuthorRepositoryImpl.deleteById(AuthorRepositoryImpl.java:118)
	com.github.himeraoo.library.service.AuthorServiceImpl.deleteById(AuthorServiceImpl.java:70)
	com.github.himeraoo.library.servlets.GetAuthorServlet.doDelete(GetAuthorServlet.java:87)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:658)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:733)
	org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)

	Root Cause
	java.sql.SQLException: Can&#39;t call commit when autocommit=true
	com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:67)
	com.mysql.cj.jdbc.ConnectionImpl.commit(ConnectionImpl.java:782)
	com.github.himeraoo.library.jdbc.SessionManagerJDBC.commitSession(SessionManagerJDBC.java:58)
	com.github.himeraoo.library.repository.AuthorRepositoryImpl.deleteById(AuthorRepositoryImpl.java:118)
	com.github.himeraoo.library.service.AuthorServiceImpl.deleteById(AuthorServiceImpl.java:70)
	com.github.himeraoo.library.servlets.GetAuthorServlet.doDelete(GetAuthorServlet.java:87)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:658)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:733)
	org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)

	>Note</b> The full stack trace of the root cause is available in the server logs.</p>


</html>


{
    "id": 0,
    "name": "author_name1",
    "surname": "author_surname1",
    "bookList":[
        {
            "id": 0,
            "title": "book1",
            "genre": {
                "id": 0,
                "name": "genre1"
                    },
            "authorList": []
         },
         {
            "id": 0,
            "title": "book5",
            "genre": {
                "id": 0,
                "name": "genre5"
                    },
            "authorList": []
          }
               ]
}

*/