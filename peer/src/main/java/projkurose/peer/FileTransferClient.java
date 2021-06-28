package projkurose.peer;

import projkurose.core.FileManager;
import projkurose.peer.model.Shared;

import java.io.*;
import java.net.Socket;

public class FileTransferClient implements Runnable {
    private Socket clientSocket;
    private Shared share;
    private String response;

    public FileTransferClient(Shared share, String response) {
        this.share = share;
        this.response = response;
    }

    private boolean tryCheckConnection() {
        String[] fullAddress = response.split(";");

        for (String server : fullAddress) {
            String[] addressIpPort = server.split(":");

            String ip = addressIpPort[0];
            int port = Integer.parseInt(addressIpPort[1]);

            try {
                clientSocket = new Socket(ip, port);//6543
            } catch (IOException e) {
                return false; // falha de comunicação
            }
            return clientSocket.isConnected();
        }
        return false;
    }

    @Override
    public void run() {
        try {

            if (tryCheckConnection()) startReceive();
            else System.out.println("Falha ao realizar conexão com Servidores Peer");

        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    public void startReceive() throws IOException {


        DataInputStream receive;

        DataOutputStream send;
        send = new DataOutputStream(clientSocket.getOutputStream());
        receive = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        send.writeChar('a');         // operacao
        send.writeInt(share.hashCode());        // hashCode

        int listLength = receive.readInt();

        while (listLength-- > 0) {
            FileManager.receiveFileDir(receive, send, this.share.getPath());
        }

        clientSocket.close();
    }

}
