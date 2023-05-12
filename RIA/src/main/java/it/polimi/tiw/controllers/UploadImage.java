package it.polimi.tiw.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import com.google.gson.Gson;

import it.polimi.tiw.ConnectorHandler;
import it.polimi.tiw.dao.CategoryDAO;

/**
 * Servlet implementation class UploadImgs
 */
@WebServlet("/UploadImgs")
@MultipartConfig
public class UploadImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public UploadImage() {
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("currentUser") == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
			return;
		}

		int category_id = -1;
		Part imagePart = null;
		InputStream imageStream = null;
		String mimeType = null;
		//String category_name = null;

		try {
			category_id = Integer.parseInt(request.getParameter("id"));
			imagePart = request.getPart("image");
			//category_name = StringEscapeUtils.escapeJava(request.getParameter("category_name"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing arguments");
			return;
		}
		
		if (category_id == -1 || imagePart == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "There cannot be blank fields");
			return;
		}
		
		if (imagePart != null ) {
			imageStream = imagePart.getInputStream();
			String filename = imagePart.getSubmittedFileName();
			mimeType = getServletContext().getMimeType(filename);			
		}
		
		CategoryDAO dao = new CategoryDAO(connection);
		String outcome = "true";
		try {
			dao.uploadImage(category_id, imageStream);
		} catch (SQLException e) {
			outcome = "false";
			e.printStackTrace();
			return;
		}
		String json = new Gson().toJson(outcome);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		// we don't check for the type of the file

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot resolve GET request on this page");
	}

}
