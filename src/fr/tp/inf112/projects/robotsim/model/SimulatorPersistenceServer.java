package fr.tp.inf112.projects.robotsim.model;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.app.SimulatorApplication;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SimulatorPersistenceServer implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(SimulatorPersistenceServer.class.getName());
    private Socket socket;
    private final FactoryPersistenceManager usedPersistenceManager = new FactoryPersistenceManager(null);

    public SimulatorPersistenceServer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (   ObjectOutputStream objOutStr = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objInStr = new ObjectInputStream(socket.getInputStream());) {
            Object receivedObj = objInStr.readObject();

            if (receivedObj instanceof String) {
                String identifier = (String) receivedObj;
                if (identifier.equals("LIST")) {
                    // Used to display the list of available 
                    // Creating a file object to get all present file's names 
                    File f = new File(".");
                    String[] files = f.list();
                    objOutStr.writeObject(files);
                } else {
                    Object canvas = usedPersistenceManager.read(identifier);
                    objOutStr.writeObject(canvas);
                }
            }

            else if (receivedObj instanceof Factory) {
                Factory factory = (Factory) receivedObj;
                usedPersistenceManager.persist(factory);
                objOutStr.writeObject(receivedObj);
            }

            objOutStr.flush();

        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Erreur à la réception : ", e);
        }

        finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur à la fermeture du socket : ", e);
            }
        }

    }
}