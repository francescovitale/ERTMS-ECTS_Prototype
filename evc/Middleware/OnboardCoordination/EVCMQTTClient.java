package Middleware.OnboardCoordination;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import ApplicationLayer.OnboardControl.Message;

public class EVCMQTTClient {
	private volatile static EVCMQTTClient DMC = null;
	
	MqttClient Client;
	EVCMQTTListener Listener;
	

	private EVCMQTTClient(String broker, String cid) throws MqttException {
		Client = new MqttClient(broker, cid);
	}
	public static EVCMQTTClient getInstance(String broker, String cid) throws MqttException {
		if(DMC==null) {
			synchronized(EVCMQTTClient.class) {
				if(DMC==null) {
					DMC = new EVCMQTTClient(broker, cid);
				}
			}
		}
		return DMC;
	}
	
	public void connect() throws MqttSecurityException, MqttException {
		MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        Client.connect(connOpts);
	}
	
	
	public void initializeListener() throws MqttException {
		Listener = new EVCMQTTListener();
		Client.subscribe("DriverIDEVC", Listener);
		Client.subscribe("LevelEVC", Listener);
		Client.subscribe("LevelEVCOB", Listener);
		Client.subscribe("TerminalEVC", Listener);
		Client.subscribe("RBCEVC", Listener);
		Client.subscribe("RBCEVCRBC", Listener);
		Client.subscribe("RBCRoutineEVC", Listener);
		Client.subscribe("RBCRoutineEVCRBC",Listener);
		Client.subscribe("CheckLevSessEVC",Listener);
		Client.subscribe("TrainDataEVC", Listener);
		Client.subscribe("CheckLevSessEVCRBC", Listener);
		Client.subscribe("StartEVC", Listener);
		Client.subscribe("StartEVCRBC", Listener);
		Client.subscribe("AckEVC", Listener);
		
		
	}
	
	public void disconnect() throws MqttException {
		Client.disconnect();
	}

	public void publishMessage(String Topic, Message msg) throws MqttPersistenceException, MqttException {
		String SerializedMsg = serializeMessage(msg);
		MqttMessage msgToSend = new MqttMessage(SerializedMsg.getBytes());
		Client.publish(Topic, msgToSend);
        System.out.println("Message published");
	}
	
	public void publishString(String Topic, String msg) throws MqttPersistenceException, MqttException {
		MqttMessage msgToSend = new MqttMessage(msg.getBytes());
		Client.publish(Topic, msgToSend);
        System.out.println("Message published");
	}
	
	public String serializeMessage(Message msg) {
		// Timestamp:Resource:Level:TrainData:Mode:TerminalData:Retry:Activity:CaseID
		
		String SerializedMsg = msg.getTimestamp()+"!"+msg.getResource()+"!"+msg.getLevel()+"!"+msg.getTrainData()+"!"+msg.getMode()
		+"!"+msg.getTD()+"!"+msg.getRetry()+"!"+msg.getActivity()+"!"+msg.getCaseID();
		
		
		return SerializedMsg;
	}
}
