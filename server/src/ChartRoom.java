import java.io.IOException;
import java.util.LinkedList;

public class ChartRoom{
    private final int id;
    private final Server server;
    private final boolean secret;
    private final LinkedList<User> userList = new LinkedList<>();
    private final User owner;

    public LinkedList<User> getUserList() {
        return userList;
    }

    public ChartRoom (Server server, int id, boolean secret){
        super();
        this.id = id;
        this.secret = secret;
        this.server = server;
        this.owner = null;
    }

    public ChartRoom (Server server, int id, User owner){
        super();
        this.id = id;
        this.owner = owner;
        this.server = server;
        this.secret = false;
    }

    public void addUser(User user) {
        userList.add(user);
    }

    public void removeUser(User user) {
        for (User u: userList) {
            if (u == user) {
                userList.remove(u);
                break;
            }
        }
        if (userList.size() == 0) {
            close();
        }
    }

    private void close() {
        LinkedList<ChartRoom> chartRooms = server.getChartRoomList();
        chartRooms.remove(this);
    }

    public int getId() {
        return id;
    }

    public boolean isSecret() {
        return secret;
    }

    public void broadcast(String username,  String msg) throws IOException {
        for (User u: userList) {
            u.send(String.format("[%s]: %s", username, msg));
        }
    }

    public int size() {
        return userList.size();
    }

    public User getOwner() {
        return owner;
    }

    public void removeChatRoom() throws IOException {
        if (owner != null) {
            broadcast(owner.getUsername(), "房间被解散");
        }
        for (User u: userList) {
            removeUser(u);
            u.returnMenu();
        }
    }
}
