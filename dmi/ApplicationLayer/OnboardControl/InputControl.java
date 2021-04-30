package ApplicationLayer.OnboardControl;

import org.eclipse.paho.client.mqttv3.MqttException;

import ApplicationLayer.DisplayBoundary;
import Middleware.OnboardCoordination.DMIMQTTClient;

public class InputControl extends EventPromptControl {

	private volatile static InputControl IC = null;
	
	private InputControl() throws MqttException {
	}
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
	public void insertDriverIDControl(String DriverID) throws Exception {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883", "dmi");
		
		// check that the inserted DriverID only has digits.
		
		
		DMC.publishString("DriverIDEVC", DriverID);
		sendEvent("StartOfMissionEvent", DMC);
		System.out.println("IC about to go to sleep..");
		synchronized(this)
		{
		    this.wait();// wait for the listener to wake up this thread.
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		
		System.out.println("IC WOKE UP");
	};
	
	public void insertTrainDataControl(int TrainData) throws MqttException, InterruptedException {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883", "dmi");
		Message M = new Message(10,"evc",0,0,"","",false,"som_inserttraindata_dmi_1",DisplayBoundary.getCaseID());
		DMC.publishMessage("StartOfMissionEvent", M);
		M = new Message(11,"evc",0,0,"","",false,"som_checkrbcsess_rtm_1",DisplayBoundary.getCaseID());
		DMC.publishMessage("StartOfMissionEvent", M);
	};
	public void selectModeControl() throws MqttException, InterruptedException {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883", "dmi");
		DMC.publishString("CheckLevSessEVC", "");
		synchronized(this) {
			this.wait();
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		
		System.out.println("IC WOKE UP");
		
		Message M = new Message(9,"dmi",0,0,"","",false,"som_driversel_dmi_1",DisplayBoundary.getCaseID());
		DMC.publishMessage("StartOfMissionEvent", M);
	};
	public void ackModeControl() {};
	
	
	@Override
	void sendEvent(String op, DMIMQTTClient DMC) throws Exception {
		switch(op) {
			case "StartOfMissionEvent":
				Message M = new Message(1,"dmi",0,0,"","",false,"som_enterid_dmi_1",DisplayBoundary.getCaseID());
				DMC.publishMessage("StartOfMissionEvent", M);
				break;
			default: throw new Exception();
		}
		
	}
	
	

}
