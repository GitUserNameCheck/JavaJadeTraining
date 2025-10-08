package agent.fourth.AgentCalculator;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
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
        
        // https://jade.tilab.com/doc/api/jade/core/behaviours/FSMBehaviour.html
        FSMBehaviour fsm = new FSMBehaviour(this);
        DataStore ds = new DataStore();

        WaitForMessageBehaviour waitForMsg = new WaitForMessageBehaviour(this);
        waitForMsg.setDataStore(ds);

        ParallelBehaviour CalculateRefuse = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ANY);



        RefuseBehaviour refuse = new RefuseBehaviour(this);
        CalculateBehaviour calculate = new CalculateBehaviour(this);
        calculate.setDataStore(ds);

        CalculateRefuse.addSubBehaviour(refuse);
        CalculateRefuse.addSubBehaviour(tbf.wrap(calculate));

        fsm.registerFirstState(waitForMsg, "wait");
        fsm.registerState(CalculateRefuse, "calculate");

        fsm.registerDefaultTransition("wait", "calculate");
        fsm.registerDefaultTransition("calculate", "wait" , new String[] { "calculate" });


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
