package Middleware.Controller;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ad.AnomalyDetectorController;

public class DiagnosisServlet extends HttpServlet {
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		
		String AnomalousTraces = AnomalyDetectorController.getInstance().getAnomalousTraces();
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(AnomalousTraces);
		response.getWriter().flush();
		
	}
}
