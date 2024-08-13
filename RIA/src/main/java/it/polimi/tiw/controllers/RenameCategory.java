package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;

import it.polimi.tiw.Connector;
import it.polimi.tiw.dao.CategoryDAO;

/**
 * Servlet implementation class OpenCategory
 */
@WebServlet("/RenameCategory")
public class RenameCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public RenameCategory() {
		super();
	}

	public void init() throws ServletException {
		connection = Connector.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		String outcome = "true";
		if (session == null || session.getAttribute("currentUser") == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			outcome = "No user logged in";
		} else {
			int id_category = -1;
			String newName = null;
			try {
				id_category = Integer.parseInt(request.getParameter("id"));
				newName = StringEscapeUtils.escapeJava(request.getParameter("newName"));
				
				CategoryDAO dao = new CategoryDAO(connection);
				try {
					dao.rename(id_category, newName);
				} catch (SQLException e) {
					response.setStatus(500);
					outcome = "Internal server error";
				}
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				outcome = "Error in parameters";
			}
		}
		String json = new Gson().toJson(outcome);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "This page doesn't resolve POST requests");
	}
	
    public void destroy() {
        try {
            Connector.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
