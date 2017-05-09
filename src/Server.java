import com.savarese.rocksaw.net.RawSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import static com.savarese.rocksaw.net.RawSocket.PF_INET;

public abstract class Server implements Runnable{
    private static int ID = 0;
    private static HashSet<Integer> waitingForACK = new HashSet<>();

    private RawSocket m_Socket;
    private int m_iPort;
    byte m_sendData[];
    PutinPacket m_sendPacket;
    byte m_recvData[];
    PutinPacket m_recvPacket;

    protected abstract void work() throws IOException;

    Server(int iPort) throws IOException {
        m_Socket = new RawSocket();
        m_Socket.open(PF_INET, PutinPacket.PROTOCOL_NUMBER);
        m_Socket.write(InetAddress.getLocalHost(), new byte[]{(byte)0}); // workaround on windows
        m_iPort = iPort;
        m_sendData = new byte[120 + PutinPacket.OFFSET_DATA];
        m_sendPacket = new PutinPacket(120);
        m_sendPacket.setData(m_sendData);
        m_sendPacket.setSourcePort(iPort);
        m_recvData = new byte[120 + PutinPacket.OFFSET_DATA];
        m_recvPacket = new PutinPacket(120);
        m_recvPacket.setData(m_recvData);
    }

    void receive(byte[] srcAddress) throws IOException {
        do {
            int iLength = m_Socket.read(m_recvData, srcAddress);
            int iIPHeaderLength = (m_recvData[0] & 0xF) * 4;

            if (iLength < iIPHeaderLength + PutinPacket.OFFSET_DATA)
                continue;

            System.arraycopy(m_recvData, iIPHeaderLength, m_recvData, 0, iLength - iIPHeaderLength);

            if (!m_recvPacket.verifyChecksum() || m_recvPacket.getDestinationPort() != m_iPort)
                continue;

            if (!m_recvPacket.isACK()) {
                m_sendPacket.setID(m_recvPacket.getID());
                m_sendPacket.setPutinData(new byte[]{});
                m_sendPacket.setDestinationPort(m_recvPacket.getSourcePort());
                send(InetAddress.getByAddress(srcAddress), true);
                break;
            }

            waitingForACK.remove(m_recvPacket.getID());
        } while (true);
    }

    void send(InetAddress destination, boolean isACK) throws IOException {
        m_sendPacket.setIsACK(isACK);

        if (!isACK) {
            m_sendPacket.setID(ID++);
            m_sendPacket.computeChecksum();
            waitingForACK.add(m_sendPacket.getID());

            int iLength = PutinPacket.OFFSET_DATA + m_sendPacket.getDataLength();
            byte[] sendData = new byte[iLength];
            System.arraycopy(m_sendData, 0, sendData, 0, iLength);
            int id = m_sendPacket.getID();

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!waitingForACK.contains(id)) {
                        timer.cancel();
                        return;
                    }

                    try {
                        m_Socket.write(destination, sendData, 0, iLength);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 10000);
        } else {
            m_sendPacket.computeChecksum();
            m_Socket.write(destination, m_sendData, 0, PutinPacket.OFFSET_DATA + m_sendPacket.getDataLength());
        }
    }

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
