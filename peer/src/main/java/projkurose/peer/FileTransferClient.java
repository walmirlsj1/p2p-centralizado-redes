package projkurose.peer;

import projkurose.core.FileManager;
import projkurose.peer.model.Shared;

import java.io.*;
import java.net.Socket;

public class FileTransferClient {
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

    public boolean run() {
        try {

            if (tryCheckConnection()) return startReceive();
//            else System.out.println("Falha ao realizar conexão com Servidores Peer");

        } catch (IOException e) {
            e.getStackTrace();
        }
        return false;
    }

    public boolean startReceive() throws IOException {


        DataInputStream receive;

        DataOutputStream send;
        send = new DataOutputStream(clientSocket.getOutputStream());
        receive = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        send.writeChar('a');         // operacao
        send.writeInt(share.hashCode());        // hashCode

        int listLength = receive.readInt();

        if(listLength == 0) return false;

        long shareLength = receive.readLong();

        long shareReceivedLength = 0L;

        while (listLength-- > 0) {
            shareReceivedLength += FileManager.receiveFileDir(receive, send, this.share.getPath());
        }

        clientSocket.close();

        return shareLength == shareReceivedLength;

    }

}
