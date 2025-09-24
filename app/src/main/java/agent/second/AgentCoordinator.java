package agent.second;

import java.util.HashMap;

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
        HashMap<String, Boolean> map = new HashMap<>();
        Object[] args = getArguments();

        if(args != null){
            for(String receiverName : (String[]) args){
                map.put(receiverName, false);
            }
        }

        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(new CoordinateBehaviour(this, map));
    }
}
