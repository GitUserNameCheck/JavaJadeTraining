package agent.second;

import jade.core.Agent;
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
        

        logger.info("Hello! Agent " + getLocalName() + " is ready");
        addBehaviour(new CalculateBehaviour(this));
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
