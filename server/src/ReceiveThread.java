import java.io.IOException;
import java.util.LinkedList;

public class ReceiveThread extends Thread {
    private final LinkedList<Received> receivedList;

    public ReceiveThread(LinkedList<Received> receivedList) {
        this.receivedList = receivedList;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (receivedList.isEmpty()) {
                    Thread.sleep(200);
                } else {
                    Received received = receivedList.removeFirst();
                    received.src.handle(received.msg);
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
