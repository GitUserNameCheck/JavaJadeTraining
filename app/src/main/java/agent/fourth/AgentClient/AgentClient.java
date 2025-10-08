package agent.fourth.AgentClient;

import jade.core.Agent;
import jade.util.Logger;

public class AgentClient extends Agent {

    Logger logger = Logger.getMyLogger(getClass().getName());

    public AgentClient() {
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    @Override
    protected void setup() {

        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(new AgentClientBehaviour(this));
    }
}
