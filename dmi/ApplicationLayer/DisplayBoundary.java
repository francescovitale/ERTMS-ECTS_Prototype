package ApplicationLayer;

import java.util.Random;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttException;

import ApplicationLayer.OnboardControl.InputControl;
import ApplicationLayer.OnboardControl.MissionControl;
import Middleware.OnboardCoordination.DMIMQTTClient;

public class DisplayBoundary {
	private Integer SOMCaseID; // This is the CaseID that will be used for the event tracing
	private int obtainedLevel = 2;
	private String grantedMA;
	private boolean retry = false;
	private volatile static DisplayBoundary DB = null;
	
	private DisplayBoundary() throws Exception {
		MissionControl MC = MissionControl.getInstance();
		MC.initializeSOM(); // The DMI is initialized with the listener subscribed to all the topics
		Random rand = new Random();

		// Obtain a number between [0 - 10000000].
		SOMCaseID = rand.nextInt(10000000);
	}
	// This class is a singleton.
	public static DisplayBoundary getInstance() throws Exception {
		if(DB==null) {
			synchronized(DisplayBoundary.class) {
				if(DB==null) {
					DB = new DisplayBoundary();
				}
			}
		}
		return DB;
	}
	
	// The Start of Mission process is engaged.
	public void engage(){
		try {
			MissionControl.getInstance().sendEvent("som_start",DMIMQTTClient.getInstance(null, null)); // The 'som_start' event is transmitted.
			insertDriverID(); // The driver ID will now be recovered.
		} catch (Exception e) {
			System.out.println("A problem has incurred in validating the DriverID or retrieving the position from the sensors. Please, try again"); // This happens because the process couldn't communicate with the EVC, or the EVC couldn't communicate with the sensor.
			Random rand = new Random();
			SOMCaseID = rand.nextInt(10000000); // A new Case ID is created
			standByMenu(); // The menu is re-proposed to the driver.
		}
		
	}
	// The driver is requested with its ID.
	public void insertDriverID() throws Exception {
		System.out.print("Insert the Driver ID:");
		Scanner sc1 = new Scanner(System.in);
		String InsertedOption = sc1.nextLine();
		System.out.println(InsertedOption);
		InputControl IC = InputControl.getInstance();
		if(IC.insertDriverIDControl(InsertedOption)==true) { // insertDriverIDControl() returns true if the request has timed out
			throw new Exception();
		};
	}
	// The driver is asked to manually insert the level.
	public void insertLevel() {}
	
	// The RBC is contacted. The user can retry multiple times.
	public void contactRBC() throws MqttException, Exception {
		System.out.println("Trying to open the connection to RBC...");
		System.out.println("Write 'yes' if you want to retry the connection to the RBC, otherwise write 'no': ");
		Scanner sc1 = new Scanner(System.in);
		String InsertedOption = sc1.nextLine();
		try {
			MissionControl MC = MissionControl.getInstance();
			MC.contactRBCControl(InsertedOption);
		} catch (Exception e) {
			System.out.println("A problem has incurred in contacting the RBC. Please, try again"); // This happens because the process couldn't communicate with the EVC, or the EVC couldn't communicate with the sensor.
			MissionControl.getInstance().sendEvent("som_end",DMIMQTTClient.getInstance(null, null)); // The procedure is cut.
			Random rand = new Random();
			SOMCaseID = rand.nextInt(10000000); // A new Case ID is created
			standByMenu(); // The menu is re-proposed to the driver.
		}
	}
	
