package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;


import com.google.gson.Gson;

import it.polimi.tiw.Connector;
import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.exceptions.BlankFieldException;
import it.polimi.tiw.exceptions.TooManyChildrenException;

/**
 * Servlet implementation class AppendCategory
 */
@WebServlet("/AppendCategory")
@MultipartConfig
public class AppendCategory extends HttpServlet {

	private static final long serialVersionUID = 4L;
	private Connection connection = null;


	public AppendCategory() {
		super();
	}

	public void init() throws ServletException {
		connection = Connector.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);

		if (session == null || session.getAttribute("currentUser") == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No user logged in");
			return;
		}
		
		// Parameters
		String name = null;
		int father;

		try {
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			father = Integer.parseInt(request.getParameter("father"));
			if(name == null || name.isEmpty())
				throw new BlankFieldException("There cannot be blank fields");
		} catch (BlankFieldException bfe) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, bfe.toString());
			return;
		}

		CategoryDAO dao = new CategoryDAO(connection);
		String outcome = "true";
		try {
			ResultSet dummy = dao.insertNewCategory(name, father);
		} catch (TooManyChildrenException tmce) {
			// We cannot append another child to the category
			outcome = "Cannot append the tenth child";
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		} catch (SQLException e) {
			outcome = "false";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		} finally {
			String json = new Gson().toJson(outcome);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// We allow get requests to this page just because it is easier to simulate bad requests
		doPost(request, response);
		// This method should be implemented just with the following line:
		// response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot resolve GET request on this page");
	}

	public void destroy() {
		try {
			Connector.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
