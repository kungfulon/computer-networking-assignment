import java.io.IOException;
import java.net.DatagramSocket;

public abstract class Server implements Runnable{
    protected DatagramSocket m_Socket;

    protected abstract void work() throws IOException;

    @Override
    public void run() {
        while (true) {
            try {
                work();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
