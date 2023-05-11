package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

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
@WebServlet("/RenameCategory")
public class RenameCategory extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(OpenCategory.class.getName()); // DEBUG only
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public RenameCategory() {
		super();
	}

	public void init() throws ServletException {
		connection = Connector.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("currentUser") == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
			return;
		}
		int id_category = -1;
		String newName = null;
		try {
			id_category = Integer.parseInt(request.getParameter("id"));
			newName = StringEscapeUtils.escapeJava(request.getParameter("newName"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing arguments");
			return;
		}
		
		CategoryDAO dao = new CategoryDAO(connection);
		try {
			dao.rename(id_category, newName);
		}catch (SQLException e) {
			response.sendError(500, "Database access failed");
		}	
		
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "This page doesn't resolve POST requests");
	}
}
