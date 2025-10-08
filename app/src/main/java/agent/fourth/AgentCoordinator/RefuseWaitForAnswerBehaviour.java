package agent.fourth.AgentCoordinator;

import java.util.HashMap;

import jade.core.AID;
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

    AID answerReceiverName = null;
    int answer = 0;
    int agentNumber = 0;
    boolean done = false;

    @Override
    public void action() {

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
                        answerReceiverName = (AID) getDataStore().get("answerReceiver");
                        ACLMessage ans = new ACLMessage(ACLMessage.CONFIRM);
                        ans.addReceiver(answerReceiverName);
                        ans.setContent(String.valueOf(answer));
                        myAgent.send(ans);

                        logger.info("answer " + answer + " sent to " + answerReceiverName.getLocalName());

                        agentNumber = 0;
                        answer = 0;
                        answerReceiverName = null;
                        done = true;
                        getDataStore().remove("calculatorAgents");
                        getDataStore().remove("answerReceiver");
                        getDataStore().remove("gotRequest");
                    }
                }
            } else if (msg.getPerformative() == ACLMessage.REFUSE && agents.containsKey(msg.getSender().getLocalName())){

                logger.info(myAgent.getLocalName() + ": got refuse from\n" + msg.getSender().getLocalName());

                String content = msg.getContent();
                
                ACLMessage ans = new ACLMessage(ACLMessage.REQUEST);
                ans.addReceiver(msg.getSender());
                ans.setContent(content);
                myAgent.send(ans);

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
    public boolean done() {
        if(done){
            done = false;
            return true;
        }
        return false;
    }

}


