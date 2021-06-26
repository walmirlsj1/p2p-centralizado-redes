package projkurose.peer;

import projkurose.core.Server;

import java.net.Socket;

public class PeerServer extends Server implements Runnable {

    public PeerServer(int port, int numThreads) {
        super(port, numThreads);
    }

    @Override
    protected Runnable getRunnable(Socket client) {
        return new PeerHandlerSender(client);
    }

    @Override
    public void run() {
        this.start();
    }
}
