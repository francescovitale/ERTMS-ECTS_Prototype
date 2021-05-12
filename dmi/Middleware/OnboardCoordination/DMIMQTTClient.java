package Middleware.OnboardCoordination;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import ApplicationLayer.OnboardControl.Message;

public class DMIMQTTClient {
	private volatile static DMIMQTTClient DMC = null;
	
	static MqttClient Client;
	DMIMQTTListener Listener;
	

	private DMIMQTTClient(String broker, String cid) throws MqttException {
		Client = new MqttClient(broker, cid);
	}
	public static DMIMQTTClient getInstance(String broker, String cid) throws MqttException {
		if(DMC==null) {
			synchronized(DMIMQTTClient.class) {
				if(DMC==null) {
					DMC = new DMIMQTTClient(broker, cid);
				}
			}
		}
		return DMC;
	}
	
	
	public void connect() throws MqttSecurityException, MqttException {
		MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        Client.connect(connOpts);
        System.out.println("Connection established to the broker");
	}
	
	public void initializeListener() throws MqttException {
		Listener = new DMIMQTTListener();
		Client.subscribe("DriverIDDMI", Listener);
		Client.subscribe("LevelDMI", Listener);
		Client.subscribe("TerminalDMI", Listener);
		Client.subscribe("RBCDMI", Listener);
		Client.subscribe("TrainDataDMI", Listener);
		Client.subscribe("StartDMI", Listener);
		Client.subscribe("RBCRoutineDMI", Listener);
		Client.subscribe("CheckLevSessDMI", Listener);
		Client.subscribe("AckDMI", Listener);
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
