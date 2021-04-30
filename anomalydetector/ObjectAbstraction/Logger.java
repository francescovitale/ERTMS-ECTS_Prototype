package ObjectAbstraction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import ObjectAbstraction.DatabaseAccess.DBFacade;
import ObjectAbstraction.PMLogic.Checker;
import ObjectAbstraction.PMLogic.Trace;

public class Logger {
	public Logger() {}
	
	public void logEvent(int Timestamp, int CaseID, String Resource, String ActInst, String ProcessName) {
		DBFacade DBF = new DBFacade();
		try {
			Checker C = new Checker();
			ArrayList<Trace> ReturnedTraces = null;
			DBF.insertEvent(Timestamp, Resource, CaseID, ActInst, ProcessName);
			try {
				ReturnedTraces = C.onlineConformanceChecking();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(ReturnedTraces != null) {
				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("hey");
		}
	}
	

}
