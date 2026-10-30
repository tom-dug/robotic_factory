package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.graph.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;

import fr.tp.inf112.projects.canvas.controller.Observer;

public class RemoteSimulatorController extends SimulatorController {
    private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());

    private Factory factory;
    private final ObjectMapper objectMapper;

    public RemoteSimulatorController(final CanvasPersistenceManager persistenceManager) {
        this(null, persistenceManager);
    }

    public RemoteSimulatorController(final Factory factoryModel,
            final CanvasPersistenceManager persistenceManager) {
        super(factoryModel, persistenceManager);
        objectMapper = new ObjectMapper();
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(PositionedShape.class.getPackageName())
                .allowIfSubType(Component.class.getPackageName())
                .allowIfSubType(BasicVertex.class.getPackageName())
                .allowIfSubType(ArrayList.class.getName())
                .allowIfSubType(LinkedHashSet.class.getName())
                .build();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    public Factory getFactory() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        final URI uri = new URI("http", null, "localhost", 8080, "/simulation/" + getCanvas().getId(),
                null, null);

        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        factory = objectMapper.readValue(response.body(), Factory.class);
        return factory;
    }

    private void updateViewer() throws InterruptedException, URISyntaxException, IOException {
        while (((Factory) getCanvas()).isSimulationStarted()) {
            Factory remoteFactoryModel = getFactory();
            setCanvas(remoteFactoryModel);
            Thread.sleep(100);
        }
    }

    @Override
    public void setCanvas(final Canvas canvasModel) {
        final List<Observer> observers = ((Factory) getCanvas()).getObservers();
        super.setCanvas(canvasModel);
        for (final Observer observer : observers) {
            ((Factory) getCanvas()).addObserver(observer);
        }
        ((Factory) getCanvas()).notifyObservers();
    }

    @Override
    public void startAnimation() {
        HttpClient httpClient = HttpClient.newHttpClient();
        URI uri;
        try {
            uri = new URI("http", null, "localhost", 8080, "/start/" + getCanvas().getId(), null, null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (Boolean.parseBoolean(response.body())) {
                setCanvas(getFactory());
                updateViewer();
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.warning("Error starting the animation: " + e.getMessage());
        }

    }

    @Override
    public void stopAnimation() {
        HttpClient httpClient = HttpClient.newHttpClient();
        URI uri;
        try {
            uri = new URI("http", null, "localhost", 8080, "/stop/" + getCanvas().getId(), null, null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (Boolean.parseBoolean(response.body())) {
                setCanvas(getFactory());
            }
        } catch (Exception e) {
            LOGGER.warning("Error stopping the animation: " + e.getMessage());
        }
    }
}
