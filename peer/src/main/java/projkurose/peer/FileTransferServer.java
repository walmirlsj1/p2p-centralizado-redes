package projkurose.peer;

import lombok.SneakyThrows;
import projkurose.core.FileManager;
import projkurose.peer.model.Shared;
import projkurose.peer.model.SharedDAO;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;

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
        char operation = recebe.readChar(); // seria util para continuar um download..

        int hashCode = recebe.readInt();

        SharedDAO dao = new SharedDAO();

        Shared shared = dao.findByHashCode(hashCode);

        if (shared == null) {
            envia.writeInt(0);
            return;
        }

        String path_dir = shared.getPath();

        ArrayList<String> fileList = (ArrayList<String>) FileManager.loadFileList(path_dir);


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
