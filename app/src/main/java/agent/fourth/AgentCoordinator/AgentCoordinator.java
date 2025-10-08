package agent.fourth.AgentCoordinator;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
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

        WaitForRequestBehaviour wait = new WaitForRequestBehaviour(this);
        wait.setDataStore(ds);

        RefuseWaitForAnswerBehaviour refuse = new RefuseWaitForAnswerBehaviour(this);
        refuse.setDataStore(ds);

        fsm.registerFirstState(wait, "wait");
        fsm.registerState(refuse, "refuse");

        fsm.registerDefaultTransition("wait", "refuse");
        fsm.registerDefaultTransition("refuse", "wait");


        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(fsm);
    }
}
