package ObjectAbstraction.DatabaseAccess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import ObjectAbstraction.PMLogic.ProcessInstance;
import ObjectAbstraction.PMLogic.ProcessModel;

public class ProcessInstanceDAO {

	public static ArrayList<ProcessInstance> getProcessInstanceList(Connection Conn, ArrayList<ProcessModel> PMList) throws SQLException {
		
		ArrayList<ProcessInstance> PIList = new ArrayList<ProcessInstance>();
		Statement stmt = Conn.createStatement();
		String query = "SELECT * FROM eventlog.processinstance";
		ResultSet rs = stmt.executeQuery(query);
		while(rs.next()) {
			ProcessInstance PI;
			String ProcessInstancePMName = rs.getString("ProcessModel");
			for(int i=0; i<PMList.size(); i++) {
				if(PMList.get(i).getName().equals(ProcessInstancePMName)) {
					PI = new ProcessInstance(rs.getInt("CaseID"),PMList.get(i));
					PIList.add(PI);
				}
				
			}
		}
		
		return PIList;
	}

	public static void insertProcessInstance(int CaseID, String PMName, ArrayList<ProcessModel> PMList, Connection Conn) throws SQLException {
		ArrayList<ProcessInstance> PIList = getProcessInstanceList(Conn, PMList);
		boolean found = false;
		for(int i=0; i<PIList.size(); i++)
			if(PIList.get(i).getCaseID() == CaseID)
				found = true;
		if(found == false) {
			Statement stmt = Conn.createStatement();
			String query = "INSERT INTO eventlog.processinstance (CaseID,ProcessModel) VALUES ('"+ CaseID  +"','"+PMName+"')";
			stmt.executeUpdate(query);
		}
			
	}
	
	public static void deleteProcessInstance(int CaseID, Connection Conn) throws SQLException {
		Statement stmt = Conn.createStatement();
		String query = "DELETE FROM eventlog.processinstance WHERE CaseID=" + CaseID;
		stmt.executeUpdate(query);
	}

}
