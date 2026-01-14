package agent.GeneticAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class AgentIndividual extends Agent {


    Logger logger = Logger.getMyLogger(getClass().getName());

    public AgentIndividual(){
        super();
        logger = Logger.getMyLogger(getClass().getName() + "@" + getLocalName());
    }

    private int[] genome;
    private static final int[] TARGET = { 0, 1, 4, 9, 16, 25, 36, 49, 64, 81, 100 };
    private static final int MAX_RAND_RANGE = 105;

    private static final double DEATH_PROB = 0;
    private static final double BREED_PROB = 0.5;
    private static final double REJECT_BREED_PROB = 0.1;
    private static final double MUTATION_PROB = 0.3;
    private static final double MUTATION_DISTANCE = 10;

    private static final Long ticker_timer = 1000L;
    private static final Long fitness_timer = 100L;

    private Random rnd = new Random();

    private TickerBehaviour ticker;
    private CyclicBehaviour messageListener;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        genome = (args != null && args.length > 0)
                ? (int[]) args[0]
                : randomGenome(TARGET.length);

        // logger.info(getLocalName() + ": born with fitness " + fitness());

        initBehaviors();
    }

    private void initBehaviors() {
        messageListener = new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) {
                    block();
                    return;
                }

                switch (msg.getPerformative()) {
                    case ACLMessage.REQUEST:
                        if ("fitness".equals(msg.getContent()))
                            replyFitness(msg);
                        break;
                    case ACLMessage.PROPOSE:
                        handleBreedRequest(msg);
                        break;
                    case ACLMessage.ACCEPT_PROPOSAL:
                        int[] mateGenome = decodeGenome(msg.getContent());
                        createChildWithGenome(mateGenome);
                        break;
                }
            }
        };

        ticker = new TickerBehaviour(this, ticker_timer) {
            @Override
            protected void onTick() {
                maybeDie();
                attemptBreeding();
            }
        };

        addBehaviour(messageListener);
        addBehaviour(ticker);
    }

    private void attemptBreeding() {
        if (rnd.nextDouble() < BREED_PROB) {
            try {
                List<AID> mates = discoverMates();
                if (mates.isEmpty())
                    return;

                AID mate = selectMate(mates);
                if (mate == null)
                    return;

                ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
                propose.addReceiver(mate);
                propose.setContent("breed");
                send(propose);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleBreedRequest(ACLMessage msg) {

        if (rnd.nextDouble() < REJECT_BREED_PROB) {
            ACLMessage reject = msg.createReply();
            reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
            send(reject);
            // logger.info(getLocalName() + ": rejected breeding proposal from " + msg.getSender().getLocalName());
            return;
        }

        ACLMessage accept = msg.createReply();
        accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        accept.setContent(encodeGenome());
        send(accept);

        // logger.info(getLocalName() + ": accepted breeding from " + msg.getSender().getLocalName());
    }

    private void createChildWithGenome(int[] mateGenome) {

        int[] child = onePointCrossover(genome, mateGenome);
        mutate(child);

        try {
            String name = "ind-" + UUID.randomUUID();

            getContainerController().createNewAgent(
                    name,
                    AgentIndividual.class.getName(),
                    new Object[] { child }).start();

            // logger.info(getLocalName() + ": child " + name + " is born");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void maybeDie() {

        double currentDeathChance = DEATH_PROB * fitness();
        currentDeathChance = Math.min(currentDeathChance, 0.1);

        if (rnd.nextDouble() < DEATH_PROB) {
            doDelete();
            // logger.info(getLocalName() + ": died");
        }
    }

    private int[] onePointCrossover(int[] a, int[] b) {
        int point = rnd.nextInt(a.length - 1) + 1;
        int[] child = new int[a.length];

        for (int i = 0; i < a.length; i++) {
            child[i] = (i < point) ? a[i] : b[i];
        }
        return child;
    }


    private void mutate(int[] g) {
        for (int i = 0; i < g.length; i++) {
            if (rnd.nextDouble() < MUTATION_PROB) {
                double delta = (rnd.nextDouble() * 2 * MUTATION_DISTANCE) - MUTATION_DISTANCE;
                g[i] += Math.round(delta);
            }
        }
    }

    // private void mutate(int[] g) {
    //     if (rnd.nextDouble() < MUTATION_PROB) {
    //         int i = rnd.nextInt(g.length);
    //         g[i] += rnd.nextBoolean() ? 1 : -1;
    //     }
    // }

    private List<AID> discoverMates() {


        ContainerID myContainer = (ContainerID) here();
        List<AID> mates = new ArrayList<>();

        QueryAgentsOnLocation query = new QueryAgentsOnLocation();
        query.setLocation(myContainer);

        Action a = new Action(getAMS(), query);

        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(getAMS());
        request.setOntology(JADEManagementOntology.getInstance().getName());
        request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);

        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        getContentManager().registerLanguage(new SLCodec());

        try {
            getContentManager().fillContent(request, a);
            ACLMessage response = FIPAService.doFipaRequestClient(this, request);

            // String str_mates = getLocalName() + "\nprinting mates\n";

            if (response != null && response.getPerformative() == ACLMessage.INFORM) {
                Result res = (Result) getContentManager().extractContent(response);
                Iterator it = res.getItems().iterator();
                while (it.hasNext()) {
                    Object item = it.next();
                    if (item instanceof AID) {
                        AID agentAID = (AID) item;
                        if (!agentAID.equals(getAID())) {
                            // str_mates = str_mates + "mate " + agentAID.getLocalName() + "\n";
                            mates.add(agentAID);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        // logger.info(str_mates);

        return mates;
    }

    private AID selectMate(List<AID> mates) {

        List<AID> valid = new ArrayList<>();
        List<Double> weights = new ArrayList<>();

        for (AID a : mates) {
            Double f = requestFitness(a);
            if (f != null && f > 0) {
                valid.add(a);
                weights.add(1.0 / f);
            }
        }

        if (valid.isEmpty())
            return null;

        double sum = weights.stream().mapToDouble(Double::doubleValue).sum();
        double r = rnd.nextDouble() * sum;

        double acc = 0;
        for (int i = 0; i < valid.size(); i++) {
            acc += weights.get(i);
            if (acc >= r)
                return valid.get(i);
        }
        return valid.get(0);
    }

    private void replyFitness(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(String.valueOf(fitness()));
        send(reply);
    }

    private Double requestFitness(AID aid) {

        try {
            String cid = "fitness-" + UUID.randomUUID();

            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(aid);
            req.setContent("fitness");
            req.setConversationId(cid);
            send(req);

            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchConversationId(cid),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));

            ACLMessage reply = blockingReceive(mt, fitness_timer);

            if (reply == null) {
                return null;
            }

            return Double.parseDouble(reply.getContent());

        } catch (Exception e) {
            return null;
        }
    }

    private double fitness() {
        double sum = 0;
        for (int i = 0; i < genome.length; i++) {
            sum += Math.pow(genome[i] - TARGET[i], 2);
        }
        return Math.sqrt(sum);
    }

    private int[] randomGenome(int n) {
        int[] g = new int[n];
        for (int i = 0; i < n; i++) {
            g[i] = rnd.nextInt(MAX_RAND_RANGE);
        }
        return g;
    }

    private String encodeGenome() {
        return Arrays.stream(genome)
                .mapToObj(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    private int[] decodeGenome(String content) {
        return Arrays.stream(content.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    protected void takeDown() {
        if (ticker != null) {
            ticker.stop();
        }
    }
    
    @Override
    protected void beforeMove() {
        if (ticker != null) {
            ticker.stop();
        }
        removeBehaviour(ticker);
        removeBehaviour(messageListener);
        logger.info(getLocalName() + ": moving to new container");
    }

    @Override
    protected void afterMove() {
        logger.info(getLocalName() + ": arrived at new container. Restarting timers");
        initBehaviors();
    }

}