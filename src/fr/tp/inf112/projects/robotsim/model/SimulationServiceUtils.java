package fr.tp.inf112.projects.robotsim.model;

public class SimulationServiceUtils {
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "simulation-";

    public static String getTopicName(final Factory factoryModel) {
        return TOPIC + factoryModel.getId();
    }
}
