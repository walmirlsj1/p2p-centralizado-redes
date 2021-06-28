package projkurose.peer;

import projkurose.core.FileManager;

import java.io.*;
import java.net.Socket;

public class FileTransferClient implements Runnable {
    private Socket clientSocket;
    private int serverPortDir;
    private String serverIpDir;
    private String path_dir;
    private Long id;

    public FileTransferClient(Long id, String clients, String path_dir) {
        this.id = id;
        String[] fullAddress = clients.split(";");
        String[] addressIp = fullAddress[0].split(":");

        this.serverIpDir = addressIp[0];
        this.serverPortDir = Integer.valueOf(addressIp[1]);
        this.path_dir = path_dir;

    }

    @Override
    public void run() {
        try {
            teste();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    public void teste() throws IOException {

        try {
            clientSocket = new Socket(this.serverIpDir, this.serverPortDir);//6543
        } catch (IOException e) {
            System.out.println("Server offline");
            System.exit(0);
        }
        DataInputStream receive;

        DataOutputStream send;
        send = new DataOutputStream(clientSocket.getOutputStream());
        receive = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        String data = "Teste envio";

        send.writeChar('a');         // operacao
        send.writeInt(data.length());   // length
        send.writeBytes(data);          // data

        int listLength = receive.readInt();

        while (listLength-- > 0) {
            FileManager.receiveFileDir(receive, send, this.path_dir);
        }

        clientSocket.close();
    }

}
