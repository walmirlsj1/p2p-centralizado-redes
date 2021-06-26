package projkurose.peer;

import projkurose.core.FileManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class PeerSender implements Runnable{
    private Integer clientSrvPort;

    public PeerSender(Integer clientSrvPort) {
        this.clientSrvPort = clientSrvPort;
    }

    @Override
    public void run() {
        this.startThreadSrv();
    }

    private void startThreadSrv() {
        Runnable threadSrv = () -> {
            try {
                System.out.println("Iniciando Servidor Local porta: " + this.clientSrvPort);
                serverStart();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(threadSrv).start();
    }

    private void serverStart() throws IOException {
        String clientSentence;
        String capitalizedSentence;

        ServerSocket server = new ServerSocket(this.clientSrvPort);

        while (true) {
            /**
             * Da forma que está atende um cliente por vez
             * seria interessante ter um spool de thread:? e tratar cliente em thread separadas
             * ex. 16 clientes de uma vez
             *
             */
            Socket cliente = server.accept();

            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(cliente.getInputStream()));

            DataOutputStream outToClient =
                    new DataOutputStream(cliente.getOutputStream());

            clientSentence = inFromClient.readLine();

            String[] infoSrvDir = clientSentence.trim().split(";");

            System.out.println("Cliente solicita: " + infoSrvDir[0]);
            String respP2P = "Falha na requisição";
            if (infoSrvDir[0].equals("GET")) {

//                respP2P = this.shareList.get(infoSrvDir[1]);

            }
            capitalizedSentence = respP2P + '\n';

            System.out.println("Data: " + capitalizedSentence.trim() +
                    " Tamanho: " + capitalizedSentence.length());

            outToClient.writeBytes(capitalizedSentence);

            cliente.close();
        }
    }

    private void getConnectionClientServer(String hostname, int port, String path) throws IOException {
        FileManager fr = new FileManager();


        Socket clientSocket = new Socket(hostname, port);//6543
        String ipAddressSrv = clientSocket.getLocalAddress().getHostAddress();
        DataOutputStream sendSrv = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream receiveSrv = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        List<String> loadFileList = fr.loadFileList(path);
//        for (String s : loadFileList) {
//            fr.sendFileDir(new File(s), sendSrv);
//        }
        /**
         *
         * 1 - enviar a lista de arquivos para download
         * 2 - enviar tamanho total sera salvo em um zip para teste
         * 3 - enviar arquivo e indice da ultima posicao enviada
         * 4 - client vai ler o arquivo e guardar o indice, e o ultimo arquivo enviado
         * 5 - enviar um informação como fim de conexao
         * Apartir do momento que o cliente tem esse arquivo, ou parte dele
         * seria interessante o cliente compartilhar essa informação
         * mais vamos só fazer funcionar por enquanto.
         */
        /**
         * while(count<total){
         *      part = byte[16*1024] //
         *      append[part] in file
         *      count++;
         * }
         *
         */
        /* @TODO falta implementar 1!!!*/

//        this.type = receive.readChar();
//        this.length = receive.readInt();
//        this.retornoSRV = receive.readNBytes(length);

    }
}
