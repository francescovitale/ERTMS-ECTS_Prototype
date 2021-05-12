package ObjectAbstraction.DatabaseAccess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import ObjectAbstraction.PMLogic.ActivityInstance;
import ObjectAbstraction.PMLogic.Event;
import ObjectAbstraction.PMLogic.ProcessInstance;

public class EventDAO {

	public static ArrayList<Event> getEventList(ArrayList<ProcessInstance> PIList, ArrayList<ActivityInstance> AIList,
			Connection Conn) throws SQLException {
		
		ArrayList<Event> EList = new ArrayList<Event>();
		
		Statement stmt = Conn.createStatement();
		String query = "SELECT ID, round(unix_timestamp(Timestamp)*1000) as Timestamp, Resource, ActInst, CaseID FROM eventlog.event";
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next()) {
			int EventActInst = rs.getInt("ActInst");
			int Case = rs.getInt("CaseID");
			for(int i=0;i<PIList.size();i++) {
				if(PIList.get(i).getCaseID() == Case){
					for(int j=0; j<AIList.size(); j++) {
						if(AIList.get(j).getID() == EventActInst) {
							EList.add(new Event(rs.getInt("ID"),rs.getLong("Timestamp"),rs.getString("Resource"),PIList.get(i),AIList.get(j)));
						}
					}
				}
			}
		}
		
		
		return EList;
	}

	public static void insertEvent(String Timestamp, String resource, int CaseID, int actID, Connection Conn) throws SQLException {
		Statement stmt = Conn.createStatement();
		String query = "INSERT INTO eventlog.event (Timestamp,Resource,ActInst,CaseID) VALUES ('"+ Timestamp +"','"+ resource +"','"+actID+"','"+CaseID+"')";
		stmt.executeUpdate(query);
		
	}
	
	public static void deleteEvent(int ActInst, Connection Conn) throws SQLException{
		Statement stmt = Conn.createStatement();
		String query = "DELETE FROM eventlog.event WHERE ActInst=" + ActInst;
		stmt.executeUpdate(query);
		
	}

}



