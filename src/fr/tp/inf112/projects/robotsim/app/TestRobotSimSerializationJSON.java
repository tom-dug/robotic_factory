package fr.tp.inf112.projects.robotsim.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.graph.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Area;
import fr.tp.inf112.projects.robotsim.model.Battery;
import fr.tp.inf112.projects.robotsim.model.ChargingStation;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Conveyor;
import fr.tp.inf112.projects.robotsim.model.Door;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.Machine;
import fr.tp.inf112.projects.robotsim.model.Robot;
import fr.tp.inf112.projects.robotsim.model.Room;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.JGraphTDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.BasicPolygonShape;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

public class TestRobotSimSerializationJSON {
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = Logger.getLogger(TestRobotSimSerializationJSON.class.getName());
    final Factory myFactory = new Factory(200, 200, "Simple Test Puck Factory", 5);

    public TestRobotSimSerializationJSON() {
        objectMapper = new ObjectMapper();
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(PositionedShape.class.getPackageName())
                .allowIfSubType(Component.class.getPackageName())
                .allowIfSubType(BasicVertex.class.getPackageName())
                .allowIfSubType(ArrayList.class.getName())
                .allowIfSubType(LinkedHashSet.class.getName())
                .build();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);

        
        final Room room1 = new Room(myFactory, new RectangularShape(20, 20, 75, 75), "Production Room 1");
        new Door(room1, Room.WALL.BOTTOM, 10, 20, true, "Entrance");
        final Area area1 = new Area(room1, new RectangularShape(35, 35, 50, 50), "Production Area 1");
        new Machine(area1, new RectangularShape(50, 50, 15, 15), "Machine 1");

        final BasicPolygonShape conveyorShape = new BasicPolygonShape();
        final Conveyor conveyor1 = new Conveyor(myFactory, conveyorShape, "Conveyor 1");

		final Room chargingRoom = new Room(myFactory, new RectangularShape(125, 125, 50, 50), "Charging Room");
		new Door(chargingRoom, Room.WALL.RIGHT, 10, 20, false, "Entrance");
		final ChargingStation chargingStation = new ChargingStation(myFactory, new RectangularShape(150, 145, 15, 15), "Charging Station");

		final int factoryPathResolution = myFactory.getPathResolution();
		final FactoryPathFinder jgraphPahtFinder = new JGraphTDijkstraFactoryPathFinder(myFactory, factoryPathResolution);
        final Robot robot1 = new Robot(myFactory, jgraphPahtFinder, new CircularShape(5, 5, 2), new Battery(10), "Robot 1");
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        final String factoryAsJsonString = objectMapper.writeValueAsString(myFactory);
        LOGGER.info(factoryAsJsonString);
        final Factory roundTrip = objectMapper.readValue(factoryAsJsonString, Factory.class);
        LOGGER.info(roundTrip.toString());
        assertEquals(myFactory.getComponents().toString(), roundTrip.getComponents().toString());
    }

}
