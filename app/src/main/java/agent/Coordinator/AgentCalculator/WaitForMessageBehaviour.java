package agent.Coordinator.AgentCalculator;

import java.time.Duration;
import java.time.Instant;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class WaitForMessageBehaviour extends Behaviour {

    Logger logger = null;

    private ThreadedBehaviourFactory tbf;
    boolean election = false;
    Instant lastFailureCheck;


    public WaitForMessageBehaviour(Agent parent, ThreadedBehaviourFactory tbf){
        super(parent);
        this.tbf = tbf;
        this.lastFailureCheck = null;
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }
    
    @Override
    public void action() {

        if(lastFailureCheck == null){
            lastFailureCheck = Instant.now();
        }

        Instant currentTime = Instant.now();

        long lastCheckDiff = Duration.between(lastFailureCheck, currentTime).getSeconds();

        logger.info(myAgent.getLocalName()+ ": time from last failure successful check " + lastCheckDiff);

        if (lastCheckDiff > 10) {
            logger.info(myAgent.getLocalName() + " starting election");
            election = true;
            return;
        }

        if (lastCheckDiff > 1) {
            AID coordinator = (AID) getDataStore().get("coordinator");
            if (coordinator != null) {
                logger.info(myAgent.getLocalName() + ": sending failure check to " + coordinator.getLocalName());
                ACLMessage failureCheck = new ACLMessage(ACLMessage.FAILURE);
                failureCheck.addReceiver(coordinator);
                myAgent.send(failureCheck);
            }
        }

        ACLMessage msg = myAgent.receive();

        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.REQUEST && "sum".equals(msg.getLanguage())) {

                String content = msg.getContent();
                
                logger.info(myAgent.getLocalName() + ": got message " + content + " from " + msg.getSender().getLocalName());

                if (content != null && content.matches("\\d+\\s*,\\s*\\d+")) {
                    myAgent.addBehaviour(tbf.wrap(new CalculateBehaviour(myAgent, msg.getSender(), content)));
                }
            }

            if (msg.getPerformative() == ACLMessage.PROPOSE) {

                String senderName = msg.getSender().getLocalName();

                logger.info(myAgent.getLocalName() + ": got proposal from "
                        + senderName);
                
                ACLMessage reply = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                reply.addReceiver(msg.getSender());
                myAgent.send(reply);

                election = true;
                return;
                
            }

            if (msg.getPerformative() == ACLMessage.INFORM) {

                String senderName = msg.getSender().getLocalName();

                logger.info(myAgent.getLocalName() + ": got coordinator inform from "
                        + senderName);

                getDataStore().put("coordinator", msg.getSender());
            }


            if (msg.getPerformative() == ACLMessage.FAILURE && msg.getSender().equals((AID) getDataStore().get("coordinator"))) {
                lastFailureCheck = Instant.now();
            }

        } else {
            block(1000);
        }

        
    }

    @Override
    public int onEnd() {
        lastFailureCheck = null;
        election = false;
        return 2;
    }

    @Override
    public boolean done() {
        return election;
    }
    
}
