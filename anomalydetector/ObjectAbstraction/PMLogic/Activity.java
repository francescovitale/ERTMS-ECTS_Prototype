package ObjectAbstraction.PMLogic;

import java.util.ArrayList;

public class Activity {
private
	String Name;
	ArrayList<ActivityInstance> AI;
	ProcessModel PM;
public
	Activity() {
		Name = "";
		PM = null;
		AI = null;
	}
	public Activity(String N, ProcessModel P_in) {
		Name = N;
		PM = P_in;
		AI = null;
	}
	Activity(Activity A_in){
		Name = A_in.getName();
		PM = new ProcessModel(A_in.getPM());
		AI = new ArrayList<ActivityInstance>(A_in.getAI());
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public ArrayList<ActivityInstance> getAI() {
		return AI;
	}
	public void setAI(ArrayList<ActivityInstance> aI) {
		AI = aI;
	}
	public ProcessModel getPM() {
		return PM;
	}
	public void setPM(ProcessModel pM) {
		PM = pM;
	}
}
