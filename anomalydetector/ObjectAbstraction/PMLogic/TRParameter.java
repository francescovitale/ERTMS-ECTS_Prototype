package ObjectAbstraction.PMLogic;

public class TRParameter {
	String Resource;
	float Value;
	ProcessModel PM;
	int Counter;
	
	public TRParameter(String R, float V, ProcessModel PMin) {
		Resource = R;
		Value = V;
		PM = PMin;
		Counter = 0;
		
	}

	public String getResource() {
		return Resource;
	}

	public void setResource(String resource) {
		Resource = resource;
	}

	public float getValue() {
		return Value;
	}

	public void setValue(float value) {
		Value = value;
	}

	public int getCounter() {
		return Counter;
	}

	public void setCounter(int counter) {
		Counter = counter;
	}
	
	
}
