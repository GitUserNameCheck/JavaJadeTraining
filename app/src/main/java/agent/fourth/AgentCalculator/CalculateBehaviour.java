package agent.fourth.AgentCalculator;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class CalculateBehaviour extends OneShotBehaviour {
    
    Logger logger = null;

    public CalculateBehaviour(Agent parent){
        super(parent);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    public void action() {
        long delay = 5000;

        logger.info(myAgent.getLocalName() + ": calculating\n" + "sum: " + getDataStore().get("sum") + "\n");

        String[] parts = ((String) getDataStore().get("sum")).split(",");
        int a = Integer.parseInt(parts[0].trim());
        int b = Integer.parseInt(parts[1].trim());
        int answer = (b - a + 1) * (a + b) / 2;

        long start = System.currentTimeMillis();
        while(System.currentTimeMillis() - start <= delay){}

        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
        reply.setContent(String.valueOf(answer));
        reply.addReceiver(((AID) getDataStore().get("replyTo")));
        myAgent.send(reply);


        getDataStore().remove("sum");
        getDataStore().remove("replyTo");

    }
}
