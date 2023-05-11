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

import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.ConnectorerHandler;
import it.polimi.tiw.beans.Category;

@WebServlet("/GoToHome")
public class GoToHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public GoToHome() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectorerHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("currentUser") == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
			return;
		}
		// No arguments since we are searching for the entire tree

		CategoryDAO dao = new CategoryDAO(connection);
		List<Category> tree = null;
		List<Category> linear = new ArrayList<Category>();
		

		try {
			tree = dao.getAll(null);		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
		}
		linearize(tree, linear);
		
		String path = "/WEB-INF/home.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("categories", tree);
		ctx.setVariable("line", linear);
		if (request.getParameter("ErrorMsgInsertion") != null ) 
			ctx.setVariable("ErrorMsgInsertion", StringEscapeUtils.escapeJava(request.getParameter("username")));
		templateEngine.process(path, ctx, response.getWriter());
		
	}
	
	private void linearize(List<Category> nl, List<Category> res) {
		for(Category c : nl) {
			res.add(c);
			linearize(c.getChildren(), res);
		}
	}

}