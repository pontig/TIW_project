package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.Connector;
import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.exceptions.TooManyChildrenException;
import it.polimi.tiw.beans.Category;

/**
 * Servlet implementation class CopyHere
 */
@WebServlet("/CopyHere")
public class CopyHere extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CopyHere() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("currentUser") == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
			return;
		}
		int id_from = -1, id_to = -1;

		try {
			id_from = Integer.parseInt(request.getParameter("id_from"));
			id_to = Integer.parseInt(request.getParameter("id_to"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing arguments");
			return;
		}

		CategoryDAO dao = new CategoryDAO(connection);
		Category treeToCopy = dao.getByParentId(id_from); // Ho il sottoalbero da copiare
		boolean isRecursive = searchChild(treeToCopy, id_to);
		if (isRecursive) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot copy a category into itself");
			return;
		}
		String path = getServletContext().getContextPath() + "/GoToHome";
		try {
			copySubTree(treeToCopy, (long) id_to);
		} catch (TooManyChildrenException tmce) {
			// We cannot append another child to the category
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot add the tenth child");
			return;
			
		} catch (SQLException e) {
		
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error in MySQL occurred: " + e.getMessage());
			return;
		}
		response.sendRedirect(path);

	}
	
	private boolean searchChild(Category p, int id) {
		return p.getId() == id ? true : searchInChildren(p.getChildren(), id);
	}
	
	private boolean searchInChildren(List<Category> tree, int id) {
		boolean res = false;
		if (tree == null) return false;
		else for (Category c : tree) {
			if (c.getId() == id) res = true;
			else res = res || searchInChildren(c.getChildren(), id);
		}
		return res;
	}

	private void copySubTree(Category node, long father) throws TooManyChildrenException, SQLException {
		CategoryDAO dao = new CategoryDAO(connection);
		ResultSet new_id = dao.insertNewCategory(node.getName(), (int) father);
		if (new_id.next()) {
			List<Category> children = node.getChildren();
			for (Category c : children) {
				copySubTree(c, new_id.getLong(1));
			}
		}
		return;
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
