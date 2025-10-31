package agent.Coordinator.AgentCalculator;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ParallelBehaviour;
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

    private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

    @Override
    protected void setup(){

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
