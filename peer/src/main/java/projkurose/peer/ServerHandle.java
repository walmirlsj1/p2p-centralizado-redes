package projkurose.peer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import projkurose.peer.model.Shared;

public class ServerHandle {
    private final Integer clientSrvPort;
    private final Integer serverPortDir;

    private final String serverIpDir;
    private Long clientId;

    private Socket clientSocket;
    private DataOutputStream send;
    private DataInputStream receive;

    private boolean started;

    private ExecutorService threadPool =
            Executors.newFixedThreadPool(20);

    char type;
    int length = 0;
    byte[] received;

    public ServerHandle(Long clientId, int clientSrvPort, String serverIpDir, int serverPortDir) throws IOException {
        this.clientSrvPort = clientSrvPort; //6789
        this.clientId = clientId;
        this.serverIpDir = serverIpDir;
        this.serverPortDir = serverPortDir;

        applyServer();

        this.started = true;
    }

    private void receiveFromServer() throws IOException {
        this.type = receive.readChar();
        this.length = receive.readInt();
        this.received = receive.readNBytes(length);
    }

    private void sendServer(char op, String data) throws IOException {
        send.writeChar(op);                 // operacao
        send.writeInt(data.length());   // length
        send.writeBytes(data);          // data
        send.flush();
    }

    public void registerShareServer(Shared share) throws IOException {
        if (share == null) return;

        /**
         * clientId$title;size()|title;size()|...title;size()
         */
        serverGo('r', String.format("%d$%s;%d", clientId, share.getTitle(), share.getSize()));
    }

    public void registerShareServer(List<Shared> shares) throws IOException {
        if (shares.isEmpty()) return;

        /**
         * clientId$title;size()|title;size()|...title;size()
         */

        String shareText = "";
        for (Shared share : shares) {
            shareText += String.format("%s;%d|", share.getTitle(), share.getSize());
        }

        shareText = shareText.substring(0, shareText.length() - 1);

        serverGo('r', String.format("%d$%s", clientId, shareText));
    }

    public void disconnectFromServerDirectory() throws IOException {
        if (!this.started) return;

        serverGo('d', String.valueOf(this.clientId));

        this.started = false;

        String resposta = new String(received);
        System.out.println("Desconectado do servidor: " + resposta);
    }

    private void serverGo(char op, String message) throws IOException {
        try {
            clientSocket = new Socket(this.serverIpDir, this.serverPortDir);//6543
        } catch (IOException e) {
            System.out.println("Server offline");
            System.exit(0);
        }
        this.send = new DataOutputStream(clientSocket.getOutputStream());
        this.receive = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        this.sendServer(op, message);
        this.receiveFromServer();

        clientSocket.close();
    }

    private void applyServer() throws IOException {
        serverGo('a', String.valueOf(this.clientSrvPort));

        String received = new String(this.received);
        try {
            this.clientId = Long.valueOf(received);
        } catch (Exception e) {
            throw new RuntimeException("Não foi possivel registrar cliente no servidor");
        }
        System.out.println("Cliente registrado no servidor");
    }

    private void taskStartedNotification(String texto){
        System.out.println("A tarefa esta comecando! " + texto);
    }
    private void taskFinishedNotification(String texto){
        System.out.println("A tarefa terminou! " + texto);
    }
    private void getFromServerPeer(Long id, String clients, String path_dir) {
        this.threadPool.execute(new Runnable() {
            public void run() {
                taskStartedNotification(path_dir);
                new FileTransferClient(id, clients, path_dir).run();
                taskFinishedNotification(path_dir);
            }
        });
//        this.threadPool.execute(new FileTransferClient(id, clients, path_dir));

    }

    public boolean getFromServer(Long id, String path_dir) throws IOException {
        serverGo('s', String.valueOf(id));

        if (this.type == 'n') return false;

        getFromServerPeer(id, new String(received), path_dir);
        return true;
    }

    public String[] findTitleServer(String title) throws IOException {
        serverGo('l', title);

        if (this.type == 'n') return null;

        return new String(received).split(";");
    }
}
