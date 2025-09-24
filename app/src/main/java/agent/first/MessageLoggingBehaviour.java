package agent.first;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class MessageLoggingBehaviour extends CyclicBehaviour {
    
    Logger logger = null;

    public MessageLoggingBehaviour(Agent parent){
        super(parent);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    public void action() {

        ACLMessage msg = myAgent.receive();

        if (msg != null) {
            logger.info("Sender: " + msg.getSender().getLocalName() + "\n" + 
                        "Permormative: " + ACLMessage.getPerformative(msg.getPerformative()) + "\n" +
                        "Content: " + msg.getContent());
            ;

            ACLMessage reply = msg.createReply();
            reply.setContent("Message received");
            reply.setPerformative(ACLMessage.CONFIRM);
            myAgent.send(reply);

        } else {
            block();
        }

    }
    
}
