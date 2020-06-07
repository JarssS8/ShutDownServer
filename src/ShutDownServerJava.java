/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.Action;


/**
 * @author Adrian
 */
public class ShutDownServerJava {

    private static final Logger LOGGER = Logger.getLogger("shutdownserverjava.ShutDownServerJava");
    private static ThreadWaiter threadWaiter = null;
    private static int counter = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(3000);
            LOGGER.info("Server online, now accept connections...");
            while (true) {
                Socket socket = serverSocket.accept();
                if(counter == 0) {
                    threadWaiter = new ThreadWaiter(socket);
                    threadWaiter.start();
                    counter = 1;

                }else{
                    threadWaiter.interrupt();

                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ShutDownServerJava.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        ShutDownServerJava.counter = counter;
    }
}
