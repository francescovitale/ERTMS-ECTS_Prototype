package ObjectAbstraction.DatabaseAccess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import ObjectAbstraction.PMLogic.Activity;
import ObjectAbstraction.PMLogic.ActivityInstance;
import ObjectAbstraction.PMLogic.ProcessInstance;

public class ActivityInstanceDAO {

	public static ArrayList<ActivityInstance> getActivityInstanceList(ArrayList<Activity> AList,
		ArrayList<ProcessInstance> PIList, Connection Conn) throws SQLException {
		ArrayList<ActivityInstance> AIList = new ArrayList<ActivityInstance>();
		Statement stmt = Conn.createStatement();
		String query = "SELECT * FROM eventlog.activityinstance";
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next()) {
			int AICase = rs.getInt("CaseID");
			String AIActivity = rs.getString("Act");
			for(int i=0; i<AList.size(); i++) {
				if(AList.get(i).getName().equals(AIActivity)) {
					for(int j=0; j<PIList.size(); j++) {
						if(PIList.get(j).getCaseID() == AICase) {
							AIList.add(new ActivityInstance(rs.getInt("ID"), PIList.get(j),AList.get(i)));
						}
					}
				}
			}
		}
		
		return AIList;
	}

	public static void insertActivityInstance(String AIAct, int CaseID, Connection Conn) throws SQLException {
		Statement stmt = Conn.createStatement();
		String query = "INSERT INTO eventlog.activityinstance (CaseID,Act) VALUES ('"+ CaseID  +"','"+AIAct+"')";
		stmt.executeUpdate(query);
		
		
	}

}
