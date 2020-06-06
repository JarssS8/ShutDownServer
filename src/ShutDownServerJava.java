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
    private static ObjectInputStream objectInputStream = null;
    private static Socket socket = null;
    private static Action action = null;
    private static String serverOS = null;
    private static ThreadWaiter threadWaiter = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //test();
            ServerSocket serverSocket = new ServerSocket(3000);
            while (true) {
                socket = serverSocket.accept();
                if(threadWaiter == null)
                    readMessage();
                else{
                    threadWaiter.stop();
                    threadWaiter = null;
                    LOGGER.info("Aborted action");
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ShutDownServerJava.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void test() {
        InetAddress ip = null;
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String hostname = ip.getHostName();
        System.out.println(hostname);
        serverOS = System.getProperty("os.name");
        System.out.println(serverOS);
        action = new Action();
        action.setTimeInSeconcds(10);
        action.setType(Action.ActionType.LOCK);
        threadWaiter = new ThreadWaiter(action.getTimeInSeconcds());
        executeLinuxScript(action);

    }

    private static String readMessage() {
        String response = null;

        try {
            serverOS = System.getProperty("os.name");
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            LOGGER.info("Starting to read the message...");
            action = (Action) objectInputStream.readObject();
            threadWaiter = new ThreadWaiter(action.getTimeInSeconcds());
            switch (serverOS) {
                case "Windows 10":
                    response = executeWindowsScript(action);
                    break;
                case "Linux":
                    response = executeLinuxScript(action);
                    break;
                default:
                    response = "Not recognised OS";
                    break;
            }

        } catch (IOException ex) {
            LOGGER.warning("ServerWorkerThread: IO exception" + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            LOGGER.warning("ServerWorkerThread: Class not found exception: " + ex.getMessage());
        } catch (Exception ex) {
            LOGGER.warning("ServerWorkerThread: Exception: " + ex.getMessage());
        }

        return response;
    }

    private static String executeWindowsScript(Action action) {
        String response = null;
        StringBuilder command = new StringBuilder();
        command.append("shutdown -t ").append(action.getTimeInSeconcds());
        switch (action.getType()) {
            case SHUT_DOWN:
                command.append(" -s");
                break;
            case REBOOT:
                command.append(" -r");
                break;
            case HIBERNATE:
                command = new StringBuilder();
                command.append("ping -n ").append(action.getTimeInSeconcds()).append(" 127.0.0.1 > NUL 2>&1 && shutdown /h /f");
                break;
            case LOCK:
                command = new StringBuilder();
                command.append("rundll32.exe user32.dll,LockWorkStation");
                break;
            case CANCEL:
                command = new StringBuilder();
                command.append("/a");
                break;

        }

        executeFile(command.toString());
        return response;
    }

    private static String executeLinuxScript(Action action) {
        String response = null;
        StringBuilder command = new StringBuilder();
        switch (action.getType()) {
            case SHUT_DOWN:
                command.append("shutdown -h now");
                break;
            case REBOOT:
                command.append("reboot");
                break;
            case HIBERNATE:
                command.append("systemctl hibernate");
                break;
            case LOCK:
                command.append("gnome-screensaver-command -l");
                break;
        }

        executeFile(command.toString());
        response = "OK";
        return response;
    }

    private static void executeFile(String command) {
        ProcessBuilder pb = null;
        try {
            LOGGER.info("Starting countdown...");
            threadWaiter.start();
            threadWaiter.join();
            LOGGER.info("Countdown finished...");
            threadWaiter = null;
            LOGGER.info("Executing command...");
            Runtime.getRuntime().exec(command);
            LOGGER.info("Done correct!");

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ShutDownServerJava.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


}
