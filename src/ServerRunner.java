import java.net.InetAddress;

public class ServerRunner {
    public static void main(String args[]) {
        try {
            int frontEndServerPort = Integer.parseInt(args[0]);
            int md5ServerPort = Integer.parseInt(args[1]);
            int sha256ServerPort = Integer.parseInt(args[2]);

            InetAddress localhost = InetAddress.getLocalHost();

            Thread t = new Thread(new DigestServer(md5ServerPort, "MD5"));
            t.start();

            t = new Thread(new DigestServer(sha256ServerPort, "SHA-256"));
            t.start();

            FrontEndServer s = new FrontEndServer(frontEndServerPort, localhost, md5ServerPort, localhost, sha256ServerPort);
            s.run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
