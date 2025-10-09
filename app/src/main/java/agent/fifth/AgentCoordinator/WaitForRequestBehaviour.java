package agent.fifth.AgentCoordinator;

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
import jade.util.Logger;

public class WaitForRequestBehaviour extends Behaviour {

    Logger logger = null;

    public WaitForRequestBehaviour(Agent parent) {
        super(parent);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    };

    @Override
    public void action() {

        ACLMessage msg = myAgent.receive();

        if (msg != null) {

            if (msg.getPerformative() == ACLMessage.REQUEST && "sum".equals(msg.getLanguage())) {

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

                String content = msg.getContent();

                if (content != null && content.matches("\\d+\\s*,\\s*\\d+")) {
                    AID answerReceive = msg.getSender();
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
                }

                getDataStore().put("gotRequest", true);
            }
            
        } else {
            block();
        }
    }

    @Override
    public boolean done() {
        return getDataStore().containsKey("gotRequest");
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
