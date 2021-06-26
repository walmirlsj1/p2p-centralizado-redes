package projkurose.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Server {

    private static final int MAX_THREAD = 50;
    private int port, numThreads = 10;
    protected ExecutorService threadPool;

    public Server(int port, int numThreads) {
        this.port = port;

        if (numThreads <= MAX_THREAD) this.numThreads = numThreads;

        threadPool = Executors.newFixedThreadPool(this.numThreads);
    }

    public void start() {
        ServerSocket socketServidor = null;
        try {
            socketServidor = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException(String.format("start: Falha ao criar socket na porta: %d ", this.port));
        }
        System.out.println(" ::::::: PORT: " + this.port + " ::::::: ");

        while (!socketServidor.isClosed()) {
            Socket client = null;
            System.out.println("Aguardando clientes");
            try {
                client = socketServidor.accept();
            } catch (IOException e) {
                throw new RuntimeException("start: Falha ao aceitar conexão do cliente");
            }
            this.threadPool.execute(this.getRunnable(client));
        }
    }

    protected abstract Runnable getRunnable(Socket client);

}
