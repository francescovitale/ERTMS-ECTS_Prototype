package ApplicationLayer.OnboardControl;

import org.eclipse.paho.client.mqttv3.MqttException;

import ApplicationLayer.DisplayBoundary;
import Middleware.OnboardCoordination.DMIMQTTClient;

public class MissionControl extends EventPromptControl {

	private volatile static MissionControl MC = null;
	
	private MissionControl() throws MqttException {
	}
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
	
	public void initializeSOM() throws Exception {
		
		try {
			DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883", "dmi");
			DMC.connect();
			DMC.initializeListener();
			sendEvent("StartOfMissionEvent",DMC);
			
			
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void validateLevel() throws MqttException, InterruptedException {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883", "dmi");
		DMC.publishString("LevelEVC", DisplayBoundary.getCaseID().toString());
		synchronized(this) {
			this.wait();
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		
		System.out.println("MC WOKE UP");
	}
	public void contactRBCControl() throws MqttException, InterruptedException {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883", "dmi");
		DMC.publishString("RBCEVC", DisplayBoundary.getCaseID().toString());
		synchronized(this) {
			this.wait();
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		
		System.out.println("MC WOKE UP");
	}
	public void RBCRoutine() throws MqttException, InterruptedException {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883", "dmi");
		DMC.publishString("RBCRoutineEVC", DisplayBoundary.getCaseID().toString());
		synchronized(this) {
			this.wait();
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		
		System.out.println("MC WOKE UP");
	}
	public void giveUpControl() {}
	public void ackTrainDataControl() {}
	public void startControl() throws MqttException, InterruptedException {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883", "dmi");
		Message M = new Message(12,"dmi",0,0,"","",false,"som_selstart_dmi_1",DisplayBoundary.getCaseID());
		DMC.publishMessage("StartOfMissionEvent", M);
		DMC.publishString("StartEVC", DisplayBoundary.getCaseID().toString());
		synchronized(this) {
			this.wait();
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		
		System.out.println("MC WOKE UP");
		M = new Message(17,"dmi",0,0,"","",false,"som_awaitack_dmi_1",DisplayBoundary.getCaseID());
		DMC.publishMessage("StartOfMissionEvent", M);
	}
	public void ackControl() throws MqttException, InterruptedException {
		DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883", "dmi");
		DMC.publishString("AckEVC", String.valueOf(DisplayBoundary.getCaseID()));
		synchronized(this) {
			this.wait();
		}
		// If it doesn't get woken up, it assumes there's a network problem between the DMI and the EVC.
		
		// A check should be done on the timeout. If the check is positive, then the driver is provided with
		// an error message.

		// Code to execute after the thread wakes up.
		System.out.println("MC WOKE UP");
	}
	
	
	@Override
	void sendEvent(String op,DMIMQTTClient DMC) throws Exception {
		switch(op) {
		case "StartOfMissionEvent":
			Message M = new Message(0,"dmi",0,0,"","",false,"som_start",DisplayBoundary.getCaseID());
			DMC.publishMessage("StartOfMissionEvent", M);
			break;
		default: throw new Exception();
		
		}
		
	}

}
