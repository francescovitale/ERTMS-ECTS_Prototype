/* The listener has the job to receive the messages published on the Broker by other MQTT clients.
 * 
 * Reading the flow might be difficult, so here it's described what's the flow of topics related to a certain part of the procedure.
 * 
 * 1) Inserting the Driver ID
 * 		The DMI publishes a message to the Broker addressing the topic 'DriverIDEVC'
 * 		The EVC elaborates the request and publishes a message to the Broker addressing the topic 'DriverIDDMI'
 * 2) Validating the level
 * 		The DMI publishes a message to the Broker addressing the topic 'LevelEVC'
 *		The EVC elaborates the message and publishes a message to the Broker addressing the topic 'retrievepos'
 *		The OB elaborates the message and publishes a message to the Broker addressing the topic 'LevelEVCOB'
 *		The EVC elaborates the message and publishes a message to the Broker addressing 'LevelDMI'
 * 3) Contacting the RBC
 * 		The DMI publishes a message to the Broker addressing the topic 'RBCEVC'
 * 		The EVC elaborates the message and publishes a message to the Broker depending on the stored level
 * 			If the level is < 2, then it publishes a message to the Broker addressing the topic 'RBCDMI'
 * 			If the level is >= 2, then it publishes a message to the Broker addressing 'OpenConnRBC'
 * 				The RBC elaborates the message and publishes a message to the Broker addressing 'RBCEVCRBC'
 * 				The EVC elaborates the message and publishes a message to the Broker addressing 'RBCDMI'
 * 4) Performing the RBC routine
 * 		The DMI publishes a message to the Broker addressing the topic 'RBCRoutineEVC'
 * 		The EVC elaborates the message and publishes a message to the Broker addressing the topic 'ElabPosRBC'
 * 		The RBC elaborates the message and publishes a message to the Broker addressing the topic 'RBCRoutineEVCRBC'
 * 		The EVC elaborates the message and publishes a message to the Broker addressing the topic 'RBCRoutineDMI'
 * 5) Select the Mode
 * 		The DMI publishes a message to the Broker addressing the topic 'CheckLevSessEVC'
 * 		The EVC elaborates the message and publishes a message to the Broker depending on the level
 * 			If the level is <2, then it publishes a message to the Broker addressing the topic 'CheckLevSessDMI'
 * 			If the level is >=2, then it publishes a message to the Broker addressing the topic 'CheckSessRBC'
 * 				The RBC elaborates the message and publishes a message to the Broker addressing the topic 'CheckLevSessEVCRBC'
 * 				The EVC elaborates the message and publishes a message to the Broker addressing the topic 'CheckLevSessDMI'
 * 6) Start procedure
 * 		The DMI publishes a message to the Broker addressing the topic 'StartEVC'
 * 		The EVC elaborates the message and publishes a message to the Broker depending on the level
 * 			If the level is <2, then it publishes a message to the Broker addressing the topic 'StartDMI'
 * 			If the level is >=2, then it publishes a message to the Broker addressing the topic 'MAManagementRBC'
 * 				The RBC elaborates the message and publishes a message to the Broker addressing the topic 'StartEVCRBC'
 * 				The EVC elaborates the message and publishes a message to the Broker addressing the topic 'StartDMI'
 * 7) Acknowledgement
 * 		The DMI publishes a message to the Broker addressing the topic 'AckEVC'
 * 		The EVC elaborates the message and publishes a message to the Broker addressing the topic 'AckDMI'
 */

package Middleware;

import ApplicationLogic.Message;
import ApplicationLogic.RBCControl;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class RBCMQTTListener implements IMqttMessageListener {

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		RBCControl RC;
		RBCMQTTClient RMC;
		switch(topic) {
		case "OpenConnRBC":
			System.out.println("OpenConnRBC received");
			RC = RBCControl.getInstance();
			RC.openConnection();
			break;
			
		case "ElabPosRBC":
			System.out.println("ElabPosRBC received");
			// The position should here be validated if the EVC deemed it as being valid.
			RC = RBCControl.getInstance();
			if(true) { // Always valid
				RC.setValidPos(true);
			}
			RC.elaboratePosition(Integer.parseInt(message.toString()));
			break;
			
		case "CheckSessRBC":
			System.out.println("CheckSessRBC received");
			RMC = RBCMQTTClient.getInstance(null, null);
			System.out.println(String.valueOf(RBCControl.getInstance().isOpen()));
			RMC.publishString("CheckLevSessEVCRBC",String.valueOf(RBCControl.getInstance().isOpen()));
			
			break;
		case "MAManagementRBC":
			RC = RBCControl.getInstance();
			RC.manageMA(Integer.parseInt(message.toString()));
			break;
		}
	}
	
	public Message deserializeMessage(MqttMessage message) {
		Message DeserializedMsg;
		String msgToDeserialize = message.toString();
		String [] Parts = msgToDeserialize.split("\\!");
		DeserializedMsg = new Message(Parts[0],Parts[1],(int)Integer.parseInt(Parts[2]),(int)Integer.parseInt(Parts[3]),Parts[4],Parts[5],Boolean.getBoolean(Parts[6]),Parts[7],(int)Integer.parseInt(Parts[8]));
		return DeserializedMsg;
	}

}
