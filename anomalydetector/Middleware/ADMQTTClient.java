/* MQTT Classes will be removed for the AD... */

package Middleware;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import ad.Message;

public class ADMQTTClient {
	private volatile static ADMQTTClient DMC = null;
	
	MqttClient Client;
	ADMQTTListener Listener;
	

	private ADMQTTClient(String broker, String cid) throws MqttException {
		MemoryPersistence persistence = new MemoryPersistence();
		Client = new MqttClient(broker, cid, persistence);
	}
	public static ADMQTTClient getInstance(String broker, String cid) throws MqttException {
		if(DMC==null) {
			synchronized(ADMQTTClient.class) {
				if(DMC==null) {
					DMC = new ADMQTTClient(broker, cid);
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
	
	
	public void disconnect() throws MqttException {
		Client.disconnect();
	}
	
	public void initializeListener() throws MqttException {
		Listener = new ADMQTTListener();
		Client.subscribe("StartOfMissionEvent", Listener);
	}
	
	public void publishMessage(String Topic, Message msg) throws MqttPersistenceException, MqttException {
		String SerializedMsg = serializeMessage(msg);
		MqttMessage msgToSend = new MqttMessage(SerializedMsg.getBytes());
		Client.publish(Topic, msgToSend);
        System.out.println("Message published");
	}
	
	public String serializeMessage(Message msg) {
		// Timestamp:Resource:Level:TrainData:Mode:TerminalData:Retry:Activity:CaseID
		
		String SerializedMsg = msg.getTimestamp()+":"+msg.getResource()+":"+msg.getLevel()+":"+msg.getTrainData()+":"+msg.getMode()
		+":"+msg.getTD()+":"+msg.getRetry()+":"+msg.getActivity()+":"+msg.getCaseID();
		
		
		return SerializedMsg;
	}
}
