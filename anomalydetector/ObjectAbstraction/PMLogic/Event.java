package ObjectAbstraction.PMLogic;
import java.sql.Timestamp;

public class Event {
private
	int ID;
	int T;
	String Resource;
	ProcessInstance PI;
	ActivityInstance AI;
public
	Event() {
		ID = -1;
		T = -1;
		Resource = "";
		PI = null;
		AI = null;
	}

	public Event(int ID_in, int T_in, String Resource_in, ProcessInstance P_in, ActivityInstance A_in) {
		ID = ID_in;
		T = T_in;
		Resource = Resource_in;
		PI = P_in;
		AI = A_in;
	}
	Event(Event E){
		ID = E.getID();
		T = E.getT();
		Resource = E.getResource();
		PI = new ProcessInstance(E.getPI());
		AI = new ActivityInstance(E.getAI());
		
	}
public int getID() {
	return ID;
}
public void setID(int iD) {
	ID = iD;
}
public int getT() {
	return T;
}
public void setT(int i) {
	T = i;
}
public String getResource() {
	return Resource;
}
public void setResource(String resource) {
	Resource = resource;
}

public ProcessInstance getPI() {
	return PI;
}

public void setPI(ProcessInstance pI) {
	PI = pI;
}

public ActivityInstance getAI() {
	return AI;
}

public void setAI(ActivityInstance aI) {
	AI = aI;
}
	
}
