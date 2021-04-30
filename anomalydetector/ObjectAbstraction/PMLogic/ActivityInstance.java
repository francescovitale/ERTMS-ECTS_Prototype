package ObjectAbstraction.PMLogic;

public class ActivityInstance {
private
	int ID;
	ProcessInstance PI;
	Event E;
	Activity A;

public
	ActivityInstance(ProcessInstance processInstance, Activity activity) {
		ID = -1;
		PI = null;
		E = null;
		A = null;
	}

	public ActivityInstance(int ID_in,ProcessInstance PI_in, Activity A_in) {
		ID = ID_in;
		PI = PI_in;
		A = A_in;
		E = null;
	}
	ActivityInstance(ActivityInstance AI){
		PI = new ProcessInstance(AI.getPI());
		A = new Activity(AI.getA());
		E = new Event(AI.getE());
		ID = AI.getID();
	}
	
	public ProcessInstance getPI() {
		return PI;
	}
	public void setPI(ProcessInstance pI) {
		PI = pI;
	}
	public Event getE() {
		return E;
	}
	public void setE(Event e) {
		E = e;
	}
	public Activity getA() {
		return A;
	}
	public void setA(Activity a) {
		A = a;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}
}
