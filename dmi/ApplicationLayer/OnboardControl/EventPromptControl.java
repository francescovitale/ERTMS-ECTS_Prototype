package ApplicationLayer.OnboardControl;

import Middleware.OnboardCoordination.DMIMQTTClient;

public abstract class EventPromptControl {
	abstract void sendEvent(String op, DMIMQTTClient DMC) throws Exception;

}
