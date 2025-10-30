package agent.Coordinator.AgentCoordinator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class ResponderBehaviourByHand extends Behaviour{
    Logger logger = null;

    MessageTemplate mtCFP;

    int exitCode = 0;
    boolean finished = false;

    ACLMessage cfp_last = null;

    public ResponderBehaviourByHand(Agent parent, MessageTemplate mt) {
        super(parent);
        this.mtCFP = mt;
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    public void action() {
        
        ACLMessage accept = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
        if (accept != null) {
            logger.info(myAgent.getLocalName() + " proposal accepted. Doing the job...");

            HashMap<String, Boolean> agents = new HashMap<String, Boolean>();
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("calculation");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);

                for (DFAgentDescription DFD : result) {
                    AID agentID = DFD.getName();
                    String localName = agentID.getLocalName();

                    agents.put(localName, false);
                }

                getDataStore().put("calculatorAgents", agents);

                logger.info(myAgent.getLocalName() + ": found calculator agents\n" + agents.toString());

            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            String content = cfp_last.getContent();

            getDataStore().put("acceptProposal", accept);

            String[] parts = content.split(",");
            int a = Integer.parseInt(parts[0].trim());
            int b = Integer.parseInt(parts[1].trim());

            List<int[]> intervals = splitInterval(a, b, agents.size());

            int i = 0;
            for (String agentName : agents.keySet()) {
                int[] interval = intervals.get(i++);

                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(new AID(agentName, AID.ISLOCALNAME));
                request.setLanguage("sum");
                request.setContent(interval[0] + "," + interval[1]);
                myAgent.send(request);
                logger.info(myAgent.getLocalName() + ": interval [" + interval[0] + "," + interval[1] + "] to "
                        + agentName);
            }
            exitCode = 1;
            finished = true;
            return;
        }

        ACLMessage cfp = myAgent.receive(mtCFP);
        if (cfp != null) {

            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            int cost = 10 + (int) (Math.random() * 90);
            propose.setContent(String.valueOf(cost));
            myAgent.send(propose);
            cfp_last = cfp;
            logger.info(myAgent.getLocalName() + " proposing cost=" + cost);

        } else {
            block(); 
        }
    }

    @Override
    public boolean done() {
        return finished;
    }

    @Override
    public int onEnd() {
        return exitCode;
    }

    @Override
    public void reset() {
        finished = false;
        exitCode = 0;
        super.reset();
    }

    public static List<int[]> splitInterval(int a, int b, int n) {
        List<int[]> intervals = new ArrayList<>();
        int totalNumbers = b - a + 1;
        int baseSize = totalNumbers / n;
        int remainder = totalNumbers % n;
        int start = a;

        for (int i = 0; i < n; i++) {
            int size = baseSize + (i < remainder ? 1 : 0);
            int end = start + size - 1;
            intervals.add(new int[] { start, end });
            start = end + 1;
        }
        return intervals;
    }


}
