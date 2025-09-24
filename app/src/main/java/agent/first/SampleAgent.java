package agent.first;

import jade.core.Agent;
import jade.util.Logger;

public class SampleAgent extends Agent{

    Logger logger = Logger.getMyLogger(getClass().getName());

    public SampleAgent(){
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    @Override
    protected void setup(){
        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(new SampleBehaviour(this));
        addBehaviour(new MessageLoggingBehaviour(this));
    }
}
