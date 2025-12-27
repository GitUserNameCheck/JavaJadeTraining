package agent.Coordinator.AgentClient;

import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.Logger;

public class AgentClient extends Agent {

    Logger logger = Logger.getMyLogger(getClass().getName());

    private transient ThreadedBehaviourFactory tbf;

    public AgentClient() {
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    @Override
    protected void setup() {

        Object[] args = getArguments();

        if (args != null && args[0] != null) {
            ContainerID target = new ContainerID((String) args[0], null);

            if (!here().getName().equals(target.getName())) {
                logger.info("Moving Agent " + getLocalName() + " to destinaiton " + target.getName());
                doMove(target);
            }
        } else {
            tbf = new ThreadedBehaviourFactory();
            addBehaviours();
        }


    }

    @Override
    protected void afterMove() {
        tbf = new ThreadedBehaviourFactory();
        addBehaviours();
    }

    private void addBehaviours() {
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
