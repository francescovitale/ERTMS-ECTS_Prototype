package ObjectAbstraction.PMLogic;
import java.util.ArrayList;

public class ProcessModel {
private
	String Name;
	ArrayList<Activity> AL;
	ArrayList<ProcessInstance> PI;
	PetriNet P;
	BPMN BPMN_model;
	
public
	ProcessModel() {
		Name = "";
		AL = null;
		PI = null;
		P = null;
		BPMN_model = null;
	}

	public ProcessModel(String Name_in) {
		Name = Name_in;
	}
	ProcessModel(ProcessModel PM_in){
		Name = PM_in.getName();
		AL = new ArrayList<Activity>(PM_in.getAL());
		PI = new ArrayList<ProcessInstance>(PM_in.getPI());
		P = new PetriNet(PM_in.getP());
		BPMN_model = new BPMN(PM_in.getBPMN_model());
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public ArrayList<Activity> getAL() {
		return AL;
	}
	public void setAL(ArrayList<Activity> aL) {
		AL = aL;
	}
	public ArrayList<ProcessInstance> getPI() {
		return PI;
	}
	public void setPI(ArrayList<ProcessInstance> pI) {
		PI = pI;
	}
	public PetriNet getP() {
		return P;
	}
	public void setP(PetriNet p) {
		P = p;
	}

	public BPMN getBPMN_model() {
		return BPMN_model;
	}

	public void setBPMN_model(BPMN bPMN_model) {
		BPMN_model = bPMN_model;
	}
}
