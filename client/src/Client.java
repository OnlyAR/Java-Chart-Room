import java.io.IOException;
import java.net.Socket;

public class Client {

    private final String SERVER_IP = "81.70.132.82";
//    private final String SERVER_IP = "127.0.0.1";
    private final int PORT = 8000;
    private boolean online;

    public Client() {
        Socket socket;
        try {
            socket = new Socket(SERVER_IP, PORT);
            ReceiveThread receive = new ReceiveThread(this, socket);
            SendThread send = new SendThread(this, socket);
            online = true;
            receive.start();
            send.start();

            while (online) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("输入任意内容退出");
            socket.close();
        } catch (IOException e) {
            System.out.println("服务器关闭");
        }
    }

    public boolean isOnline() {
        return online;
    }

    public void offLine() {
        online = false;
    }

    public static void main(String[] args) {
        System.out.println("请输入您的昵称：");
        new Client();
    }
}
