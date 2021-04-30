package ApplicationLayer.OnboardControl;

import org.eclipse.paho.client.mqttv3.MqttException;

import Middleware.OnboardCoordination.EVCMQTTClient;

public class EVCControl {
	
	private volatile static EVCControl EVC = null;
	Position P;
	ERTMSLevel L;
	boolean Accepted;
	boolean Open;
	
	private EVCControl() throws MqttException {
		P = new Position();
		L = new ERTMSLevel();
		Accepted = true;
		Open = true;
	}
	public static EVCControl getInstance() throws MqttException {
		if(EVC==null) {
			synchronized(EVCControl.class) {
				if(EVC==null) {
					EVC = new EVCControl();
				}
			}
		}
		return EVC;
	}
	public void initializeClient() throws MqttException {
		EVCMQTTClient EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
		EMC.connect();
		EMC.initializeListener();
		
	}
	public void startProcedureControl(int CaseID) throws MqttException, InterruptedException {
		if(getLevelValue()== 2 || getLevelValue() == 3) {
			// Only option possible
			EVCMQTTClient EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
			Message M = new Message(13,"evc",0,0,"","",false,"som_sendMAreq_rtm_1",CaseID);
			EMC.publishMessage("StartOfMissionEvent", M);
			EMC.publishString("MAManagementRBC", String.valueOf(CaseID));
			synchronized(this) {
				this.wait();
			}
			// If it doesn't get woken up, it assumes there's a network problem between the EVC and the RBC.
			
			// A check should be done on the timeout. If the check is positive, the nothing is done, which
			// in turn makes the DMI timeout, and will prompt the driver with an error.

			// Code to execute after the thread wakes up.
			
			System.out.println("EVC WOKE UP");
		}
		else {
			// to implement...
		}
	}
	public void validateDriverID(String DriverID) throws MqttException {
		boolean valid = true;
		EVCMQTTClient EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
		// Check the Driver ID
		if(valid == false) {
			Message M = new Message(0,"evc",0,0,"","",true,"",0);
			EMC.publishMessage("DriverIDDMI", M);
		}
		else
		{
			Message M = new Message(0,"evc",0,0,"","",false,"",0);
			EMC.publishMessage("DriverIDDMI", M);
		}
	}
	
	public void contactRBC() throws MqttException, InterruptedException {
		// Check the level. For simplicity, we suppose it's 2. Otherwise we would have to 'give up'
		EVCMQTTClient EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
		EMC.publishString("OpenConnRBC", "");
		synchronized(this) {
			this.wait();
		}
		// If it doesn't get woken up, it assumes there's a network problem between the EVC and the RBC.
		
		// A check should be done on the timeout. If the check is positive, the nothing is done, which
		// in turn makes the DMI timeout, and will prompt the driver with an error.

		// Code to execute after the thread wakes up.
		
		System.out.println("EVC WOKE UP");
		
	}
	public void validateTrainData() {}
	public void sendMARequest() {}
	public void validatePositionLevel(int CaseID) throws MqttException {
		// The controller must retrieve the stored level and position.
		
		// Try to get the position from the sensor OnboardToBalises
		// If no communication can be instantiated with the sensor, END the communication:
		
		/* if(no comm is possible with OnboardToBalises)
		     transmit som_end to the logger
		     don't send any message to DMI: make it timeout. */
		
		// If the position can't be obtained from the sensor, try to retrieve the one that's stored.
		
		// Check that the position is valid. Could be done by checking the coordinates and the last timestamp
		// Retrievable with Position.getTimestamp() and Position.getCoordinates.
		
		// Set that the position is valid or not. Assume that the position is valid. Therefore:
		
		P.setValid(true);
		
		// Recover the stored level. Check whether it is valid or not, by looking at the Timestamp.
		// Assume that the level is valid. Therefore:
		
		L.setValid(true);
		System.out.println("Validating");
		
		EVCMQTTClient EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
		EMC.publishString("LevelDMI", "ok");
		
		
	}
	
	public void ackManagement(int CaseID) throws MqttException {
		// The EVC need to register that the train is in a new mode.
		EVCMQTTClient EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
		Message msg = new Message(18,"evc",0,0,"","",true,"som_chmod_evc_2",CaseID);
		EMC.publishMessage("StartOfMissionEvent", msg);
		msg = new Message(19,"evc",0,0,"","",true,"som_end",CaseID);
		EMC.publishMessage("StartOfMissionEvent", msg);
		EMC.publishString("AckDMI", "");
	}
	
	public void RBCRoutineControl(int receivedCaseId) throws MqttException, InterruptedException {
		// Check if the position is valid. Remember that we will assume it IS valid.
		if(P.isValid()) {
			EVCMQTTClient EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
			EMC.publishString("ElabPosRBC", String.valueOf(receivedCaseId)); // The whole Position variable should be sent.
			synchronized(this) {
				this.wait();
			}
			// If it doesn't get woken up, it assumes there's a network problem between the EVC and the RBC.
			
			// A check should be done on the timeout. If the check is positive, the nothing is done, which
			// in turn makes the DMI timeout, and will prompt the driver with an error.

			// Code to execute after the thread wakes up.
			
			// if(woken up..)
			if(true) {
				Accepted = true;
				Message msg = new Message(8,"evc",0,0,"","",true,"som_storeacc_evc_1",receivedCaseId);
				EMC.publishMessage("StartOfMissionEvent", msg);
				EMC.publishString("RBCRoutineDMI", "");
			}
			else {
				
			}
		}
		else {
			// to implement..
		}
		
	}
	
	public void checkRBCSession() throws MqttException, InterruptedException {
		EVCMQTTClient EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
		EMC.publishString("CheckSessRBC", "");
		System.out.println("EVC About to go to sleep");
		synchronized(this) {
			this.wait();
		}
		System.out.println("EVC woke up");
		// If it doesn't get woken up, it assumes there's a network problem between the EVC and the RBC.
		
		// A check should be done on the timeout. If the check is positive, the nothing is done, which
		// in turn makes the DMI timeout, and will prompt the driver with an error.

		// Code to execute after the thread wakes up.
		if(Open == true) { // only possible option
			System.out.println("true:"+String.valueOf(getLevelValue()));
			EMC.publishString("CheckLevSessDMI", "true:"+String.valueOf(getLevelValue()));
		}
		else { 
			// to implement
			}
	}
	public int getLevelValue() {
		return L.getValue();
	}
	public boolean getLevelValidity() {
		return L.isValid();
	}
	
	public static void main(String[] args) throws MqttException {
		EVCControl EVC = EVCControl.getInstance();
		EVC.initializeClient();
	}
	public boolean isOpen() {
		return Open;
	}
	public void setOpen(boolean open) {
		Open = open;
	}
	
}
