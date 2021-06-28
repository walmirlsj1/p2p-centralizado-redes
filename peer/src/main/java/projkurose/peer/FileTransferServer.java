package projkurose.peer;

import lombok.SneakyThrows;
import projkurose.core.FileManager;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class FileTransferServer implements Runnable {
    private final Socket connectionSocket;
    private String clientIP;

    public FileTransferServer(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }


    private void processRequest() throws IOException {
        DataInputStream recebe;
        DataOutputStream envia;
        try {
            recebe = new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));

            envia = new DataOutputStream(connectionSocket.getOutputStream());

            this.clientIP = connectionSocket.getInetAddress().getHostAddress();

        } catch (IOException e) {
            throw new RuntimeException("tratarRequisição: Falha no tratamento da requisição - " + clientIP);
        }
        /**
         * Recebe dados do cliente
         */
        char operation = recebe.readChar();

        int length = recebe.readInt();

        String data = new String(recebe.readNBytes(length));

        String path_dir = "/home/ghost/Shared/Meu/";

        ArrayList<String> fileList = (ArrayList<String>) FileManager.loadFileList("/home/ghost/Shared/Meu/");


        envia.writeInt(fileList.size());

        for (String filename : fileList) {
            FileManager.sendFileDir(recebe, envia, path_dir, filename);
        }
    }

    @SneakyThrows
    @Override
    public void run() {
        processRequest();
    }


}
