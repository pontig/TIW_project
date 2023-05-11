package it.polimi.tiw.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.ConnectorerHandler;
import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.exceptions.BlankFieldException;

/**
 * Servlet implementation class UploadImgs
 */
@WebServlet("/UploadImgs")
@MultipartConfig
public class UploadImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(OpenCategory.class.getName()); // DEBUG only
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public UploadImage() {
		super();
		// TODO Auto-generated constructor stub
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
		String category_name = null;

		try {
			category_id = Integer.parseInt(request.getParameter("id"));
			imagePart = request.getPart("image");
			category_name = StringEscapeUtils.escapeJava(request.getParameter("category_name"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing arguments");
			return;
		}

		try {
			if (category_id == -1 || imagePart == null || imagePart.getSize()==0) {
				throw new BlankFieldException("There cannot be blank fields");
			}

		} catch (BlankFieldException bfe) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, bfe.toString());
			return;
		}

		if (imagePart != null) {
			imageStream = imagePart.getInputStream();
			String filename = imagePart.getSubmittedFileName();
			mimeType = getServletContext().getMimeType(filename);
		}

		CategoryDAO dao = new CategoryDAO(connection);
		try {
			dao.uploadImage(category_id, imageStream);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		response.sendRedirect(getServletContext().getContextPath() + "/OpenCategory?id=" + category_id
				+ "&category_name=" + category_name);
		// TODO: test type of the file

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
//		doPost(request, response);
	}

}
