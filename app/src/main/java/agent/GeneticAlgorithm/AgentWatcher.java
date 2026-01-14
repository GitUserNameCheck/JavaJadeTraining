package agent.GeneticAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;


public class AgentWatcher extends Agent {

    Logger logger = Logger.getMyLogger(getClass().getName());

    public AgentWatcher(){
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    private int POPULATION_LIMIT = 100;
    private long ticker_timer = 1000;


    private Map<String, AID> pendingFitness = new HashMap<>();
    private Map<AID, Double> lastFitness = new HashMap<>();

    private SLCodec codec = new SLCodec();
    private Ontology ontology = JADEManagementOntology.getInstance();

    @Override
    protected void setup() {
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        logger.info("AgentWatcher started");

        addBehaviour(new TickerBehaviour(this, ticker_timer) {
            @Override
            protected void onTick() {
                try {
                    discoverAndRequestFitness();
                    collectFitnessReplies();
                    logGlobalBest();
                    maybeKill();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

  
    private List<AID> discoverIndividuals() {
        AMSAgentDescription template = new AMSAgentDescription();
        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults(-1L);

        try {
            AMSAgentDescription[] result = AMSService.search(this, template, sc);

            return Arrays.stream(result)
                    .map(AMSAgentDescription::getName)
                    .filter(a -> a.getLocalName().startsWith("ind-"))
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
            return new ArrayList<AID>();
        }
    }

    private void discoverAndRequestFitness() {

        List<AID> agents = discoverIndividuals();

        for (AID a : agents) {
            if (lastFitness.containsKey(a))
                continue;

            String cid = "fitness-" + UUID.randomUUID();
            pendingFitness.put(cid, a);

            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(a);
            req.setContent("fitness");
            req.setConversationId(cid);
            send(req);
        }
    }

    private void collectFitnessReplies() {

        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage msg;

        while ((msg = receive(mt)) != null) {

            String cid = msg.getConversationId();
            AID sender = pendingFitness.remove(cid);

            if (sender == null)
                continue;

            try {
                double f = Double.parseDouble(msg.getContent());
                lastFitness.put(sender, f);
            } catch (Exception ignored) {}
        }
    }

    private void logGlobalBest() {

        if (lastFitness.isEmpty())
            return;

        Map.Entry<AID, Double> best = lastFitness.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .orElse(null);

        if (best != null) {
            logger.info("GLOBAL BEST = " + best.getValue() + " by " + best.getKey().getLocalName());
        }
    }


    private void maybeKill() {

        if (lastFitness.size() < POPULATION_LIMIT)
            return;

        List<Map.Entry<AID, Double>> sorted = new ArrayList<>(lastFitness.entrySet());

        sorted.sort(Map.Entry.comparingByValue());

        int toKill = sorted.size() / 2;

        for (int i = sorted.size() - 1; i >= toKill; i--) {
            killViaAMS(sorted.get(i).getKey());
            lastFitness.remove(sorted.get(i).getKey());
        }

        logger.info("Killed " + toKill + " agents");
    }

    private void killViaAMS(AID aid) {
        try {
             
            Codec codec = new SLCodec();
            getContentManager().registerLanguage(codec);
            getContentManager().registerOntology(JADEManagementOntology.getInstance());

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(getAMS());
            msg.setLanguage(codec.getName());
            msg.setOntology(JADEManagementOntology.getInstance().getName());

            KillAgent ka = new KillAgent();
            ka.setAgent(aid);

            getContentManager().fillContent(
                    msg,
                    new Action(getAID(), ka)
            );

            send(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}