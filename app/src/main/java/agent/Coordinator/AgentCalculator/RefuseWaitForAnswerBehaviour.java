package agent.Coordinator.AgentCalculator;

import java.util.HashMap;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;


public class RefuseWaitForAnswerBehaviour extends Behaviour{
    
    Logger logger = null;

    public RefuseWaitForAnswerBehaviour(Agent parent){
        super(parent);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    ACLMessage acceptProposal = null;
    int answer = 0;
    int agentNumber = 0;
    boolean done = false;
    int state = 0;

    @Override
    public void action() {


        // logger.info("refuse");

        ACLMessage failure = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.FAILURE));

        if(failure != null){
            logger.info(myAgent.getLocalName() + " answering failure check to " + failure.getSender().getLocalName());
            ACLMessage failureCheck = new ACLMessage(ACLMessage.FAILURE);
            failureCheck.addReceiver(failure.getSender());
            myAgent.send(failureCheck);
        }

        ACLMessage msg = myAgent.receive();

        if (msg != null) {

            HashMap<String, Boolean> agents = (HashMap<String, Boolean>) getDataStore().get("calculatorAgents");

            if (msg.getPerformative() == ACLMessage.CONFIRM 
                    && agents.containsKey(msg.getSender().getLocalName())
                    && !agents.get(msg.getSender().getLocalName())
                ) {


                String content = msg.getContent();

                if (content != null && content.matches("\\d+")) {
                    int sum = Integer.parseInt(content);
                    agents.put(msg.getSender().getLocalName(), true);
                    agentNumber++;
                    answer += sum;

                    logger.info(myAgent.getLocalName() + ": " + "got " + sum + " from " + msg.getSender().getLocalName());

                    if (agentNumber == agents.size()) {
                        acceptProposal = (ACLMessage) getDataStore().get("acceptProposal");
                        ACLMessage ans = acceptProposal.createReply();
                        ans.setPerformative(ACLMessage.INFORM);
                        ans.setContent(String.valueOf(answer));
                        myAgent.send(ans);

                        logger.info("answer " + answer + " sent to " + acceptProposal.getSender().getLocalName());

                        done = true;
                        return;
                    }
                }
            } else {
                String content = msg.getContent();

                logger.info(myAgent.getLocalName() + ": send refuse to\n" + msg.getSender().getLocalName());

                ACLMessage reply = msg.createReply();
                reply.setContent(content);
                reply.setPerformative(ACLMessage.REFUSE);
                myAgent.send(reply);
            }

        } else {
            block();
        }
    }

    @Override
    public int onEnd() {
        agentNumber = 0;
        answer = 0;
        acceptProposal = null;
        done = false;
        getDataStore().remove("calculatorAgents");
        getDataStore().remove("acceptProposal");
        return state;
    }

    @Override
    public boolean done() {
        return done;
    }

}


