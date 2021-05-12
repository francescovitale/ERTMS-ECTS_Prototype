package ApplicationLayer.OnboardControl;

public class ERTMSLevel {
	int value;
	int Timestamp;
	boolean Valid;
	
	ERTMSLevel(){
		value = 2;
		Timestamp = 0;
		Valid = false;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getTimestamp() {
		return Timestamp;
	}

	public void setTimestamp(int timestamp) {
		Timestamp = timestamp;
	}

	public boolean isValid() {
		return Valid;
	}

	public void setValid(boolean valid) {
		Valid = valid;
	}
	
}
