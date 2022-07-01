import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) {

        Socket socket = null;
        OutputStream os = null;
        // 要知道服务器的地址
        try {
            JSON config = new JSON("config.json");
            String ip;
            if (config.get("MODE").equals("LOCAL"))
                ip = config.get("LOCAL_IP");
            else
                ip = config.get("SERVER_IP");
            InetAddress serverIp = InetAddress.getByName(ip);
            int port = Integer.parseInt(config.get("PORT"));

            // 连接 ip 和端口
            socket = new Socket(serverIp, port);

            // IO
            os = socket.getOutputStream();
            os.write("你好hello".getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
