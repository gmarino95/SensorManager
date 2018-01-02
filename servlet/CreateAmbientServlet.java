package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import componenti.Ambiente;
import componenti.UserAccount;
import exceptions.NullException;
import exceptions.ZeroException;
import utils.DBUtils;
import utils.MyUtils;

/**
 * Servlet implementation class CreateAmbientServlet
 */
@WebServlet(urlPatterns = { "/createAmbient"})
public class CreateAmbientServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
     
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateAmbientServlet() {
        super();
    }

	/**
	 * Show ambient create page
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/WEB-INF/views/createAmbient.jsp");
		
		dispatcher.forward(request, response);
	}

	/**
	 * When the user enters the ambient information, and click Submit.
	 * This method will be called.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Connection conn = MyUtils.getStoredConnection(request);
		
		Ambiente ambient = null;
		
		UserAccount user = null;
		
		String errorString = null;
		
		String name = (String) request.getParameter("nome");
		String ubicazione = (String) request.getParameter("ubicazione");
		String tipo = (String) request.getParameter("selAmbient");

		try {
			ambient = new Ambiente(name, tipo, ubicazione, 0);
			
		} catch (NullException e) {

			System.out.println(e.getMessage());
		}
		
		if (errorString == null) {
			try {
				
				DBUtils.insertAmbient(conn, ambient);
				
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				errorString = e.getMessage();
			}
		}	
			
		user = new UserAccount(LoginServlet.name);
			
		user.setPassword(LoginServlet.password);
	
		try {
			user.setAmbientID(DBUtils.maxIdAm(conn));
			
		} catch (SQLException e) {
			
			System.out.println(e.getMessage());
		}
		
		if (errorString == null) {
			
			//If user not logged like superUser
			if(LoginServlet.name.equals("admin")) {
				
				user.setPrivilegi(2);
				
				UserAccount admin = null;
				
				System.out.println(LoginServlet.name);
				
				try {
					admin = DBUtils.findUser(conn, "admin");
					
					
				} catch (SQLException e) {
				
					System.out.println(e.getMessage());
				}
			
				admin.setAmbientID(user.getAmbientID());
				
				try {
					DBUtils.insertUser(conn, admin);
					
				} catch (SQLException e) {
					
					System.out.println(e.getMessage());
				}

			}
			else {
				
				try {
					DBUtils.insertUser(conn, user);
					
					System.out.println(LoginServlet.name);
					
				} catch (SQLException e) {
					
					System.out.println(e.getMessage());
					errorString = e.getMessage();
				}
			
				//Insert ambient to user admin
				UserAccount admin = null;
			
				try {
					admin = DBUtils.findUser(conn, "admin");
					
				} catch (SQLException e) {
				
					System.out.println(e.getMessage());
				}
			
				admin.setAmbientID(user.getAmbientID());
				
				try {
					DBUtils.insertUser(conn, admin);
					
				} catch (SQLException e) {
					
					System.out.println(e.getMessage());
				}
			}
		}
		
		// Store infomation to request attribute, before forward to views.
		request.setAttribute("errorString", errorString);
		request.setAttribute("ambient", ambient);
		
		// If error, forward to Create page.
		if (errorString != null) {
			RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/WEB-INF/views/createAmbient.jsp");
			dispatcher.forward(request, response);
		}
		
		// If everything nice.
		// Redirect to the ambient listing page.
		else {
			response.sendRedirect(request.getContextPath() + "/ambientList");
		}	
	}
}
