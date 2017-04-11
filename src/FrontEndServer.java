import java.io.IOException;
import java.net.*;

public class FrontEndServer extends Server {
    private InetSocketAddress m_MD5ServerAddress;
    private InetSocketAddress m_SHAServerAddress;

    public FrontEndServer(int port, InetAddress md5ServerIP, int md5ServerPort, InetAddress shaServerIP, int shaServerPort) throws IOException {
        m_Socket = new DatagramSocket(port);
        m_MD5ServerAddress = new InetSocketAddress(md5ServerIP, md5ServerPort);
        m_SHAServerAddress = new InetSocketAddress(shaServerIP, shaServerPort);
    }

    public void work() throws IOException {
        byte buffer[] = new byte[100];
        DatagramPacket p = new DatagramPacket(buffer, 7, 93);
        m_Socket.receive(p);

        byte rawClientIP[] = p.getAddress().getAddress();
        System.arraycopy(rawClientIP, 0, buffer, 0, 4);
        buffer[4] = (byte)(p.getPort() >> 8);
        buffer[5] = (byte)(p.getPort() & 0xFF);
        buffer[6] = (byte)p.getLength();

        p = new DatagramPacket(buffer, 0, 7 + p.getLength());

        p.setSocketAddress(m_MD5ServerAddress);
        m_Socket.send(p);
        p.setSocketAddress(m_SHAServerAddress);
        m_Socket.send(p);
    }
}
