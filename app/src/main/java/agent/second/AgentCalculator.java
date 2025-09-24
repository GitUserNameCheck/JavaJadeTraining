package agent.second;

import jade.core.Agent;
import jade.util.Logger;

public class AgentCalculator extends Agent{
    
    Logger logger = Logger.getMyLogger(getClass().getName());

    public AgentCalculator(){
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    @Override
    protected void setup(){

        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(new CalculateBehaviour(this));
    }
}
