package agent.Coordinator.AgentCoordinator;

import java.util.HashMap;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
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

    @Override
    public void action() {

        logger.info("RefuseWaitForAnswerBehaviour");

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

                        agentNumber = 0;
                        answer = 0;
                        acceptProposal = null;
                        done = true;
                        getDataStore().remove("calculatorAgents");
                        getDataStore().remove("answerReceiver");
                        getDataStore().remove("gotRequest");
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
        return 0;
    }

    @Override
    public boolean done() {
        if(done){
            done = false;
            return true;
        }
        return false;
    }

}


