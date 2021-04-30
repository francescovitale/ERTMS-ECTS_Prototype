package ApplicationLogic;

public class Position {
	int[] Coordinates;
	boolean Valid;
	int LastTimestamp;
	
	public Position() {
		Coordinates = new int[3];
		Valid = true;
		LastTimestamp = 0;
	}

	public int[] getCoordinates() {
		return Coordinates;
	}

	public void setCoordinates(int[] coordinates) {
		Coordinates = coordinates;
	}

	public boolean isValid() {
		return Valid;
	}

	public void setValid(boolean valid) {
		Valid = valid;
	}
	
	
}
