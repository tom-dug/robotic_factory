package fr.tp.inf112.projects.robotsim.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

public class RemoteFactoryPersistenceManager extends AbstractCanvasPersistenceManager {
    private static final Logger LOGGER = Logger.getLogger(RemoteFactoryPersistenceManager.class.getName());

    private final String host;
    private final int port;

    public RemoteFactoryPersistenceManager(CanvasChooser canvasChooser, String host, int port) {
        super(canvasChooser);
        this.host = host;
        this.port = port;
    }

    @Override
    public Canvas read(String CanvasId) {
        try (Socket socket = new Socket(host, port);
                ObjectOutputStream objOutStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objInpStream = new ObjectInputStream(socket.getInputStream())) {
            objOutStream.flush();

            objOutStream.writeObject(CanvasId);
            objOutStream.flush();

            Object receivedObj = objInpStream.readObject();

            if (receivedObj instanceof Canvas) {
                return (Canvas) receivedObj;
            } else {
                LOGGER.log(Level.WARNING, "L'objet reçu n'est pas de type Canvas", receivedObj);
                return null;
            }

        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du read : ", e);
            return null;
        }
    };


    public void persist(Canvas canvas) {
        try (Socket socket = new Socket(host, port);
                ObjectInputStream ObjInStr = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream ObjOutStr = new ObjectOutputStream(socket.getOutputStream());
        ) {
            // TODO : InputStream not needed ?
            ObjOutStr.flush();

            ObjOutStr.writeObject(canvas);
            ObjOutStr.flush();

            LOGGER.log(Level.FINE, "Tentative de persist complétée");
            
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Persist error : ", e);
        }
    }

    public boolean delete(Canvas canvas) {
        return false;
    }
}
