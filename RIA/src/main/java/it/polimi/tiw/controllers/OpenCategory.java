package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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

import it.polimi.tiw.Connector;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.dao.CategoryDAO;

/**
 * Servlet implementation class OpenCategory
 */
@WebServlet("/OpenCategory")
public class OpenCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public OpenCategory() {
		super();
	}

	public void init() throws ServletException {
		connection = Connector.getConnection(getServletContext());

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		String outcome = "true";
		List<Image> images;
		if (session == null || session.getAttribute("currentUser") == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			outcome = "No user logged in";
		} else {
			int id_category = -1;
			String name_category = null;
			try {
				id_category = Integer.parseInt(request.getParameter("id"));

				CategoryDAO dao = new CategoryDAO(connection);
				try {
					images = dao.getImages(id_category);
				} catch (SQLException e) {
					response.setStatus(500);
					outcome = "Internal server error";
				}
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				outcome = "Invalid category id";
				return;
			}
		}
		String json;
		if (outcome.equals("true")) {
			json = new Gson().toJson(images);
		} else {
			json = new Gson().toJson(outcome);
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "This page doesn't resolve POST requests");
	}
}
