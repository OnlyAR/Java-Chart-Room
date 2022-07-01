import java.io.BufferedWriter;
import java.io.IOException;

public class Sent {
    public User dst;
    public String msg;

    public Sent (User dst, String msg) {
        this.dst = dst;
        this.msg = msg;
    }

    public void send() throws IOException {
        BufferedWriter out = dst.getOut();
        out.write(msg +"\n");
        out.flush();
    }
}
