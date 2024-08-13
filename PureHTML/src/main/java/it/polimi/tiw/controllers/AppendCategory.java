package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
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
	private TemplateEngine templateEngine;

	public AppendCategory() {
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);

		if (session == null || session.getAttribute("currentUser") == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
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
		ServletContext context = getServletContext();
		final WebContext cont = new WebContext(request, response, context, request.getLocale());
		String path = getServletContext().getContextPath() + "/GoToHome";
		try {
			ResultSet dummy = dao.insertNewCategory(name, father);
		} catch (TooManyChildrenException tmce) {
			// We cannot append another child to the category
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot add the tenth child");
			return;

		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		} 
			response.sendRedirect(path);
		
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
