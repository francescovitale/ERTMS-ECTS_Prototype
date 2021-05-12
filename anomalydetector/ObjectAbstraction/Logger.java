package ObjectAbstraction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import ObjectAbstraction.DatabaseAccess.DBFacade;
import ObjectAbstraction.PMLogic.Activity;
import ObjectAbstraction.PMLogic.ActivityInstance;
import ObjectAbstraction.PMLogic.Checker;
import ObjectAbstraction.PMLogic.Description;
import ObjectAbstraction.PMLogic.ProcessInstance;
import ObjectAbstraction.PMLogic.ProcessModel;
import ObjectAbstraction.PMLogic.Trace;

public class Logger {
	public Logger() {}
	
	public void logEvent(String Timestamp, int CaseID, String Resource, String ActInst, String ProcessName) throws SQLException {
		DBFacade DBF = new DBFacade();
		try {
			Checker C = new Checker();
			ArrayList<Trace> ReturnedTraces;
			DBF.insertEvent(Timestamp, Resource, CaseID, ActInst, ProcessName);
			try {
				switch(ActInst) { // The algorithms checks for the end transitions of known processes.
				case "som_end":
					ReturnedTraces = C.onlineConformanceChecking();
					break;
				default:
					System.out.println("The activity isn't the end transition");
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("hey");
		}
		DBF.closeConnection();
	}
	
	public ArrayList<Trace> getAnomalousTraces() throws SQLException {
		DBFacade DBF = new DBFacade();
		
		ArrayList<ProcessModel> PMList = DBF.getProcessList();
		ArrayList<Activity> AList = DBF.getActivityList(PMList);
		ArrayList<ProcessInstance> PIList = DBF.getProcessInstanceList(PMList);
		
		ArrayList<ActivityInstance> AIList = DBF.getActivityInstanceList(AList, PIList);
		
		ArrayList<Description> D = DBF.getDescriptionList();
		ArrayList<Trace> ATList = DBF.getAnomalousTraceList(AIList, D);
		
		DBF.closeConnection();
		
		
		return ATList;
	}
}
