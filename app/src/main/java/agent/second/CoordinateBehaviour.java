package agent.second;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class CoordinateBehaviour extends CyclicBehaviour{

    Logger logger = null;
    HashMap<String, Boolean> agents;

    Boolean busy = false;
    AID answerReceiverName = null;
    int answer = 0;
    int agentNumber = 0;

    public CoordinateBehaviour(Agent parent, HashMap<String, Boolean> map){
        super(parent);
        this.agents = map;
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    public void action() {

        ACLMessage msg = myAgent.receive();

        if (msg != null) {

            if (msg.getPerformative() == ACLMessage.REQUEST && "sum".equals(msg.getLanguage()) && !busy) {

                String content = msg.getContent();

                if (content != null && content.matches("\\d+\\s*,\\s*\\d+")){
                    busy = true;
                    answerReceiverName = msg.getSender();
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
                        logger.info(myAgent.getLocalName() + ": interval [" + interval[0] + "," + interval[1]+ "] to " + agentName);
                    }
                }
            }


            if (msg.getPerformative() == ACLMessage.CONFIRM && agents.containsKey(msg.getSender().getLocalName()) 
                                                            && !agents.get(msg.getSender().getLocalName())){
                
                String content = msg.getContent();

                if (content != null && content.matches("\\d+")){
                    int sum = Integer.parseInt(content);
                    agents.put(msg.getSender().getLocalName(), true);
                    agentNumber++;
                    answer += sum;

                    logger.info(myAgent.getLocalName() + ": " + "got " + sum +" from " + msg.getSender().getLocalName());

                    if(agentNumber == agents.size()){
                        ACLMessage ans = new ACLMessage(ACLMessage.CONFIRM);
                        ans.addReceiver(answerReceiverName);
                        ans.setContent(String.valueOf(answer));
                        myAgent.send(ans);

                        logger.info("answer " + answer + " sent to " + answerReceiverName.getLocalName());

                        agentNumber = 0;
                        answer = 0;
                        answerReceiverName = null;
                        busy = false;
                        agents.replaceAll((k, v) -> false);
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

}
