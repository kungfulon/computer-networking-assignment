import java.io.IOException;
import java.net.*;
import java.security.*;
import java.util.Arrays;

public class DigestServer extends Server {
    private MessageDigest m_MessageDigest;
    private byte m_bAlgorithmSignature;

    public DigestServer(int port, String algorithm) throws IOException, NoSuchAlgorithmException {
        super(port);
        m_MessageDigest = MessageDigest.getInstance(algorithm);
        m_bAlgorithmSignature = (byte)algorithm.charAt(0);
    }

    public void work() throws IOException {
        byte[] srcAddress = new byte[4];
        receive(srcAddress);

        byte rawClientIP[] = new byte[4];
        System.arraycopy(m_recvData, RussianPacket.OFFSET_DATA, rawClientIP, 0, 4);
        int clientPort =((m_recvData[RussianPacket.OFFSET_DATA + 4] << 8 & 0xFFFF) | m_recvData[RussianPacket.OFFSET_DATA + 5] & 0xFF);
        int dataLength = m_recvPacket.getDataLength() - 6;

        m_MessageDigest.reset();
        m_MessageDigest.update(m_recvData, RussianPacket.OFFSET_DATA + 6, dataLength);
        byte digest[] = m_MessageDigest.digest();

        byte[] data = new byte[digest.length + 1];
        data[0] = m_bAlgorithmSignature;
        System.arraycopy(digest, 0, data, 1, digest.length);

        m_sendPacket.setData(data, 0, data.length);
        m_sendPacket.setDestinationPort(clientPort);
        send(InetAddress.getByAddress(rawClientIP), false);
    }
}
