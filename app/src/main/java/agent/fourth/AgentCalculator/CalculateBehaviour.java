package agent.fourth.AgentCalculator;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class CalculateBehaviour extends OneShotBehaviour {
    
    Logger logger = null;

    AID agent_to_reply;
    String interval;

    public CalculateBehaviour(Agent parent, AID agent_to_reply, String interval){
        super(parent);
        this.agent_to_reply = agent_to_reply;
        this.interval = interval;
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    public void action() {
        long delay = 5000;

        logger.info(myAgent.getLocalName() + ": calculating\n" + "sum: " + interval + "\n");

        String[] parts = interval.split(",");
        int a = Integer.parseInt(parts[0].trim());
        int b = Integer.parseInt(parts[1].trim());
        int answer = (b - a + 1) * (a + b) / 2;

        long start = System.currentTimeMillis();
        while(System.currentTimeMillis() - start <= delay){}

        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
        reply.setContent(String.valueOf(answer));
        reply.addReceiver(agent_to_reply);
        myAgent.send(reply);

    }
}
