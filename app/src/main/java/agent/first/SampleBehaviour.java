package agent.first;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.util.Logger;

public class SampleBehaviour extends Behaviour{

    Logger logger = null;

    public SampleBehaviour(Agent parent){
        super(parent);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    public void action(){
        logger.info("Hello from SampleBehaviour");
    }

    @Override
    public boolean done(){
        return true;
    }
}
