import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;


public class Server{

    final private int PORT = 8000;
    final private LinkedList<User> userList = new LinkedList<>();
    final private LinkedList<ChartRoom> chartRoomList = new LinkedList<>();
    final private LinkedList<Received> receivedList = new LinkedList<>();
    final private LinkedList<Sent> sentList = new LinkedList<>();
    private int cntRoom = 0;

    public Server() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            new ReceiveThread(receivedList).start();
            new SendThread(sentList).start();
            while (true) {
                userList.removeIf(u -> !u.isOnline());
                Socket socket = serverSocket.accept();
                System.out.println("新用户加入");
                User newUser = new User(socket, this);
                userList.add(newUser);
                newUser.start();
            }
        }
    }

    public LinkedList<User> getUserList() {
        return userList;
    }

    public static void main(String[] args) {
        try {
            new Server();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<ChartRoom> getChartRoomList() {
        return chartRoomList;
    }

    public ChartRoom createChatroom(User src, User dst) {
        ChartRoom room = new ChartRoom(this, cntRoom, true);
        cntRoom++;
        chartRoomList.add(room);
        room.addUser(src);
        room.addUser(dst);
        return room;
    }

    public ChartRoom createPublicChatroom(User owner) {
        ChartRoom room = new ChartRoom(this, cntRoom++, owner);
        room.addUser(owner);
        chartRoomList.add(room);
        return room;
    }

    public LinkedList<Received> getReceivedList() {
        return receivedList;
    }

    public LinkedList<Sent> getSentList() {
        return sentList;
    }
}
