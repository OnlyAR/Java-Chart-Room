import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;

public class SendThread extends Thread {
    private final LinkedList<Sent> sentList;

    public SendThread(LinkedList<Sent> sentList) {
        this.sentList = sentList;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (sentList.isEmpty()) {
                    Thread.sleep(200);
                } else {
                    Sent sent = sentList.removeFirst();
                    sent.send();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}

