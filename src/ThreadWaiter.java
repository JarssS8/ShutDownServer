
import model.Action;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadWaiter extends Thread{
    private int seconds;
    private final Logger LOGGER = Logger.getLogger("shutdownserverjava.ShutDownServerJava");
    private ObjectInputStream objectInputStream = null;
    private Socket socket = null;
    private Action action = null;
    private String serverOS = null;
    private boolean abort = false;

    public ThreadWaiter (Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        readMessage();
    }

    private void test() {
        InetAddress ip = null;
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String hostname = ip.getHostName();
        System.out.println(hostname+" "+ip.toString());
        serverOS = System.getProperty("os.name");
        System.out.println(serverOS);
        action = new Action();
        action.setTimeInSeconcds(10);
        action.setType(Action.ActionType.LOCK);
        executeLinuxScript(action);
    }

    private String readMessage() {
        String response = null;

        try {
            serverOS = System.getProperty("os.name");
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            LOGGER.info("Starting to read the message...");
            action = (Action) objectInputStream.readObject();
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

    private String executeWindowsScript(Action action) {
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

    private String executeLinuxScript(Action action) {
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

    private void executeFile(String command) {
        ProcessBuilder pb = null;
        try {
            LOGGER.info("Starting countdown...");
            TimeUnit.SECONDS.sleep(action.getTimeInSeconcds());
            if(!abort) {
                LOGGER.info("Countdown finished...");
                LOGGER.info("Executing command...");
                Runtime.getRuntime().exec(command);
                LOGGER.info("Done correct!");
            }else{
                LOGGER.info("Aborted");
            }
            ShutDownServerJava.setCounter(0);

        } catch (IOException ex) {
            Logger.getLogger(ShutDownServerJava.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex){
            LOGGER.info("Interrupted");
            ShutDownServerJava.setCounter(0);
        }

    }

    public boolean isAbort() {
        return abort;
    }

    public void setAbort(boolean abort) {
        this.abort = abort;
    }
}
