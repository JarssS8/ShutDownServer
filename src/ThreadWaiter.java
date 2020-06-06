import java.util.concurrent.TimeUnit;

public class ThreadWaiter extends Thread{
    private int seconds;

    public ThreadWaiter (int seconds){
        this.seconds = seconds;
    }

    @Override
    public synchronized void run() {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
