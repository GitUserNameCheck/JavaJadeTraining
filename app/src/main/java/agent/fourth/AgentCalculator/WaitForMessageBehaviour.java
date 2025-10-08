package agent.fourth.AgentCalculator;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class WaitForMessageBehaviour extends Behaviour{

    Logger logger = null;

    public WaitForMessageBehaviour(Agent parent){
        super(parent);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }
    
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();

        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.REQUEST && "sum".equals(msg.getLanguage())) {

                String content = msg.getContent();

                logger.info(myAgent.getLocalName() + ": got message " + content + " from " + msg.getSender().getLocalName());

                if (content != null && content.matches("\\d+\\s*,\\s*\\d+")) {
                    getDataStore().put("sum", content);
                    getDataStore().put("replyTo", msg.getSender());
                }
            }

        } else {
            block();
        }
    }

    @Override
    public boolean done() {
        boolean done = getDataStore().containsKey("sum");
        return done;
    }
    
}
