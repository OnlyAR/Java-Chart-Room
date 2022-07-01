import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class User extends Thread {

    private final Server server;
    private final String username;
    private final Socket client;
    private final BufferedWriter out;
    private final BufferedReader in;
    private User chatSrc = null;
    private ChartRoom chartRoom = null;

    private enum Status {
        OFFLINE, CHART, MENU, READY_FOR_REPLY, READY_TO_ANSWER, SUSPEND, READY_TO_KICK, JOIN_ROOM
    }

    private Status status;

    public BufferedWriter getOut() {
        return out;
    }

    public void returnMenu() {
        send("返回菜单");
        status = Status.MENU;
    }

    public User(Socket socket, Server server) throws IOException {
        this.server = server;
        this.client = socket;
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
        username = in.readLine();
        System.out.println("username: " + username);
        send("您好[" + username + "]，连接成功\n");
        help();
        status = Status.MENU;
    }

    @Override
    public void run() {
        while (status != Status.OFFLINE) {
            try {
                String msg = in.readLine();
                if (msg != null)
                    server.getReceivedList().add(new Received(this, msg));
            } catch (IOException e) {
                status = Status.OFFLINE;
            }
        }
        System.out.printf("[%s] 退出%n", username);
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.getUserList().removeIf(u -> u == this);
        LinkedList<ChartRoom> rooms = server.getChartRoomList();
        for (ChartRoom r : rooms) {
            for (User u: r.getUserList()) {
                if (u == this)
                    r.removeUser(u);
            }
        }
    }

    public void handle(String msg) throws IOException {
        if (status == Status.MENU) {
            switch (msg) {
                case "lu":
                    listUsers();
                    break;

                case "lr":
                    listRooms();
                    break;

                case "chat":
                    status = Status.READY_FOR_REPLY;
                    send("请输入要聊天的用户名，输入 $quit 退出");
                    listUsers();
                    break;

                case "cr":
                    createPublicRoom();
                    break;


                case "jr":
                    status = Status.JOIN_ROOM;
                    send("请输入房间号，输入 $quit 返回菜单");
                    listRooms();
                    break;

                case "logout":
                    send("##SERVER_CMD:LOGOUT##");
                    status = Status.OFFLINE;
                    break;

                case "help":
                    help();
                    break;

                default:
                    send("无此命令");
            }
        } else if (status == Status.READY_FOR_REPLY) {
            chat(msg);
        } else if (status == Status.READY_TO_ANSWER) {
            answer(msg);
        } else if (status == Status.CHART) {
            if (msg.equals("$bye")) {
                if (chartRoom.getOwner() != this) {
                    bye();
                } else {
                    removeChatroom();
                }
            } else if (msg.equals("$kick") && this == chartRoom.getOwner()) {
                status = Status.READY_TO_KICK;
                send("请输入用户名踢人，输入 $quit 返回聊天");
                listRoomUser();
            } else {
                broadcast(msg);
            }
        } else if (status == Status.READY_TO_KICK) {
            if (msg.equals("$quit")) {
                send("已返回聊天");
                status = Status.CHART;
            }
            kickUser(msg);
        } else if (status == Status.JOIN_ROOM) {
            if (msg.equals("$quit")) {
                returnMenu();
            } else {
                joinRoom(msg);
            }
        }
    }

    private void joinRoom(String msg) throws IOException {
        try {
            int id = Integer.parseInt(msg);
            LinkedList<ChartRoom> rooms = new LinkedList<>(server.getChartRoomList());
            rooms.removeIf(ChartRoom::isSecret);
            for (ChartRoom r : rooms) {
                if (r.getId() == id) {
                    chartRoom = r;
                    r.addUser(this);
                    r.broadcast(username, "加入了房间");
                    status = Status.CHART;
                    return;
                }
            }
            send("房间号输入错误");
        } catch (NumberFormatException e) {
            send("房间号输入错误");
        }
    }

    private void kickUser(String user) throws IOException {
        LinkedList<User> users = new LinkedList<>(chartRoom.getUserList());
        for (User u : users) {
            if (u.username.equals(user)) {
                chartRoom.broadcast(u.username, "被房主移出房间");
                chartRoom.removeUser(u);
                u.returnMenu();
                status = Status.CHART;
                return;
            }
        }
        send("房间无此用户");
    }

    private void listRoomUser() {
        for (User u : chartRoom.getUserList()) {
            send(u.username);
        }
    }

    private void removeChatroom() throws IOException {
        chartRoom.removeChatRoom();
    }

    public String getUsername() {
        return username;
    }

    private void createPublicRoom() {
        chartRoom = server.createPublicChatroom(this);
        status = Status.CHART;
        send(String.format("已创建房间，房间号是[%06d]，输入 $bye 解散房间，输入 $kick 踢人", chartRoom.getId()));
    }

    private void bye() throws IOException {
        returnMenu();
        chartRoom.removeUser(this);
        chartRoom.broadcast(username, "### 已退出房间 ###");
        help();
    }

    private void broadcast(String msg) throws IOException {
        chartRoom.broadcast(username, msg);
    }

    private void answer(String answer) {
        if (answer.equals("y") || answer.equals("Y")) {
            chatSrc.send("对方同意了您的邀请");
            send("已同意对方的邀请");
            status = Status.CHART;
            chatSrc.status = Status.CHART;
            ChartRoom room = server.createChatroom(this, chatSrc);
            chatSrc.chartRoom = room;
            chartRoom = room;
            chatSrc.send(String.format("房间号是 [%06d], 输入 $bye 退出房间", room.getId()));
            send(String.format("房间号是 [%06d], 输入 $bye 退出房间", room.getId()));
            chatSrc = null;
            return;
        } else if (answer.equals("n") || answer.equals("N")) {
            send("已拒绝对方邀请");
            chatSrc.send("对方拒绝了您的邀请");
            chatSrc = null;
            status = Status.MENU;
            return;
        }
        send("请输入y/N");
    }

    private void chat(String cmd) {
        if (cmd.equals("$quit")) {
            status = Status.MENU;
            send("已返回菜单");
        } else {
            LinkedList<User> users = new LinkedList<>(server.getUserList());
            users.removeIf(u -> u.status != Status.MENU);
            for (User u : users) {
                if (u.username.equals(cmd)) {
                    u.chatSrc = this;
                    u.send(username + "邀请您聊天 [y/N]");
                    u.status = Status.READY_TO_ANSWER;
                    send("邀请已发送，等待对方同意");
                    status = Status.SUSPEND;
                    return;
                }
            }
            send("找不到用户");
            listUsers();
        }
    }

    public void send(String msg) {
        server.getSentList().add(new Sent(this, msg));
    }

    public void listUsers() {
        LinkedList<User> users = new LinkedList<>(server.getUserList());

        if (users.size() == 1) {
            send("无其他在线用户");
        } else {
            for (User u : users) {
                if (!u.equals(this)) {
                    String sentMsg = u.username;
                    if (u.status == Status.MENU) sentMsg = "[" + sentMsg + "]  <free>";
                    server.getSentList().add(new Sent(this, sentMsg));
                }
            }
        }
    }

    public void listRooms() {
        LinkedList<ChartRoom> rooms = server.getChartRoomList();
        rooms.removeIf(ChartRoom::isSecret);
        if (rooms.size() == 0) {
            send("无群聊房间");
        } else {
            for (ChartRoom r : server.getChartRoomList()) {
                String sentMsg = String.format("[%06d] 在线人数：%d", r.getId(), r.size());
                send(sentMsg);
            }
        }
    }

    public void help() {
        send("lu: 列出所有用户");
        send("lr: 列出房间");
        send("chat: 单聊");
        send("cr: 创建房间");
        send("jr: 加入房间");
        send("logout: 退出");
        send("help: 帮助");
    }

    public boolean isOnline() {
        return status != Status.OFFLINE;
    }
}