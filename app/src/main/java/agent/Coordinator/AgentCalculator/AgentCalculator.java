package agent.Coordinator.AgentCalculator;

import agent.Coordinator.AgentClient.AgentClientBehaviour;
import agent.Coordinator.AgentClient.ContractNetCreateBehaviour;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class AgentCalculator extends Agent{
    
    Logger logger = Logger.getMyLogger(getClass().getName());

    public AgentCalculator(){
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    private transient ThreadedBehaviourFactory tbf;

    @Override
    protected void setup(){

        Object[] args = getArguments();

        if(args != null && args[0] != null){
            ContainerID target = new ContainerID((String) args[0], null);

            if(!here().getName().equals(target.getName())){
                logger.info("Moving Agent " + getLocalName() + " to destinaiton " + target.getName());
                doMove(target);
            }
        } else {
            tbf = new ThreadedBehaviourFactory();
            registerInDF();
            addBehaviours();
        }
    }

    @Override
    protected void afterMove() {
        tbf = new ThreadedBehaviourFactory();
        registerInDF();
        addBehaviours();
    }

    private void registerInDF() {
        DFAgentDescription DFD = new DFAgentDescription();
        DFD.setName(getAID());
        ServiceDescription SD = new ServiceDescription();
        SD.setType("calculation");
        SD.setName(getLocalName());
        DFD.addServices(SD);

        try {
            DFService.register(this, DFD);
            logger.info(getLocalName() + " registered in DF as a 'calculation' service.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void addBehaviours() {
        FSMBehaviour fsm = new FSMBehaviour(this);
        DataStore ds = new DataStore();
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);

        WaitForMessageBehaviour waitForMes = new WaitForMessageBehaviour(this, tbf);
        waitForMes.setDataStore(ds);

        ElectionBehaviour election = new ElectionBehaviour(this);
        election.setDataStore(ds);

        ResponderBehaviourByHand respond = new ResponderBehaviourByHand(this, mt);
        respond.setDataStore(ds);

        RefuseWaitForAnswerBehaviour refuse = new RefuseWaitForAnswerBehaviour(this);
        refuse.setDataStore(ds);

        fsm.registerFirstState(waitForMes, "waitForMes");
        fsm.registerState(election, "election");
        fsm.registerState(respond, "respond");
        fsm.registerState(refuse, "refuse");

        fsm.registerTransition("waitForMes", "election", 2);
        fsm.registerTransition("election", "waitForMes", 3);
        fsm.registerTransition("election", "respond", 4);
        fsm.registerTransition("respond", "refuse", 1);
        fsm.registerTransition("respond", "election", 5);
        fsm.registerTransition("respond", "waitForMes", 6);

        fsm.registerDefaultTransition("refuse", "respond");

        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(fsm);
    }


    @Override
    protected void takeDown() {
        tbf.interrupt();

        try {
            DFService.deregister(this);
            logger.info(getLocalName() + " deregistered from DF.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        logger.info("Agent " + getLocalName() + " terminating.");
    }

}
