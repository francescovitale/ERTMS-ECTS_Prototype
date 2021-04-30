package ObjectAbstraction.PMLogic;
import java.util.ArrayList;

public class Trace {
private
	ArrayList<ActivityInstance> AI;
	Description CD;
public
	Trace() {}

	Trace(ArrayList<ActivityInstance> AI_in) {
		AI = AI_in;
	}

	Trace(Trace T){
		AI = new ArrayList<ActivityInstance>(T.getAI());
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

	
	
	
}
