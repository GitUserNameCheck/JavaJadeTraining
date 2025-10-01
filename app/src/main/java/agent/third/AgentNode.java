package agent.third;

import java.util.HashSet;

import jade.core.Agent;
import jade.util.Logger;

public class AgentNode extends Agent{

    Logger logger = Logger.getMyLogger(getClass().getName());

    public AgentNode(){
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    @Override
    protected void setup(){
        HashSet<String> set = new HashSet<>();
        Object[] args = getArguments();

        if(args != null){
            for(String receiverName : (String[]) args){
                set.add(receiverName);
            }
        }

        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(new NodeBehaviour(this, set));
    }

}
