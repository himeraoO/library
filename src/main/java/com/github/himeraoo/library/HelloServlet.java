package com.github.himeraoo.library;

import com.github.himeraoo.library.dao.BookDAO;
import com.github.himeraoo.library.dao.BookDAOImpl;
import com.github.himeraoo.library.repository.AuthorRepository;
import com.github.himeraoo.library.jdbc.SessionManager;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    private String message;
//    private AuthorRepository authorRepository;
    private SessionManager JDBCSession;

    public void init() {
        message = "Hello World!";
//        final Object aDAO = getServletContext().getAttribute("authorDao");
//        this.authorRepository = (AuthorRepository) aDAO;
        final Object jdbcSession = getServletContext().getAttribute("JDBCSession");
        this.JDBCSession = (SessionManager) jdbcSession;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");

        try {
            JDBCSession.beginSession();
//            Statement statement = JDBCSession.getCurrentSession().createStatement();
//
//        ResultSet resultSet = statement.executeQuery("SELECT * FROM book");
//            while (resultSet.next()){
//                out.println("<p>" + resultSet.getString(2) + "</p>");
//            }
//            resultSet.close();
//            statement.close();

            BookDAO bookDAO = new BookDAOImpl();
            out.println("<p>" + bookDAO.findAllBook(JDBCSession.getCurrentSession()).toString() + "</p>");

            JDBCSession.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

//        try {
////            String stringADAO = authorDAO.findById(3).get().toString();
//            String stringADAO = authorRepository.findAll().toString();
//            out.println("<p>" + "////////////////////////////" + "</p>");
//            out.println("<p>" + stringADAO + "</p>");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }



        out.println("<h1>" + message + "</h1>");
        out.println("</body></html>");
    }

    public void destroy() {
    }
}