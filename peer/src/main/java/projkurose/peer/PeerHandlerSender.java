package projkurose.peer;

import java.net.Socket;

public class PeerHandlerSender implements Runnable {
    private final Socket connectionSocket;
    private String clientIP;

    public PeerHandlerSender(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {

    }
}
