import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ReceiveThread extends Thread {

    private final Client client;
    private final BufferedReader in;

    public ReceiveThread(Client client, Socket socket) throws IOException {
        this.client = client;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public void run() {
        try {
            while (client.isOnline()) {
                String result = in.readLine();
                if (result == null) {
                    client.offLine();
                }
                if ("##SERVER_CMD:LOGOUT##".equals(result)) {
                    client.offLine();
                    break;
                } else {
                    System.out.println(result);
                }
            }
        } catch (IOException e) {
            System.out.println("服务器关闭");
        } finally {
            try {
                if (in != null)
                    in.close();
                client.offLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
