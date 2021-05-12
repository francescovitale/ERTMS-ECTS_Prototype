package ApplicationLayer.OnboardControl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttException;

import ApplicationLayer.DisplayBoundary;
import Middleware.OnboardCoordination.DMIMQTTClient;

public class InputControl extends EventPromptControl {

	private volatile static InputControl IC = null;
	boolean timedOut;
	
	
	private InputControl() throws MqttException {
		timedOut = false;
	}
	// This class is a singleton.
	public static InputControl getInstance() throws MqttException {
		if(IC==null) {
			synchronized(InputControl.class) {
				if(IC==null) {
					IC = new InputControl();
				}
			}
		}
		return IC;
	}
	// This method manages the insertion of the Driver ID and the retrieval of the elaboration from the EVC
	public boolean insertDriverIDControl(String DriverID) throws Exception {
		DMIMQTTClient.getInstance(null,null).publishString("DriverIDEVC", DriverID);
		sendEvent("som_enterid_dmi_1", DMIMQTTClient.getInstance(null,null)); // The event 'som_enterid_dmi_1' is sent to the Anomaly Detector
		System.out.println("IC about to go to sleep..");
		timedOut = true;
		synchronized(this)
		{
		    this.wait(20000); // Wait for the listener to wake up this thread.
		}
		
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		System.out.println("IC WOKE UP");
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.
		if(timedOut == true) {
			timedOut = false;
			return true;
		}
		else {
			return false;
		}
		
	};
	// When the user inserts Train Data, this method is supposed to control for its validity.
	public void insertTrainDataControl(int TrainData) throws Exception {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance(null,null);
		sendEvent("som_inserttraindata_dmi_1",DMC);
		
		sendEvent("som_checkrbcsess_rtm_1",DMC); // This is always supposed to be traced, even when the ERTMS level is not 2 or higher.
	};
	// This method manages the mode selection made by the user. 
	public void selectModeControl() throws Exception {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance(null,null);
		DMC.publishString("CheckLevSessEVC", DisplayBoundary.getInstance().getCaseID().toString());
		timedOut = true;
		synchronized(this) {
			this.wait(25000);
		}
		System.out.println("IC WOKE UP");
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		if(timedOut == true) {
			timedOut = false;
			sendEvent("som_end",DMC);
		}
		else {
			sendEvent("som_driversel_dmi_1",DMIMQTTClient.getInstance(null,null));
		}
		
		
	};
	
	
	@Override
	public
	void sendEvent(String activity,DMIMQTTClient DMC) throws Exception {
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
