package agent.second;

import jade.core.Agent;
import jade.util.Logger;

public class AgentCoordinator extends Agent{

    Logger logger = Logger.getMyLogger(getClass().getName());

    public AgentCoordinator(){
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    @Override
    protected void setup(){

        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(new CoordinateBehaviour(this));
    }
}
