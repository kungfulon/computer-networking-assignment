import com.savarese.rocksaw.net.RawSocket;

import static com.savarese.rocksaw.net.RawSocket.PF_INET;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Client {
    private static final int PORT = 34000;
    private static int ID = 0;
    private static HashSet<Integer> waitingForACK = new HashSet<>();
    private static ArrayList<Timer> timers = new ArrayList<>();
    private static RawSocket socket;
    private static byte sendData[];
    private static RussianPacket sendPacket;
    private static byte recvData[];
    private static RussianPacket recvPacket;

    private static void receive(byte[] srcAddress) throws IOException {
        do {
            int iLength = socket.read(recvData, srcAddress);

            if (iLength < 20 + RussianPacket.OFFSET_DATA)
                continue;

            System.arraycopy(recvData, 20, recvData, 0, iLength - 20);

            if (!recvPacket.verifyChecksum() || recvPacket.getDestinationPort() != PORT)
                continue;

            if (!recvPacket.isACK()) {
                sendPacket.setID(recvPacket.getID());
                sendPacket.setData(new byte[]{0}, 0, 0);
                sendPacket.setDestinationPort(sendPacket.getSourcePort());
                sendPacket.computeChecksum();
                send(InetAddress.getByAddress(srcAddress), true);
                break;
            }

            waitingForACK.remove(recvPacket.getID());
        } while (true);
    }

    private static void send(InetAddress destination, boolean isACK) throws IOException {
        sendPacket.setIsACK(isACK);

        if (!isACK) {
            sendPacket.setID(ID++);
            sendPacket.computeChecksum();
            waitingForACK.add(sendPacket.getID());

            int iLength = RussianPacket.OFFSET_DATA + sendPacket.getDataLength();
            byte[] _sendData = new byte[iLength];
            System.arraycopy(sendData, 0, _sendData, 0, iLength);
            int id = sendPacket.getID();

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!waitingForACK.contains(id)) {
                        timer.cancel();
                        return;
                    }

                    try {
                        socket.write(destination, _sendData, 0, iLength);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 10000);
            timers.add(timer);
        } else {
            sendPacket.computeChecksum();
            socket.write(destination, sendData, 0, RussianPacket.OFFSET_DATA + sendPacket.getDataLength());
        }
    }

    public static void main(String args[]) throws IOException {
        socket = new RawSocket();
        socket.open(PF_INET, RussianPacket.PROTOCOL_NUMBER);
        socket.write(InetAddress.getLocalHost(), new byte[]{(byte)0}); // workaround on windows
        sendData = new byte[120 + RussianPacket.OFFSET_DATA];
        sendPacket = new RussianPacket(120);
        sendPacket.setData(sendData);
        sendPacket.setSourcePort(PORT);
        sendPacket.setDestinationPort(Integer.parseInt(args[1]));
        recvData = new byte[120 + RussianPacket.OFFSET_DATA];
        recvPacket = new RussianPacket(120);
        recvPacket.setData(recvData);

        sendPacket.setData(args[2].getBytes(), 0, args[2].getBytes().length);
        send(InetAddress.getByName(args[0]), false);

        System.out.println("Response from server:");
        System.out.println("Hash value of \"" + args[2] + "\"");

        for (int i = 0; i < 2; ++i) {
            byte[] srcAddress = new byte[4];
            receive(srcAddress);

            if (recvData[RussianPacket.OFFSET_DATA] == 'S')
                System.out.print("SHA-256: ");
            else
                System.out.print("MD5: ");

            StringBuilder digest = new StringBuilder();

            for (int j = 1; j < recvPacket.getDataLength(); ++j)
                digest.append(String.format("%02x", recvData[RussianPacket.OFFSET_DATA + j]));

            System.out.println(digest);
        }

        for (Timer timer:timers) {
            timer.cancel();
        }
    }
}
