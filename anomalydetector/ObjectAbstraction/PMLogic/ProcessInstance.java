package ObjectAbstraction.PMLogic;
import java.util.ArrayList;

public class ProcessInstance {
private
	int CaseID;
	ArrayList<Event> E;
	ArrayList<ActivityInstance> AI;
	ProcessModel P;
	
public
	ProcessInstance() {
		CaseID = -1;
		E = null;
		AI = null;
		P = null;
	}

	public ProcessInstance(int CaseID_in, ProcessModel P_in) {
		CaseID = CaseID_in;
		P = P_in;
	}
	ProcessInstance(ProcessInstance PI){
		CaseID = PI.getCaseID();
		E = new ArrayList<Event>(PI.getE());
		AI = new ArrayList<ActivityInstance>(PI.getAI());
		P = new ProcessModel(PI.getP());
	}
	public int getCaseID() {
		return CaseID;
	}
	public void setCaseID(int caseID) {
		CaseID = caseID;
	}
	public ArrayList<Event> getE() {
		return E;
	}
	public void setE(ArrayList<Event> e) {
		E = e;
	}
	public ArrayList<ActivityInstance> getAI() {
		return AI;
	}
	public void setAI(ArrayList<ActivityInstance> aI) {
		AI = aI;
	}
	public ProcessModel getP() {
		return P;
	}
	public void setP(ProcessModel p) {
		P = p;
	}
}

