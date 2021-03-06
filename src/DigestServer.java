import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestServer extends Server {
    private MessageDigest m_MessageDigest;
    private byte m_bAlgorithmSignature;

    DigestServer(int port, String algorithm) throws IOException, NoSuchAlgorithmException {
        super(port);
        m_MessageDigest = MessageDigest.getInstance(algorithm);
        m_bAlgorithmSignature = (byte)algorithm.charAt(0);
    }

    public void work() throws IOException {
        byte[] srcAddress = new byte[4];
        receive(srcAddress);

        byte rawClientIP[] = new byte[4];
        System.arraycopy(m_recvData, PutinPacket.OFFSET_DATA, rawClientIP, 0, 4);
        int clientPort = ByteUtil.getUShort(m_recvData, PutinPacket.OFFSET_DATA + 4);
        int dataLength = m_recvPacket.getDataLength() - 6;

        m_MessageDigest.reset();
        m_MessageDigest.update(m_recvData, PutinPacket.OFFSET_DATA + 6, dataLength);
        byte digest[] = m_MessageDigest.digest();

        byte[] data = new byte[digest.length + 1];
        data[0] = m_bAlgorithmSignature;
        System.arraycopy(digest, 0, data, 1, digest.length);

        m_sendPacket.setPutinData(data);
        m_sendPacket.setDestinationPort(clientPort);
        send(InetAddress.getByAddress(rawClientIP), false);
    }
}
