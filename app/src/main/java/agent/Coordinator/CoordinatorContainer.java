package agent.Coordinator;

import agent.Coordinator.AgentCalculator.AgentCalculator;
import agent.Coordinator.AgentClient.AgentClient;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class CoordinatorContainer {
        public static void main(String[] args) throws StaleProxyException {

        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();

        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "1099");
        p.setParameter(Profile.CONTAINER_NAME, "Coordinator-Container");

        AgentContainer container = rt.createAgentContainer(p);

        AgentController client = container.createNewAgent("client", AgentClient.class.getName(), null);

        client.start();

        for (int i = 1; i <= 5; i++) {
            container
                    .createNewAgent("calculator" + i, AgentCalculator.class.getName(), null)
                    .start();
        }
    }
}
