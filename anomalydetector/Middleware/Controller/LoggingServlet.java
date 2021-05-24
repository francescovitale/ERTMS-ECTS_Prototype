package Middleware.Controller;

import java.io.IOException;
import java.util.Base64;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import ad.AnomalyDetectorController;
import ad.Message;

public class LoggingServlet extends HttpServlet {
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		String postString = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		System.out.println(postString);
		Message msg = deserializeMessage(postString);
		System.out.println(msg.isAware());
		System.out.println("Request received");
		AnomalyDetectorController.getInstance().logEvent(msg, "StartOfMission");
		
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		System.out.println("Request received");
	
		
	}
	
	public Message deserializeMessage(String postString) {
		
		Message DeserializedMsg;
		String [] Parts = postString.split("\\!");
		// Timestamp:Resource:Level:TrainData:Mode:TerminalData:Retry:Activity:CaseID:Aware
		DeserializedMsg = new Message(Parts[0],Parts[1],(int)Integer.parseInt(Parts[2]),(int)Integer.parseInt(Parts[3]),Parts[4],Parts[5],Boolean.getBoolean(Parts[6]),Parts[7],(int)Integer.parseInt(Parts[8]),Boolean.valueOf(Parts[9]));
		return DeserializedMsg;
	}
}
