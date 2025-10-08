package agent.fourth.AgentClient;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class AgentClientBehaviour extends CyclicBehaviour {

    Logger logger = null;

    AID answerReceiverName = null;

    public AgentClientBehaviour(Agent parent) {
        super(parent);
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    public void action() {

        ACLMessage msg = myAgent.receive();

        if (msg != null) {

            if (msg.getPerformative() == ACLMessage.REQUEST && "sum".equals(msg.getLanguage())) {

                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("coordinator");
                template.addServices(sd);

                String content = msg.getContent();

                if (content != null && content.matches("\\d+\\s*,\\s*\\d+")) {

                    answerReceiverName = msg.getSender();

                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);


                        for (DFAgentDescription DFD : result) {
                            AID agentID = DFD.getName();

                            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                            request.addReceiver(agentID);
                            request.setLanguage("sum");
                            request.setContent(content);
                            myAgent.send(request);

                            logger.info(myAgent.getLocalName() + ": interval [" + content + "] to "
                                            + agentID.getLocalName());
                        }

                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                }
            }

            if (msg.getPerformative() == ACLMessage.CONFIRM) {

                String content = msg.getContent();

                if (content != null && content.matches("\\d+")) {
                    int ans = Integer.parseInt(content);

                    logger.info(myAgent.getLocalName() + ": " + "got " + ans + " from " + msg.getSender().getLocalName());

                    ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                    reply.addReceiver(answerReceiverName);
                    reply.setContent(String.valueOf(ans));
                    myAgent.send(reply);

                    logger.info(myAgent.getLocalName() + ": answer " + ans + " sent to " + answerReceiverName.getLocalName());
                    
                }
            }

        } else {
            block();
        }

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
