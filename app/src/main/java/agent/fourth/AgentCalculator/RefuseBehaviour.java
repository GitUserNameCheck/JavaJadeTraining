package agent.fourth.AgentCalculator;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class RefuseBehaviour extends CyclicBehaviour{
    
    Logger logger = null;

    public RefuseBehaviour(Agent parent){
        super(parent);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }


    @Override
    public void action() {

        ACLMessage msg = myAgent.receive();

        if (msg != null) {

            String content = msg.getContent();

            ACLMessage reply = msg.createReply();
            reply.setContent(content);
            reply.setPerformative(ACLMessage.REFUSE);
            myAgent.send(reply);
               

        } else {
            block();
        }
    }
}
