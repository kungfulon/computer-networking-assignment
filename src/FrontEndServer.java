import com.savarese.rocksaw.net.RawSocket;

import static com.savarese.rocksaw.net.RawSocket.PF_INET;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class FrontEndServer extends Server {
    private InetAddress m_MD5ServerAddress;
    private int m_iMD5ServerPort;
    private InetAddress m_SHAServerAddress;
    private int m_iSHAServerPort;

    public FrontEndServer(int port, InetAddress md5ServerIP, int md5ServerPort, InetAddress shaServerIP, int shaServerPort) throws IOException {
        super(port);
        m_MD5ServerAddress = md5ServerIP;
        m_iMD5ServerPort = md5ServerPort;
        m_SHAServerAddress = shaServerIP;
        m_iSHAServerPort = shaServerPort;
    }

    public void work() throws IOException {
        byte[] srcAddress = new byte[4];
        receive(srcAddress);
        System.arraycopy(srcAddress, 0, m_sendData, RussianPacket.OFFSET_DATA, 4);
        m_sendData[RussianPacket.OFFSET_DATA + 4] = (byte)(m_recvPacket.getSourcePort() >> 8);
        m_sendData[RussianPacket.OFFSET_DATA + 5] = (byte)(m_recvPacket.getSourcePort() & 0xFF);
        System.arraycopy(m_recvData, RussianPacket.OFFSET_DATA, m_sendData, RussianPacket.OFFSET_DATA + 6, m_recvPacket.getDataLength());
        m_sendPacket.setDataLength(m_recvPacket.getDataLength() + 6);
        m_sendPacket.setDestinationPort(m_iMD5ServerPort);
        send(m_MD5ServerAddress, false);
        m_sendPacket.setDestinationPort(m_iSHAServerPort);
        send(m_SHAServerAddress, false);
    }
}
