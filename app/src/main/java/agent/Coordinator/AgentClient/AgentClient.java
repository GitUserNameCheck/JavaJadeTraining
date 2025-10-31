package agent.Coordinator.AgentClient;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.util.Logger;

public class AgentClient extends Agent {

    Logger logger = Logger.getMyLogger(getClass().getName());

    private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

    public AgentClient() {
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    @Override
    protected void setup() {

        FSMBehaviour fsm = new FSMBehaviour(this);
        DataStore ds = new DataStore();

        AgentClientBehaviour agentClient = new AgentClientBehaviour(this);
        agentClient.setDataStore(ds);
        ContractNetCreateBehaviour contractCreate = new ContractNetCreateBehaviour(this, tbf);
        contractCreate.setDataStore(ds);

        fsm.registerFirstState(agentClient, "agentClient");
        fsm.registerState(contractCreate, "contractCreate");

        fsm.registerDefaultTransition("agentClient", "contractCreate");
        fsm.registerDefaultTransition("contractCreate", "agentClient");

        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(fsm);
    }

    @Override
    protected void takeDown() {
        tbf.interrupt();
        logger.info("Agent " + getLocalName() + " terminating.");
    }
}
