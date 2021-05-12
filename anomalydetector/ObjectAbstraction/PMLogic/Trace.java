package ObjectAbstraction.PMLogic;
import java.util.ArrayList;

public class Trace {
private
	int ID;
	ArrayList<ActivityInstance> AI;
	Description CD;
public
	Trace() {
		ID = -1;
		AI = new ArrayList<ActivityInstance>();
		CD = null;
	}

	Trace(ArrayList<ActivityInstance> AI_in) {
		ID = -1;
		AI = AI_in;
		CD = null;
	}
	
	public Trace(int ID_in){
		ID = ID_in;
		AI = new ArrayList<ActivityInstance>();
		CD = null;
	}
	public Trace(int ID_in, ArrayList<ActivityInstance> AI_in, Description D){
		ID = ID_in;
		AI = AI_in;
		CD = D;
	}

	Trace(Trace T){
		ID = T.getID();
		AI = new ArrayList<ActivityInstance>(T.getAI());
		CD = T.getCD();
	}

	public ArrayList<ActivityInstance> getAI() {
		return AI;
	}

	public void setAI(ArrayList<ActivityInstance> aI) {
		AI = aI;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((AI == null) ? 0 : AI.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object T) {
		if(T == null)
			return false;
		if(((Trace) T).getAI().size() != AI.size())
			return false;
		boolean equal = true;
		for (int i = 0; i<((Trace) T).getAI().size(); i++) {
			if(!((Trace) T).getAI().get(i).getA().getName().equals(AI.get(i).getA().getName()))
				equal = false;
		}
		return equal;
						
		
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public Description getCD() {
		return CD;
	}

	public void setCD(Description cD) {
		CD = cD;
	}

	
	
	
}
