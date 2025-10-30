package agent.Coordinator.AgentCoordinator;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class AgentCoordinator extends Agent{

    Logger logger = Logger.getMyLogger(getClass().getName());

    public AgentCoordinator(){
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    @Override
    protected void setup(){

        DFAgentDescription DFD = new DFAgentDescription();
        DFD.setName(getAID());
        ServiceDescription SD = new ServiceDescription();
        SD.setType("coordinator");
        SD.setName(getLocalName());
        DFD.addServices(SD);

        try {
            DFService.register(this, DFD);
            logger.info(getLocalName() + " registered in DF as a 'coordinator' service.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        FSMBehaviour fsm = new FSMBehaviour(this);
        DataStore ds = new DataStore();
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);

        ResponderBehaviourByHand respond = new ResponderBehaviourByHand(this, mt);
        respond.setDataStore(ds);

        RefuseWaitForAnswerBehaviour refuse = new RefuseWaitForAnswerBehaviour(this);
        refuse.setDataStore(ds);

        fsm.registerFirstState(respond, "respond");
        fsm.registerState(refuse, "refuse");

        // fsm.registerDefaultTransition("respond", "refuse");
        // fsm.registerDefaultTransition("refuse", "respond");

        fsm.registerTransition("respond", "refuse", 1); 
        fsm.registerTransition("respond", "respond", 0); 

        fsm.registerDefaultTransition("refuse", "respond");


        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(fsm);
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            logger.info(getLocalName() + " deregistered from DF.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        logger.info("Agent " + getLocalName() + " terminating.");
    }

}
