package org.academiadecodigo.notorbios.pedrov.snakemultiplayer.settings;

import org.academiadecodigo.notorbios.pedrov.snakemultiplayer.game.awards.Apple;
import org.academiadecodigo.notorbios.pedrov.snakemultiplayer.game.awards.Award;
import org.academiadecodigo.notorbios.pedrov.snakemultiplayer.game.awards.Freezer;
import org.academiadecodigo.notorbios.pedrov.snakemultiplayer.game.awards.TypeAward;
import org.academiadecodigo.notorbios.pedrov.snakemultiplayer.game.snake.Direction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;

public class Sockets {

    private ServerSocket server;
    private Socket connection;

    private ObjectInputStream get;
    private ObjectOutputStream post;

    public Sockets() {
        if (Settings.isP1) {
            try {
                server = new ServerSocket(Settings.portTCP);
                connection = server.accept();

                post = new ObjectOutputStream(connection.getOutputStream());
                get = new ObjectInputStream(connection.getInputStream());

                post.writeObject(Settings.numColumns + ";" + Settings.numRows + ";" + Settings.velocity);
                post.flush();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "[#00] Unexpected error.", "ERROR", JOptionPane.ERROR_MESSAGE);

                System.exit(0);
            }
        } else {
            try {
                connection = new Socket(Settings.IPOpponent, Settings.portTCP);

                get = new ObjectInputStream(connection.getInputStream());
                post = new ObjectOutputStream(connection.getOutputStream());

                String[] settings = ((String) get.readObject()).split(";");

                Settings.numColumns = Integer.parseInt(settings[0]);
                Settings.numRows = Integer.parseInt(settings[1]);
                Settings.velocity = Integer.parseInt(settings[2]);
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "[#01] No server with this adress was found.", "ERROR", JOptionPane.ERROR_MESSAGE);

                System.exit(0);
            }
        }
    }

    public void sendStartGame() {
        try {
            post.writeObject(true);
            post.flush();
        } catch (IOException e) {
            errorLostConnection("02");
        }
    }

    public void readStartGame() {
        try {
            get.readObject();
        } catch (IOException | ClassNotFoundException e) {
            errorLostConnection("03");
        }
    }

    public void sendMove(Direction direction) {
        try {
            post.writeObject(direction);
            post.flush();
        } catch (IOException e) {
            errorLostConnection("04");
        }
    }

    public Direction readMove() {
        try {
            return (Direction) get.readObject();
        } catch (IOException | ClassNotFoundException e) {
            errorLostConnection("05");
        }

        return null;
    }

    public void sendAward(Award award) {
        try {
            String send = ((award.getTypeAward() == TypeAward.APPLE) ? "A" : "F") + ";" + award.getX() + ";" + award.getY();

            post.writeObject(send);
            post.flush();
        } catch (IOException e) {
            errorLostConnection("06");
        }
    }

    public Award readAward() {
        Award award = null;

        try {
            String[] receivedAward = ((String) get.readObject()).split(";");

            if (receivedAward[0].equals("A")) {
                award = new Apple(Integer.valueOf(receivedAward[1]), Integer.valueOf(receivedAward[2]));
            } else {
                award = new Freezer(Integer.valueOf(receivedAward[1]), Integer.valueOf(receivedAward[2]));
            }
        } catch (IOException | ClassNotFoundException e) {
            errorLostConnection("07");
        }

        return award;
    }

    public void close() {
        try {
            post.close();
            get.close();
            connection.close();

            if (Settings.isP1) {
                server.close();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "[#08] Unexpected error.", "ERROR", JOptionPane.ERROR_MESSAGE);

            System.exit(0);
        }
    }

    private void errorLostConnection(String idError) {
        JOptionPane.showMessageDialog(null, "[#" + idError + "] Unexpected error.\nPossibly you/yours opponent has lost the connection.", "ERROR", JOptionPane.ERROR_MESSAGE);

        System.exit(0);
    }

}