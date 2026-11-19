package fr.tp.inf112.projects.robotsim.app;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.SimulationServiceUtils;

public class FactorySimulationEventConsumer {
    private static final Logger LOGGER = Logger.getLogger(FactorySimulationEventConsumer.class.getName());

    private final KafkaConsumer<String, String> consumer;
    private final RemoteSimulatorController controller;

    public FactorySimulationEventConsumer(final RemoteSimulatorController controller) {
        this.controller = controller;
        final Properties props = SimulationServiceUtils.getDefaultConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        this.consumer = new KafkaConsumer<>(props);
        final String topicName = SimulationServiceUtils.getTopicName((Factory) controller.getCanvas());
        this.consumer.subscribe(Collections.singletonList(topicName));
    }

    public void consumeMessages() {
        try {
            while (controller.isAnimationRunning()) {
                final ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (final ConsumerRecord<String, String> record : records) {
                    LOGGER.fine("Received JSON Factory text '" + record.value() + "'.");
                    controller.setCanvas(record.value());
                }
            }
        } finally {
            consumer.close();
        }
    }
}
