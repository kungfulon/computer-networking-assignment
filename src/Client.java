import java.net.*;
import java.util.Arrays;

public class Client {
    public static void main(String args[]) {
        try {
            SocketAddress serverAddress = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket p = new DatagramPacket(args[2].getBytes(), 0, args[2].length(), serverAddress);
            socket.send(p);

            System.out.println("Response from server:");
            System.out.println("Hash value of \"" + args[2] + "\"");

            for (int i = 0; i < 2; ++i) {
                byte buffer[] = new byte[100];
                p = new DatagramPacket(buffer, 0, 100);
                socket.receive(p);

                if (buffer[0] == 'S')
                    System.out.print("SHA-256: ");
                else
                    System.out.print("MD5: ");

                StringBuilder digest = new StringBuilder();

                for (int j = 2; j < buffer[1]; ++j)
                    digest.append(String.format("%02x", buffer[j]));

                System.out.println(digest);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
