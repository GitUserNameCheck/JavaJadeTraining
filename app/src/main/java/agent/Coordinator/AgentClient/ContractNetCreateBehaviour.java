package agent.Coordinator.AgentClient;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class ContractNetCreateBehaviour extends Behaviour{
    
    Logger logger = null;

    boolean first = true;

    private ThreadedBehaviourFactory tbf;

    public ContractNetCreateBehaviour(Agent parent, ThreadedBehaviourFactory tbf) {
        super(parent);
        this.tbf = tbf;
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    public void action() {
        if(first){

            logger.info("Contra");

            getDataStore().put("done", false);
            logger.info(myAgent.getLocalName() + ": created contract net behaviour");

            InitiatorBehaviour initBehaviour = new InitiatorBehaviour(myAgent, (ACLMessage) getDataStore().get("cfp"), (AID) getDataStore().get("receiver"));
            initBehaviour.setDataStore(getDataStore());

            myAgent.addBehaviour(tbf.wrap(initBehaviour));
            first = false;
        } else {
            block();
        }
    }

    @Override
    public boolean done() {
        boolean value = (boolean) getDataStore().get("done");
        first = true;
        getDataStore().put("done", false);
        return value;
    }

}
