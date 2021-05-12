package ApplicationLayer.OnboardControl;

public class Position {
	float[] Coordinates;
	boolean Valid;
	int LastTimestamp;
	
	public Position() {
		Coordinates = new float[2]; // lat, lon
		Valid = true;
		LastTimestamp = 0;
	}

	public float[] getCoordinates() {
		return Coordinates;
	}

	public void setCoordinates(float[] coordinates) {
		Coordinates = coordinates;
	}

	public boolean isValid() {
		return Valid;
	}

	public void setValid(boolean valid) {
		Valid = valid;
	}

	public int getLastTimestamp() {
		return LastTimestamp;
	}

	public void setLastTimestamp(int lastTimestamp) {
		LastTimestamp = lastTimestamp;
	}
	
	
}
