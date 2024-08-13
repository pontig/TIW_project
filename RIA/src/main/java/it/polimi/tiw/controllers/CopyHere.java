package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

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

	public CopyHere() {
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
			int id_from = -1, id_to = -1;

			try {
				id_from = Integer.parseInt(request.getParameter("id_from"));
				id_to = Integer.parseInt(request.getParameter("id_to"));

				CategoryDAO dao = new CategoryDAO(connection);
				Category treeToCopy = dao.getByParentId(id_from); // Ho il sottoalbero da copiare
				boolean isRecursive = searchChild(treeToCopy, id_to);
				if (isRecursive) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					outcome = "Cannot copy a category into itself";

				} else {
					try {
						copySubTree(treeToCopy, (long) id_to);
					} catch (TooManyChildrenException tmce) {
						// We cannot append another child to the category
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						outcome = "Cannot copy the category: too many children";
					} catch (SQLException e) {
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						outcome = "Cannot copy the category: database error " + e.getMessage();
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				outcome = "Invalid parameters";
			}
		}
		String json = new Gson().toJson(outcome);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);

	}

	private boolean searchChild(Category p, int id) {
		return p.getId() == id ? true : searchInChildren(p.getChildren(), id);
	}

	private boolean searchInChildren(List<Category> tree, int id) {
		boolean res = false;
		if (tree == null)
			return false;
		else
			for (Category c : tree) {
				if (c.getId() == id)
					res = true;
				else
					res = res || searchInChildren(c.getChildren(), id);
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
