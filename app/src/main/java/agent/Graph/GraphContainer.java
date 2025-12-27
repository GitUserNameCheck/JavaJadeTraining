package agent.Graph;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class GraphContainer {
    public static void main(String[] args) throws StaleProxyException {

        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();

        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "1099");
        p.setParameter(Profile.CONTAINER_NAME, "Graph-Container");

        AgentContainer container = rt.createAgentContainer(p);

        AgentController node1 = container.createNewAgent("node1", AgentNode.class.getName(), new Object[] {new String[] { "node2", "node5" }, "Graph-Container"});
        node1.start();
        AgentController node2 = container.createNewAgent("node2", AgentNode.class.getName(), new Object[] {new String[] { "node1", "node3" }, "Graph-Container"});
        node2.start();
        AgentController node3 = container.createNewAgent("node3", AgentNode.class.getName(), new Object[] {new String[] { "node2", "node4", "node6" }, "Graph-Container"});
        node3.start();
        AgentController node4 = container.createNewAgent("node4", AgentNode.class.getName(), new Object[] {new String[] { "node3", "node5" }, "Graph-Container"});
        node4.start();
        AgentController node5 = container.createNewAgent("node5", AgentNode.class.getName(), new Object[] {new String[] { "node1", "node4", "node8" }, "Graph-Container"});
        node5.start();
        AgentController node6 = container.createNewAgent("node6", AgentNode.class.getName(), new Object[] {new String[] { "node3" }, "Graph-Container"});
        node6.start();
        AgentController node7 = container.createNewAgent("node7", AgentNode.class.getName(), new Object[] {new String[] {}, "Graph-Container" });
        node7.start();
        AgentController node8 = container.createNewAgent("node8", AgentNode.class.getName(), new Object[] {new String[] { "node5" }, "Graph-Container"});
        node8.start();
    }
}
