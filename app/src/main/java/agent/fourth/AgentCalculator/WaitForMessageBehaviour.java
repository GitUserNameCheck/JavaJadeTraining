package agent.fourth.AgentCalculator;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class WaitForMessageBehaviour extends CyclicBehaviour{

    Logger logger = null;

    private ThreadedBehaviourFactory tbf;

    public WaitForMessageBehaviour(Agent parent, ThreadedBehaviourFactory tbf){
        super(parent);
        this.tbf = tbf;
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
                    myAgent.addBehaviour(tbf.wrap(new CalculateBehaviour(myAgent, msg.getSender(), content)));
                }
            }

        } else {
            block();
        }
    }
}
