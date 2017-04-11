import java.io.IOException;
import java.net.*;
import java.security.*;
import java.util.Arrays;

public class DigestServer extends Server {
    private MessageDigest m_MessageDigest;
    private byte m_bAlgorithmSignature;

    public DigestServer(int port, String algorithm) throws IOException, NoSuchAlgorithmException {
        m_Socket = new DatagramSocket(port);
        m_MessageDigest = MessageDigest.getInstance(algorithm);
        m_bAlgorithmSignature = (byte)algorithm.charAt(0);
    }

    public void work() throws IOException {
        byte buffer[] = new byte[100];
        DatagramPacket p = new DatagramPacket(buffer,100);
        m_Socket.receive(p);

        byte rawClientIP[] = new byte[4];
        System.arraycopy(buffer, 0, rawClientIP, 0, 4);
        int clientPort = 0, dataLength = buffer[6];

        if (buffer[4] < 0)
            clientPort += (256 + buffer[4]) << 8;
        else
            clientPort += buffer[4] << 8;

        if (buffer[5] < 0)
            clientPort += 256 + buffer[5];
        else
            clientPort += buffer[5];

        if (dataLength < 0)
            dataLength = 256 + dataLength;

        InetSocketAddress clientAddress = new InetSocketAddress(InetAddress.getByAddress(rawClientIP), clientPort);

        m_MessageDigest.reset();
        m_MessageDigest.update(buffer, 7, dataLength);
        byte digest[] = m_MessageDigest.digest();

        buffer = new byte[digest.length + 2];
        buffer[0] = m_bAlgorithmSignature;
        buffer[1] = (byte)digest.length;
        System.arraycopy(digest, 0, buffer, 2, digest.length);

        p = new DatagramPacket(buffer, 0, buffer.length, clientAddress);
        m_Socket.send(p);
    }
}
