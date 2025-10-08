package agent.fourth.AgentCoordinator;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.util.Logger;

public class AgentCoordinator extends Agent{

    Logger logger = Logger.getMyLogger(getClass().getName());

    public AgentCoordinator(){
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    @Override
    protected void setup(){

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
