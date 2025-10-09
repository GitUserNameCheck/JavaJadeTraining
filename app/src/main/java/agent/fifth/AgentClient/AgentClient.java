package agent.fifth.AgentClient;

import jade.core.Agent;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.util.Logger;

public class AgentClient extends Agent {

    Logger logger = Logger.getMyLogger(getClass().getName());

    private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

    public AgentClient() {
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    @Override
    protected void setup() {

        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(new AgentClientBehaviour(this, tbf));
    }

    @Override
    protected void takeDown() {
        tbf.interrupt();
        logger.info("Agent " + getLocalName() + " terminating.");
    }
}
