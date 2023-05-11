package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import com.google.gson.Gson;

import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.Handler;
import it.polimi.tiw.beans.Category;

@WebServlet("/GetTree")
public class GetTree extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetTree() {
		super();
	}

	public void init() throws ServletException {
		connection = Handler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		String outcome = "true";
		List<Category> tree = null;
		if (session == null || session.getAttribute("currentUser") == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			outcome = "No user logged in";
		} else {

			// No arguments since we are searching for the entire tree

			CategoryDAO dao = new CategoryDAO(connection);
			List<Category> linear = new ArrayList<Category>();

			try {
				tree = dao.getAll(null);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				outcome = "Database access failed: " + e.getMessage();
			}
		}
		if (outcome.equals("true")) {
			String json = new Gson().toJson(tree);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
		} else {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(outcome);
		}

	}

	@Deprecated
	private void linearize(List<Category> nl, List<Category> res) {
		for (Category c : nl) {
			res.add(c);
			linearize(c.getChildren(), res);
		}
	}

}