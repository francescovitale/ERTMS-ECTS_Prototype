package ApplicationLayer.OnboardControl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttException;

import ApplicationLayer.DisplayBoundary;
import Middleware.OnboardCoordination.DMIMQTTClient;

public class MissionControl extends EventPromptControl {

	private volatile static MissionControl MC = null;
	boolean timedOut;
	
	private MissionControl() throws MqttException {
		timedOut = false;
	}
	// This class is a singleton.
	public static MissionControl getInstance() throws MqttException {
		if(MC==null) {
			synchronized(MissionControl.class) {
				if(MC==null) {
					MC = new MissionControl();
				}
			}
		}
		return MC;
	}
	// This method manages the initialization of the DMI.
	public void initializeSOM() throws Exception {
		try {
			DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://192.168.1.2:1883", "dmi");
			DMC.connect();
			DMC.initializeListener();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// This method manages the validation of the level
	public void validateLevel() throws Exception {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance(null, null);
		DMC.publishString("LevelEVC", DisplayBoundary.getInstance().getCaseID().toString());
		System.out.println("MC about to go to sleep..");
		timedOut = true;
		synchronized(this) {
			this.wait(25000);
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.
		// Code to execute after the thread wakes up.
		
		System.out.println("MC WOKE UP");
		if(timedOut == true) {
			timedOut = false; // Reset the flag
			throw new Exception();
		}

		
	}
	// This method manages the contact with the RBC through the EVC
	public void contactRBCControl(String insertedOption) throws Exception {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance(null, null);
		DMC.publishString("RBCEVC", insertedOption + ":" + DisplayBoundary.getInstance().getCaseID().toString());
		System.out.println("MC about to go to sleep..");
		timedOut = true;
		synchronized(this) {
			this.wait(25000);
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.
		// Code to execute after the thread wakes up.
		
		System.out.println("MC WOKE UP");
		
		if(timedOut == true) {
			timedOut = false; // Reset the flag
			throw new Exception();
		}

		
	}
	// This method manages the 'RBC Routine', which is the routine executed by the EVC when the connection is made with the RBC, and the position and acceptance of the train is managed
	public void RBCRoutine() throws Exception {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance(null, null);
		DMC.publishString("RBCRoutineEVC", DisplayBoundary.getInstance().getCaseID().toString());
		timedOut = true;
		synchronized(this) {
			this.wait(25000);
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		
		System.out.println("MC WOKE UP");
		
		if(timedOut == true) {
			timedOut = false;
			
			throw new Exception();
		}
	}
	public void startControl() throws Exception {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance(null, null);
		sendEvent("som_selstart_dmi_1", DMC);
		DMC.publishString("StartEVC", DisplayBoundary.getInstance().getCaseID().toString());
		timedOut = true;
		synchronized(this) {
			this.wait(25000);
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		
		System.out.println("MC WOKE UP");
		if(timedOut == true) {
			timedOut = false;
			sendEvent("som_end",DMC); // The procedure is cut.
		}
		else {
			if(DisplayBoundary.getInstance().getObtainedLevel()>=2) {
				sendEvent("som_awaitack_dmi_1",DMC);
			}
			else {
				sendEvent("som_awaitack_dmi_2", DMC);
			}
		}
	}
	public void ackControl() throws Exception {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance(null, null);
		DMC.publishString("AckEVC", String.valueOf(DisplayBoundary.getInstance().getCaseID()));
		System.out.println("MC About to go to sleep..");
		timedOut = true;
		synchronized(this) {
			this.wait(25000);
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		System.out.println("MC WOKE UP");
		if(timedOut == true) {
			timedOut = false;
			sendEvent("som_end",DMC); // The procedure is cut.
		}
	}
	
	
	@Override
	public void sendEvent(String activity,DMIMQTTClient DMC) throws Exception {
		String TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		Message M = new Message(TS,"dmi",0,0,"","",false,activity,DisplayBoundary.getInstance().getCaseID());
		DMC.publishMessage("StartOfMissionEvent", M);
	}
	public boolean isTimedOut() {
		return timedOut;
	}
	public void setTimedOut(boolean timedOut) {
		this.timedOut = timedOut;
	}

}
