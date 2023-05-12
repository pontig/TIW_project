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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.ConnectorHandler;
import it.polimi.tiw.beans.Category;
import it.polimi.tiw.dao.CategoryDAO;

/**
 * Servlet implementation class SelectTree
 * anche se l'algoritmo è pressoché identico, la richiesta
 * è fondamentalmente diversa ==> servlet diversa
 */
@WebServlet("/SelectTree")
public class SelectTree extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    public SelectTree() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectorHandler.getConnection(getServletContext());
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
		int id = -1;
		try {
			id = Integer.parseInt(request.getParameter("radix"));
			if (id == -1) throw new NumberFormatException();
 		} catch (NumberFormatException e) {    
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot find subtree");
			return;
		}
		
		CategoryDAO dao = new CategoryDAO(connection);
		List<Category> tree = null;
		
		try {
			tree = dao.getAll(id);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
		}
		
		String path = "/WEB-INF/home.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("categories", tree);
		ctx.setVariable("areWeCopying", true);
		ctx.setVariable("from", id);
		templateEngine.process(path, ctx, response.getWriter());
		
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "This page doesn't resolve POST requests");
	}
	
	public void destroy() {
		try {
			ConnectorHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}