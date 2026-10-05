package fr.tp.inf112.projects.robotsim.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import javax.management.InstanceNotFoundException;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

public class RemoteFileCanvasChooser extends FileCanvasChooser {
    private static Logger LOGGER = Logger.getLogger(RemoteFactoryPersistenceManager.class.getName());

    private final String host;
    private final int port;

    public RemoteFileCanvasChooser(final String fileExtension, final String documentTypeLabel, String host, int port) {
        super(fileExtension, documentTypeLabel);
        this.host = host;
        this.port = port;
    }

    protected String browseCanvases(final boolean open) {
        if (open) {
            String[] filesAvailable = filesList();
            
            if (filesAvailable.length == 0) {
                return null;
            }

            else {
                String chosenFile = JOptionPane.showInputDialog("Choisir un fichier", filesAvailable);
                return chosenFile;
            }
        }
        
        else {
            String userInput = JOptionPane.showInputDialog(null);
            return userInput;
        }
    }

    private String[] filesList() {
        try (Socket socket = new Socket(host, port);
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());) {
            oos.flush();

            oos.writeObject("LIST");
            oos.flush();

            Object response = ois.readObject();
            if (response instanceof String[]) {
                return (String[]) response;
            }

        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Erreur dans la liste de fichiers serveur : ", e);
        }
        return new String[0];
    }
}