	// The driver is asked to select for the preferred mode.
	public void selectMode() throws Exception {
		System.out.println("You'll now select the mode of choice. Beware that if the level selected is 2 or 3, a session with RBC must be open if you want to insert Train Data");
		System.out.println("Select an option among the ones enlisted:");
		System.out.println("(a) - NL");
		System.out.println("(b) - SH");
		System.out.println("(c) - Insert train data");
		System.out.print("Select :");
		Scanner sc1 = new Scanner(System.in);
		String InsertedOption = sc1.nextLine();
		InputControl IC = InputControl.getInstance();
		IC.selectModeControl();
		System.out.println(InsertedOption);
		switch(InsertedOption.charAt(0)) {
		case 'a':
			break;
		case 'b':
			break;
		case 'c':
			// Only option implemented.
			System.out.print("Insert the Train Data:");
			InsertedOption = sc1.nextLine();
			System.out.println(InsertedOption);
			// Check the train data... 
			IC.insertTrainDataControl(Integer.parseInt(InsertedOption));
			break;
		default: System.out.println("Invalid option");
		}
		start(); // Trigger for the 'start' phase.
	}
	// The procedure enters the 'start' phase, which will follow a MA received from the RBC in case
	// the ERTMS level is the level 2 or 3, or else will trigger a proposal for the driver concerning UN, SR
	public void start() throws Exception {
		System.out.println("Write 'start' to trigger the starting procedure.");
		Scanner sc1 = new Scanner(System.in);
		String InsertedOption = sc1.nextLine();
		while(!InsertedOption.equals("start"))
		{
			System.out.println("Try again.");
			InsertedOption = sc1.nextLine();
		}
		MissionControl MC = MissionControl.getInstance();
		MC.startControl();
		ackMode(); // The driver will be asked to acknowledge the mode
	}
	
	// The driver is asked for acknowledging the proposed mode.
	public void ackMode() throws Exception {
		System.out.println("Acknowledge the following mode:" + grantedMA + " mode");
		System.out.print("Write 'ack':");
		Scanner sc1 = new Scanner(System.in);
		String InsertedOption = sc1.nextLine();
		System.out.println(InsertedOption);
		while(!InsertedOption.equals("ack"))
		{
			System.out.println("Try again.");
			InsertedOption = sc1.nextLine();
		}
		MissionControl MC = MissionControl.getInstance();
		MC.ackControl(); // This method will manage the acknowledgement.
		System.out.println("The system is now in "+ grantedMA + " mode.");
		System.out.println("The procedure will now automatically retry.");
		standByMenu();
	}
	
	// Coming from a stand-by state, the Driver is prompted with a request to trigger the Start of Mission procedure.
	public void standByMenu(){
		System.out.println("This is the Driver Interface stand-by menu.");
		System.out.println("Select an option among the ones enlisted:");
		System.out.println("(a) - Start of Mission");
		System.out.println("Make your selection: ");
		Scanner sc = new Scanner(System.in);
		String InsertedOption = sc.nextLine();
		char chosenoption = InsertedOption.charAt(0);
		switch(chosenoption) {
		case 'a':
			engage(); // The 'Start of Mission' procedure is engaged.
			break;
		default: 
			System.out.println("The option doesn't exist.");
		}
		
	}
	public Integer getCaseID() {
		return SOMCaseID;
	}
	public void setCaseID(int caseID) {
		SOMCaseID = caseID;
	}
	public int getObtainedLevel() {
		return obtainedLevel;
	}
	public void setObtainedLevel(int obtainedLevel) {
		this.obtainedLevel = obtainedLevel;
	}
	public String getGrantedMA() {
		return grantedMA;
	}
	public void setGrantedMA(String grantedMA) {
		this.grantedMA = grantedMA;
	}
	public Integer getSOMCaseID() {
		return SOMCaseID;
	}
	public void setSOMCaseID(Integer sOMCaseID) {
		SOMCaseID = sOMCaseID;
	}
	public boolean isRetry() {
		return retry;
	}
	public void setRetry(boolean retry) {
		this.retry = retry;
	}
	
	public static void main(String[] args) throws Exception {
		DisplayBoundary DB = DisplayBoundary.getInstance(); // The DMI gets initialized: a new CaseID determined and the MQTT client created.
		DB.standByMenu(); // The menu is created.
	}
}
