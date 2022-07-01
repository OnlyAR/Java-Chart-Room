import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SendThread extends Thread {

    private final Client client;
    private final BufferedWriter out;
    private final BufferedReader stdin;

    public SendThread(Client client, Socket socket) throws IOException {
        this.client = client;
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        this.stdin = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {
        try {
            while (client.isOnline()) {
                String message = stdin.readLine();
                out.write(message + "\n");
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("退出成功");
        } finally {
            try {
                stdin.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
