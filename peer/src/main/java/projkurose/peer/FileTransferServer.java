package projkurose.peer;

import lombok.SneakyThrows;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.mail.EmailException;
import projkurose.core.CommonsMail;
import projkurose.core.Config;
import projkurose.core.FileManager;
import projkurose.peer.model.Shared;
import projkurose.peer.model.SharedDAO;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class FileTransferServer implements Runnable {
    private final Socket connectionSocket;
    private String clientIP;

    public FileTransferServer(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    private String getDateTimeToString() {
        SimpleDateFormat formataData = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return formataData.format(new Date());
    }

    private void sendMail(Shared share) {
        String destinatario = "", destinatario_nome="";

        try {
            PropertiesConfiguration config = Config.getConfiguracao();
            destinatario = config.getString("destinatario_email");
            destinatario_nome = config.getString("destinatario_nome");
        } catch (Exception e) {
            throw new RuntimeException("Falha ao ler email do destinatario!");
        }

        CommonsMail mail = new CommonsMail();

        String datetime = this.getDateTimeToString();

        String title = String.format("<%s, %s> - ", this.clientIP,
                share.getTitle(), datetime);

        String message = String.format("<%s, %s> %s iniciou compartilhamento de arquivos.", this.clientIP,
                share.getTitle(), datetime);

        try {
            mail.sendSimpleMail(destinatario_nome, destinatario, title, message);
        } catch (EmailException e) {
            e.printStackTrace();
//            throw new RuntimeException("Falha ao enviar email!");
        }
    }

    private void processRequest() throws IOException {
        DataInputStream recebe;
        DataOutputStream envia;
        try {
            recebe = new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));

            envia = new DataOutputStream(connectionSocket.getOutputStream());

            this.clientIP = connectionSocket.getInetAddress().getHostAddress();

        } catch (IOException e) {
            throw new RuntimeException("processRequest: Falha ao instanciar metodos de comunicação - " + clientIP);
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

        this.sendMail(shared);


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
