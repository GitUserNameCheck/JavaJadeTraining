package agent.fifth.AgentClient;

import java.util.Enumeration;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.util.Logger;

public class InitiatorBehaviour extends ContractNetInitiator{

    Logger logger = null;

    AID recevier;

    public InitiatorBehaviour(Agent parent, ACLMessage cfp, AID receiver) {
        super(parent, cfp);
        this.recevier = receiver;
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
        logger.info("Proposal refused by " + refuse.getSender().getLocalName());
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        Integer cost = Integer.MAX_VALUE;
        ACLMessage best = null;

        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();

            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                int proposed_cost = Integer.parseInt(msg.getContent());
                if(proposed_cost < cost) {
                    cost = proposed_cost;
                    best = msg;
                }
            }
        }

        e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            ACLMessage reply = msg.createReply();
            if (msg == best) {
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                reply.setContent("Accepted");
                acceptances.add(reply);
                logger.info("Accepting proposal from " + msg.getSender().getLocalName());
            } else {
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                reply.setContent("Rejected");
                acceptances.add(reply);
            }
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        logger.info("Agent " + inform.getSender().getLocalName() +
                " successfully performed the requested action: " + inform.getContent());
        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
        reply.addReceiver(recevier);
        reply.setContent(inform.getContent());
        myAgent.send(reply);
        logger.info("Send answer " + inform.getContent() + " to " + recevier.getLocalName());
    
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        logger.info("Failure from " + failure.getSender().getLocalName());
        ACLMessage reply = new ACLMessage(ACLMessage.FAILURE);
        reply.addReceiver(recevier);
        reply.setContent(failure.getContent());
        myAgent.send(reply);
    }

}
