package ObjectAbstraction.PMLogic;
import java.util.ArrayList;

public class Description {
private
	float Fitness;
	ArrayList<Trace> T;
	
public
	Description() {
		Fitness = (float) 0.0;
		T = new ArrayList<Trace>();
	}

	Description(float Fitness_in, ArrayList<Trace> T_in) {
		Fitness = Fitness_in;
		T = new ArrayList<Trace>(T_in);
	}
	Description(Description CD){
		Fitness = CD.getFitness();
		T = CD.getT();
	}

	public float getFitness() {
		return Fitness;
	}
	public void setFitness(float fitness) {
		Fitness = fitness;
	}
	public ArrayList<Trace> getT() {
		return T;
	}
	public void setT(ArrayList<Trace> t) {
		T = t;
	}
}
