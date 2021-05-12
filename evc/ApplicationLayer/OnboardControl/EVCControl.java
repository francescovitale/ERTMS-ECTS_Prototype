package ApplicationLayer.OnboardControl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttException;

import Middleware.OnboardCoordination.EVCMQTTClient;

public class EVCControl {
	
	private volatile static EVCControl EVC = null;
	Position P;
	ERTMSLevel L;
	boolean Accepted;
	boolean Open;
	boolean timedOut;
	boolean retry;
	
	private EVCControl() throws MqttException {
		P = new Position();
		L = new ERTMSLevel();
		Accepted = true;
		Open = true;
		timedOut = false;
		retry = false;
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
		EVCMQTTClient EMC = EVCMQTTClient.getInstance("tcp://192.168.1.2:1883", "evc");
		EMC.connect();
		EMC.initializeListener();
	}
	public void startProcedureControl(int CaseID) throws Exception {
		EVCMQTTClient EMC = EVCMQTTClient.getInstance(null, null);
		if(getLevelValue()== 2 || getLevelValue() == 3) {
			
			
			sendEvent("som_sendMAreq_rtm_1", EMC, CaseID);
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
			// The level is less than 2.
			sendEvent("som_checklev_evc_1",EMC,CaseID);
			if(getLevelValue() == 1) {
				sendEvent("som_grantUN_evc_1",EMC, CaseID);
				EMC.publishString("StartDMI", "UN");
			}
			else
			{
				sendEvent("som_grantSR_evc_1",EMC, CaseID);
				EMC.publishString("StartDMI", "SR");
			}
		}
	}
	public void validateDriverID(String DriverID) throws MqttException {
		boolean valid;
		if(DriverID.equals("Francesco1234"))
			valid = true;
		else
			valid = false;
		EVCMQTTClient EMC = EVCMQTTClient.getInstance(null,null);
		// Check the Driver ID
		if(valid == false) { 
			EMC.publishString("DriverIDDMI", "retry");
		}
		else
		{ // Only option possible (for now)
			EMC.publishString("DriverIDDMI", "noretry");
		}
	}
	public void contactRBC(int CaseID) throws Exception {
		// Check the level. For simplicity, we suppose it's 2. Otherwise we would have to 'give up'
		EVCMQTTClient EMC = EVCMQTTClient.getInstance(null, null);
		if(L.getValue() < 2) {
			sendEvent("som_giveup_evc_1",EMC,CaseID);
			EMC.publishString("RBCDMI", "giveup");
		}
		else {
			EMC.publishString("OpenConnRBC", "");
			timedOut = true;
			System.out.println("EVC about to go to sleep..");
			synchronized(this) {
				this.wait(10000);
			}
			
			// If it doesn't get woken up, it assumes there's a network problem between the EVC and the RBC.
			
			// A check should be done on the timeout.
			
			// If the check is positive, check if a retry should be done, otherwise make the DMI timeout
			
			// Code to execute after the thread wakes up.
			
			System.out.println("EVC WOKE UP");
			if(timedOut == true) {
				timedOut = false; // Reset the flag
				if(retry) {
					EMC.publishString("RBCDMI", "retry");
				}
				else { // The DMI times out.
					try {
						sendEvent("som_end",EMC,CaseID);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	public void validatePositionLevel(int CaseID) throws Exception {
		// The controller must retrieve the stored level and position.
		
		// Try to get the position from the sensor OnboardToBalises
		// If no communication can be instantiated with the sensor, END the communication:
		
		EVCMQTTClient EMC = EVCMQTTClient.getInstance(null, null);
		EMC.publishString("retrievepos", "");
		timedOut = true;
		System.out.println("EVC About to go to sleep...");
		synchronized(this) {
			wait(10000);
		}
		if(timedOut == true) {
			/* if(no comm is possible with OnboardToBalises)
		     transmit som_end to the logger
		     don't send any message to DMI: make it timeout. */
			try {
				sendEvent("som_end",EMC,CaseID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			timedOut = false;
		}
		else {
		
			// Suppose the position retrieved from the sensor isn't valid.
			if(!L.isValid()) {
				// Check the local stored position.
				checkPosition(CaseID);
			}
			// Now the level must be checked. At the end, the event will be sent
			
			if(checkLevel(CaseID)) {
				EMC.publishString("LevelDMI", "ok");
			}
			else
				EMC.publishString("LevelDMI", "notok");
		}
		
	}
	public void ackManagement(int CaseID) throws Exception {
		// The EVC need to register that the train is in a new mode.
		EVCMQTTClient EMC = EVCMQTTClient.getInstance(null, null);
		sendEvent("som_chmod_evc_2",EMC,CaseID);
		sendEvent("som_end",EMC,CaseID);
		EMC.publishString("AckDMI", "");
	}
	public void RBCRoutineControl(int receivedCaseId) throws Exception {
		// Check if the position is valid. Remember that we will assume it IS valid.
		EVCMQTTClient EMC = EVCMQTTClient.getInstance(null, null);
		if(P.isValid()) {
			
			EMC.publishString("ElabPosRBC", String.valueOf(receivedCaseId)); // The whole Position variable should be sent.
			timedOut = true;
			synchronized(this) {
				this.wait(10000);
			}
			System.out.println("EVC WOKE UP");
			// If it doesn't get woken up, it assumes there's a network problem between the EVC and the RBC.
			
			// A check should be done on the timeout. If the check is positive, the nothing is done, which
			// in turn makes the DMI timeout, and will prompt the driver with an error.

			// Code to execute after the thread wakes up.
			
			// if(woken up..)
			if(timedOut == true) {
				timedOut = false;
				sendEvent("som_end",EMC,receivedCaseId);
				// Let the DMI timeout
			}
			else {
				// Store the accepted flag
				sendEvent("som_storeacc_evc_1",EMC, receivedCaseId);
				EMC.publishString("RBCRoutineDMI", "");
			}
		}
		else {
			// to implement..
		}
		
	}
	public void checkRBCSession(int receivedCaseID) throws Exception {
		EVCMQTTClient EMC = EVCMQTTClient.getInstance(null, null);
		EMC.publishString("CheckSessRBC", "");
		timedOut = true;
		System.out.println("EVC About to go to sleep");
		synchronized(this) {
			this.wait(10000);
		}
		System.out.println("EVC woke up");
		// If it doesn't get woken up, it assumes there's a network problem between the EVC and the RBC.
		
		// A check should be done on the timeout. If the check is positive, the nothing is done, which
		// in turn makes the DMI timeout, and will prompt the driver with an error.

		// Code to execute after the thread wakes up.
		if(timedOut == true) {
			timedOut = false;
			sendEvent("som_end",EMC,receivedCaseID);
			// Let the DMI timeout
		}
		else {
			System.out.println("true:"+String.valueOf(getLevelValue()));
			EMC.publishString("CheckLevSessDMI", "true:"+String.valueOf(getLevelValue()));
		}
	}
	public void checkPosition(int CaseID) throws MqttException, Exception {
		// If the position can't be obtained from the sensor, try to retrieve the one that's stored.
		
		// Check that the position is valid. Could be done by checking the coordinates and the last timestamp
		// Retrievable with Position.getTimestamp() and Position.getCoordinates.
				
		// Set that the position is valid or not. Assume that the position is valid. Therefore:
				
		P.setValid(true);
				
		
		
		
		
	}
	public boolean checkLevel(int CaseID) throws MqttException, Exception {
		// Recover the stored level. Check whether it is valid or not, by looking at the Timestamp.
		// Assume that the level is valid. Therefore:
						
		L.setValid(true);
		sendEvent("som_validate_evc_1",EVCMQTTClient.getInstance(null, null),CaseID);
		
		return L.isValid();
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
	public Position getP() {
		return P;
	}
	public void setPCoordinates(float lat, float lon) {
		float[] coordinates = new float[2];
		coordinates[0] = lat;
		coordinates[1] = lon;
		P.setCoordinates(coordinates);
	}
	public void setPLastTimestamp(int ts) {
		P.setLastTimestamp(ts);
	}
	public boolean isTimedOut() {
		return timedOut;
	}
	public void setTimedOut(boolean timedOut) {
		this.timedOut = timedOut;
	}
	public void sendEvent(String activity,EVCMQTTClient EMC, int CaseID) throws Exception {
		String TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		Message M = new Message(TS,"evc",0,0,"","",false,activity,CaseID);
		EMC.publishMessage("StartOfMissionEvent", M);
	}
	public boolean isRetry() {
		return retry;
	}
	public void setRetry(boolean retry) {
		this.retry = retry;
	}
}
