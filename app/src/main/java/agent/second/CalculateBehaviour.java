package agent.second;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class CalculateBehaviour extends CyclicBehaviour {
    
    Logger logger = null;

    public CalculateBehaviour(Agent parent){
        super(parent);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    public void action() {

        ACLMessage msg = myAgent.receive();

        if (msg != null) {
            if(msg.getPerformative() == ACLMessage.REQUEST && "sum".equals(msg.getLanguage())){

                String content = msg.getContent();
                
                if (content != null && content.matches("\\d+\\s*,\\s*\\d+")){
                    String[] parts = content.split(",");
                    int a = Integer.parseInt(parts[0].trim());
                    int b = Integer.parseInt(parts[1].trim());
                    int answer = (b - a + 1) * (a + b) / 2; 

                    logger.info(myAgent.getLocalName() + ": ans for " + content + " = " + answer);

                    ACLMessage reply = msg.createReply();
                    reply.setContent(String.valueOf(answer));
                    reply.setPerformative(ACLMessage.CONFIRM);
                    myAgent.send(reply);
                }
            }

        } else {
            block();
        }

    }
}
