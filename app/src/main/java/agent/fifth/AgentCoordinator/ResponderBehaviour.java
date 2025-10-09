package agent.fifth.AgentCoordinator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.util.Logger;

public class ResponderBehaviour extends ContractNetResponder{

    Logger logger = null;

    int exitCode = 0;
    boolean finished = false;

    public ResponderBehaviour(Agent parent, MessageTemplate mt, DataStore store) {
        super(parent, mt, store);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        logger.info(myAgent.getLocalName() + " got CFP: " + cfp.getContent());

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);
        int cost = 10 + (int) (Math.random() * 90);
        propose.setContent(String.valueOf(cost));
        logger.info(myAgent.getLocalName() + " proposing cost=" + cost);
        return propose;
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
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

        String content = cfp.getContent();

        AID answerReceive = cfp.getSender();
        getDataStore().put("answerReceiver", answerReceive);

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
        return null;
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        logger.info(myAgent.getLocalName() + ": proposal rejected by initiator.");
    }
   
    // @Override
    // public boolean done() {
    //     return finished;
    // }

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
