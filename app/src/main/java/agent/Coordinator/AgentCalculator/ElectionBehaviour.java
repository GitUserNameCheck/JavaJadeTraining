package agent.Coordinator.AgentCalculator;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class ElectionBehaviour extends Behaviour{
    Logger logger = null;

    public ElectionBehaviour(Agent parent){
        super(parent);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    int coordinator = 3;
    boolean firstRun = true;
    boolean done = false;
    Instant electionStart;

    @Override
    public void action() {
        if(firstRun){
            HashMap<AID, Boolean> agents = new HashMap<AID, Boolean>();
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("calculation");
            template.addServices(sd);
            DFAgentDescription template2 = new DFAgentDescription();
            ServiceDescription sd2 = new ServiceDescription();
            sd2.setType("coordinator");
            template2.addServices(sd2);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                DFAgentDescription[] result2 = DFService.search(myAgent, template2);
                DFAgentDescription[] merged = Stream.concat(Arrays.stream(result), Arrays.stream(result2))
                                   .toArray(DFAgentDescription[]::new);

                for (DFAgentDescription DFD : merged) {
                    AID agentID = DFD.getName();
                    String localName = agentID.getLocalName();
                    int myNumber = Integer.parseInt(myAgent.getLocalName().substring(10));
                    int agentNumber = Integer.parseInt(localName.substring(10));
                    if(agentNumber > myNumber){
                        agents.put(agentID, false);
                    }
                }
                
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            for (AID agent : agents.keySet()) {
                ACLMessage request = new ACLMessage(ACLMessage.PROPOSE);
                request.addReceiver(agent);
                myAgent.send(request);
                logger.info(myAgent.getLocalName() + ": send proposal to "
                        + agent.getLocalName());
            }
            logger.info(myAgent.getLocalName() + ": election start");
            electionStart = Instant.now();
            firstRun = false;
        }

        long timeDiff = Duration.between(electionStart, Instant.now()).getSeconds();

        logger.info(myAgent.getLocalName() + ": time from election start " + timeDiff);

        if(timeDiff > 2){
            logger.info(myAgent.getLocalName() + ": coordinator ");
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("calculation");
            template.addServices(sd);
            DFAgentDescription template2 = new DFAgentDescription();
            ServiceDescription sd2 = new ServiceDescription();
            sd2.setType("coordinator");
            template2.addServices(sd2);
            
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                DFAgentDescription[] result2 = DFService.search(myAgent, template2);
                DFAgentDescription[] merged = Stream.concat(Arrays.stream(result), Arrays.stream(result2))
                        .toArray(DFAgentDescription[]::new);

                for (DFAgentDescription DFD : merged) {
                    AID agentID = DFD.getName();
                    if (!agentID.equals(myAgent.getAID())) {
                        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                        inform.addReceiver(agentID);
                        inform.setContent("coordinator");
                        myAgent.send(inform);
                        logger.info(myAgent.getLocalName() + ": send coordinator inform to "
                                + agentID.getLocalName());
                    }
                }

                DFAgentDescription DFD = new DFAgentDescription();
                DFD.setName(myAgent.getAID());
                ServiceDescription SD = new ServiceDescription();
                SD.setType("coordinator");
                SD.setName(myAgent.getLocalName());
                DFD.addServices(SD);

                try {
                    DFService.deregister(myAgent);
                    logger.info(myAgent.getLocalName() + " deregistered in DF as a 'calculator' service.");
                    DFService.register(myAgent, DFD);
                    logger.info(myAgent.getLocalName() + " registered in DF as a 'coordinator' service.");
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                coordinator = 4;
                done = true;
                return;
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }

        ACLMessage msg = myAgent.receive();

        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                logger.info(myAgent.getLocalName() + ": received reject from "
                        + msg.getSender().getLocalName());
                done = true;
                return;
            }

            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                myAgent.send(reply);
                logger.info(myAgent.getLocalName() + ": rejected proposal from "
                        + msg.getSender().getLocalName());
            }
        } else {
            block(1000);
        }
    }

    @Override
    public int onEnd() {
        firstRun = true;
        done = false;
        int value = coordinator;
        coordinator = 3;
        return value;
    }

    @Override
    public boolean done() {
        return done;
    }

}
