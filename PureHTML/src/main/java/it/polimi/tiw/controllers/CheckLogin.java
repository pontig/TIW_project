package it.polimi.tiw.controllers;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.ConnectorHandler;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.exceptions.BlankFieldException;
import it.polimi.tiw.beans.User;

/**
 * Servlet implementation class CheckLogin
 */
@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {

	private static final long serialVersionUID = 4643992147258064618L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CheckLogin() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot resolve GET request on this page");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
				
		// get parameters
		String username = null;
		String psw = null;

		try {
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			psw = StringEscapeUtils.escapeJava(request.getParameter("psw"));

			if (username == null || psw == null || username.isEmpty() || psw.isEmpty()) {
				// One or both are missing
				throw new BlankFieldException("There cannot be blank fields");
			}
		} catch (BlankFieldException bfe) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, bfe.toString());
			return;
		}

		UserDAO dao = new UserDAO(connection);
		User user = null;

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(psw.getBytes());

			// Convert hash bytes to hexadecimal format
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			String hashedPsw = hexString.toString();
			user = dao.checkCredentials(username, hashedPsw);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		String path;
		if (user == null) {
			// Wrong credentials. redirect back to login page
			ServletContext context = getServletContext();
			final WebContext cont = new WebContext(request, response, context, request.getLocale());
			cont.setVariable("wrongCredentials", "Incorrect username or password");
			path = "/index.html";
			templateEngine.process(path, cont, response.getWriter());
			return;
		} else {
			// Found user in the database
			request.getSession().setAttribute("currentUser", user);
			path = getServletContext().getContextPath() + "/GoToHome";
			response.sendRedirect(path);
		}
		return;

	}

	public void destroy() {
		try {
			ConnectorHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
