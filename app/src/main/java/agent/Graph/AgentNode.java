package agent.Graph;

import java.util.HashSet;

import jade.core.Agent;
import jade.core.ContainerID;
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

            if(args[0] != null){
                for (String receiverName : (String[]) args[0]) {
                    set.add(receiverName);
                }
            }

            if(args[1] != null){
                ContainerID target = new ContainerID((String) args[1], null);

                if(!here().getName().equals(target.getName())){
                    logger.info("Moving Agent " + getLocalName() + " to destinaiton " + target.getName());
                    doMove(target);
                }
            }
        }

        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(new NodeBehaviour(this, set));
    }

}
