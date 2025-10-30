package agent.Coordinator.AgentClient;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class AgentClientBehaviour extends Behaviour {

    Logger logger = null;

    AID answerReceiverName = null;

    boolean done = false;

    private ThreadedBehaviourFactory tbf;

    public AgentClientBehaviour(Agent parent, ThreadedBehaviourFactory tbf) {
        super(parent);
        this.tbf = tbf;
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

                        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                        cfp.setProtocol("fipa-contract-net");
                        cfp.setContent(content);

                        for (DFAgentDescription DFD : result) {
                            cfp.addReceiver(DFD.getName());
                        }

                        myAgent.addBehaviour(tbf.wrap(new InitiatorBehaviour(myAgent, cfp, msg.getSender())));
                        done = true;
                        logger.info("INITIATOR IS WORKING");

                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
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

    @Override
    public boolean done() {
        // if (done) {
        //     done = false;
        //     return true;
        // }
        return done;
    }

}
