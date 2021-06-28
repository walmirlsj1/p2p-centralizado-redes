package projkurose.server;

import projkurose.core.Server;

import java.net.*;

public class ServerDirectory extends Server {

    public ServerDirectory(int port, int numThreads) {
        super(port, numThreads);
    }

    @Override
    protected Runnable getRunnable(Socket client){
        return new ClientHandle(client);
    }

}
