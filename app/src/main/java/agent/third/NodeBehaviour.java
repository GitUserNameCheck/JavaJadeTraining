package agent.third;

import java.util.Arrays;
import java.util.HashSet;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class NodeBehaviour extends CyclicBehaviour{

    Logger logger = null;
    HashSet<String> neighbours;


    public NodeBehaviour(Agent parent, HashSet<String> set) {
        super(parent);
        this.neighbours = set;
        logger = Logger.getMyLogger(getClass().getName() + "@" + parent.getLocalName());
    }

    @Override
    public void action() {

        ACLMessage msg = myAgent.receive();

        if (msg != null) {

            if (msg.getPerformative() == ACLMessage.REQUEST) {

                String content = msg.getContent();

                if (content != null){

                    String nodeInSearch, toReply;
                    boolean originalRequest = false;
                    String[] lines = content.split("\\r?\\n");


                    if(lines[0].contains(",")){
                        String[] tmp = lines[0].split(",");
                        nodeInSearch = tmp[0];
                        toReply = tmp[1];
                    } else {
                        nodeInSearch = lines[0];
                        toReply = msg.getSender().getLocalName();
                        originalRequest = true;
                    }

                    String agentList = String.join("\n", Arrays.copyOfRange(lines, 1, lines.length)) + "\n" + myAgent.getLocalName() + "\n";
                    

                    if(myAgent.getLocalName().equals(nodeInSearch)){
                        
                        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                        reply.addReceiver(new AID(toReply, AID.ISLOCALNAME));
                        reply.setContent(agentList);
                        myAgent.send(reply);

                        logger.info(myAgent.getLocalName() + ": replied to " + toReply + "\n" + agentList);
                        return;
                    }

                    if (neighbours.size() == 0 || Arrays.asList(lines).contains(myAgent.getLocalName())) {
                        ACLMessage reply = new ACLMessage(ACLMessage.DISCONFIRM);
                        reply.addReceiver(new AID(toReply, AID.ISLOCALNAME));
                        reply.setContent(agentList);
                        myAgent.send(reply);

                        logger.info(myAgent.getLocalName() + ": replied to " + toReply + " disconfirm\n" + agentList);
                        return;
                    }

                    for (String neighbour : neighbours) {
                        if (!neighbour.equals(msg.getSender().getLocalName())) {
                            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                            request.addReceiver(new AID(neighbour, AID.ISLOCALNAME));
                            String req;
                            if(originalRequest){
                                req = nodeInSearch + ", " + toReply + "\n" + myAgent.getLocalName() + "\n";
                            }else{
                                req = content + myAgent.getLocalName() + "\n";
                            }
                            request.setContent(req);
                            myAgent.send(request);

                            logger.info(myAgent.getLocalName() + ": send to " + neighbour + "\n" + req);
                        }
                    }
                }
            }

        } else {
            block();
        }

    }


}
