package app.client;

import app.base.FileManager;
import app.client.model.SharedDAO;
import app.config.Config;
import app.client.model.Shared;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;

/**
 * Ideias
 * enviar arquivo
 * para continuar download usar indice de ultimo pacote recebido
 * quando iniciar download enviar indice 0
 * para continuar download enviar indece do ultimo pacote recebido
 * salvar progresso do donwload, para que não corromper o arquivo.!!
 */

public class TCPClienteServidor {
    private Runnable threadSrv;
    private HashMap<Shared, String> shareList;
    private Integer portClientSrv, portServDir;
    private String databaseName;
    private String localPort;
    private String ipAddress;
    private Long myID;

    private boolean started = false;

    char type;
    int indice = 0;
    int length = 0;
    byte[] retornoSRV;

    public TCPClienteServidor(int portClientSrv) throws Exception {
        this.carregaConfig();
        this.shareList = new HashMap<>();

        this.portServDir = 6543;
        this.portClientSrv = portClientSrv; //6789

        if (!this.processar("APPLY")) return;

        this.started = true;
        this.startThreadSrv();
        this.startClient();

    }

    private void carregaConfig() {
        try {
            databaseName = Config.getConfiguracao().getString("database-srv-dir");
            localPort = Config.getConfiguracao().getString("local-port");
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    private void startThreadSrv() {
        threadSrv = () -> {
            try {
                System.out.println("Iniciando Servidor Local porta: " + portClientSrv);
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

        ServerSocket server = new ServerSocket(this.portClientSrv);

        while (true) {
            /**
             * Da forma que está atende um cliente por vez
             * seria interessante ter um spool de thread:? e tratar cliente em thread separadas
             * ex. 16 clientes de uma vez
             *
             */
//            System.out.println("Aguardando clientes");

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

                respP2P = this.shareList.get(infoSrvDir[1]);

            }
            capitalizedSentence = respP2P + '\n';

            System.out.println("Data: " + capitalizedSentence.trim() +
                    " Tamanho: " + capitalizedSentence.length());

            outToClient.writeBytes(capitalizedSentence);

            cliente.close();
        }
    }

    private void startClient() throws Exception {
        System.out.println("Iniciando Cliente");
        boolean flagContinue = true;
        while (flagContinue) {
            flagContinue = clientConsole();
        }
    }

    private boolean clientConsole() throws Exception {

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        System.out.println("===== Lista das operações possiveis ========");
        System.out.println("===  (R)EG: Registra Arquivo/Pasta       ===");
        System.out.println("===  (G)ET: Solicita Download do arquivo ===");
        System.out.println("=== (F)IND: Procura arquivo no server    ===");
        System.out.println("=== (E)XIT: Para sair                    ===");
        System.out.println("============================================");
        System.out.println("Informe OP: GET/FIND/REG");
        String op = inFromUser.readLine();


        return this.processar(op.toUpperCase(Locale.ROOT));
    }

    private boolean processar(String op) throws Exception {
        /**
         *          * (s)eek         : share from clients
         *          * (n)ew          : seek client-serv offline
         *          * (r)egister     : share list need client_id*
         *          * (a)ply         : generate client_id* para client-serv
         *          * (d)isconnect   : from server need client_id*
         *          * (e)rror        : request
         *          * (o)K           : OK
         *          * (l)ist         : list all share or contains key*
         */
        boolean flagContinue = true;

        Socket clientSocket = null;

        try {
            clientSocket = new Socket("localhost", this.portServDir);//6543

            this.ipAddress = clientSocket.getLocalAddress().getHostAddress();

        } catch (ConnectException e) {
            System.out.println("Servidor offline: " + e.getMessage());
            return false;
        }

        DataOutputStream envia = new DataOutputStream(clientSocket.getOutputStream());

        DataInputStream receive = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));


        if (op.equals("APPLY") || op.equals("A")) {
            applyServer(envia, receive);

        } else if (op.equals("REG") || op.equals("R")) {
            Shared share = registerShareConsole();
            if (share != null) registerShareServer(envia, receive, share);

        } else if (op.equals("GET") || op.equals("G")) {  // GET: Solicita Download do arquivo
            getListPeerForDownload(envia, receive);

        } else if (op.equals("FIND") || op.equals("F")) { // FIND: Procura arquivo no server
            findServer(envia, receive);

        } else if (op.equals("EXIT") || op.equals("E")) {
            disconnectFromServerDirectory(envia, receive);

            flagContinue = this.started = false;
        }

        clientSocket.close();
        return flagContinue;
    }

    private void applyServer(DataOutputStream envia, DataInputStream receive) throws IOException, Exception {
        sendServer(envia, 'a', 0, this.ipAddress + ":" + this.portClientSrv);

        receiveFromServer(receive);

        String received = new String(retornoSRV);
        System.out.println("received: " + received);
        try {
            this.myID = Long.valueOf(received);
        } catch (Exception e) {
            throw new Exception("Não foi possivel registrar cliente no servidor");
        }
        System.out.println("Meu ID: " + this.myID);
    }

    private void registerShareServer(DataOutputStream envia, DataInputStream receive, Shared share) throws IOException {
//        myID.toString() + "$" + share.getTitle() + ";" + share.getSize()
        String messagem = String.format("%d$%s;%d", myID, share.getTitle(), share.getSize());

        sendServer(envia, 'r', 0, messagem);

//        myID

        receiveFromServer(receive);

        String resposta = new String(retornoSRV);
        System.out.println("resposta do servidor: " + resposta);
    }

    private void disconnectFromServerDirectory(DataOutputStream send, DataInputStream receive) throws IOException {
        sendServer(send, 'd', 0, String.valueOf(this.myID));
        receiveFromServer(receive);


        String resposta = new String(retornoSRV);
        System.out.println("resposta do servidor: " + resposta);
    }

    private void getListPeerForDownload(DataOutputStream send, DataInputStream receive) throws IOException {
        BufferedReader scanner =
                new BufferedReader(new InputStreamReader(System.in));

        Long id = -1L;
        while (id == -1l) {
            try {
                System.out.println("Informe o ID para requisitar download: ");
                id = Long.valueOf(scanner.readLine());
            } catch (IOException e) {
                System.out.println("Error getListPeerForDownload: " + e.getMessage());
                id = -1L;
            }
        }
        sendServer(send, 's', 0, String.valueOf(id));

        receiveFromServer(receive);

        String resposta = new String(retornoSRV);
        System.out.println("resposta do servidor: " + resposta);

    }

    private void findServer(DataOutputStream send, DataInputStream receive) throws IOException {
        BufferedReader scanner =
                new BufferedReader(new InputStreamReader(System.in));
        String title = "";
        while (title.equals("")) {
            try {
                System.out.println("Informe um titulo para pesquisa");
                title = scanner.readLine();
            } catch (IOException e) {
                System.out.println("Error getListPeerForDownload: " + e.getMessage());
                title = "";
            }
        }

        sendServer(send, 'l', 0, title);

        receiveFromServer(receive);

        String[] list = new String(retornoSRV).split(";");

        if (list.length == 0) {
            System.out.println("Titulo: " + title + " - Não foi encontrado no servidor");
            return;
        }

        for (String item : list) {
            System.out.println("Item nome: " + item);
        }
    }

    private void receiveFromServer(DataInputStream receive) throws IOException {
        this.type = receive.readChar();
        this.indice = receive.readInt();
        this.length = receive.readInt();
        this.retornoSRV = receive.readNBytes(length);
    }

    private void sendServer(DataOutputStream send, char op, int indice, String message) throws IOException {
        send.writeChar(op);                 // operacao
        send.writeInt(indice);                    // indice
        send.writeInt(message.length());   // length
        send.writeBytes(message);          // data
    }

    private void processarDownload() {

        /**
         * conecta no servidor peer servidor e requisita download do arquivo xyz
         */
    }

//    String respSrvDir = this.conectar(opProcessada, "localhost", 6543);
//
////            System.out.println("Resposta ServidorDir" + resposta);
//
//    String[] infoSrvDir = respSrvDir.trim().split(";");
//
//        System.out.println("Resposta ServidorDir OP: " + opProcessada + " resposta: " + respSrvDir);
//
//        if (infoSrvDir[0].equals("GET") && !infoSrvDir[2].equals("null")) {
//        String[] hostP2P = infoSrvDir[2].split(":");
//
//        String opP2P = "GET;" + title + ";";
//        try {
//            String respP2P = this.conectar(opP2P, hostP2P[0], Integer.parseInt(hostP2P[1]));
//
//            System.out.println("Solicitei P2P OP: " + opP2P + " resposta: " + respP2P + " end: " + infoSrvDir[2]);
//        } catch (Exception e) {
//            System.out.println("Ocorreu um erro ao solicitar dados do client-serv: " + opP2P + " IP: " + hostP2P[0] + " PORTA: " + Integer.parseInt(hostP2P[1]));
//        }
//    } else if (infoSrvDir[0].equals("GET") && infoSrvDir[2].equals("null")) {
//        System.out.println("***************\nPesquisa não encontrada no servidor de diretorios!\n***************");
//    }

//    private String processar(String operacao, String key) {
////        "GET;" + msg + ";" + "127.0.0.1:6789"
//        String temp = null;
//        if (operacao.equals("FIND")) {
//            temp = "FIND;" + key + ";" + "127.0.0.1:" + this.portServDir;
//        } else if (operacao.equals("REG")) {
//            temp = "REG;" + key + ";" + "127.0.0.1:" + this.portClientSrv;
//            Shared shared = this.registerShare();
//            this.shareList.put(shared, key);//"diretorioXYZ" + (new Random().nextInt() * 10)
//        } else if (operacao.equals("GET")) {
//            temp = "GET;" + key + ";" + "127.0.0.1:" + this.portClientSrv;
//        }
//
//        return temp;
//    }

    private Shared registerShareConsole() {
        BufferedReader in =
                new BufferedReader(new InputStreamReader(System.in));

        String title;
        String directory;
        File file;

        try {
            do {
                System.out.println("Informe o diretorio ou arquivo a ser compartilhado: ");
                directory = in.readLine();
                file = new File(directory);
                title = file.getName();
            } while (!file.exists());

            return registerShare(title, directory);
        } catch (IOException e) {
            System.out.println("registerShareConsole: Error I/O");
        }
        return null;
    }

    private Shared registerShare(String title, String directory) {

        Long size = FileManager.getSizeFolder(directory);

        Shared shared = new Shared(0L, title, directory, size);
        SharedDAO dao = new SharedDAO();

        return dao.insert(shared);

    }

    private void deleteShareConsole() {
        BufferedReader in =
                new BufferedReader(new InputStreamReader(System.in));

        try {
            Long id = Long.valueOf(in.readLine());
            deleteShare(id);
        } catch (IOException e) {
            System.out.println("registerShareConsole: Error I/O");
        }
    }

    private boolean deleteShare(Long id) {
        SharedDAO dao = new SharedDAO();
        return dao.delete(id);
    }

    private String conectar(String msg, String hostname, Integer port) throws IOException {
        String sentence;
        String respSrv;

        Socket clientSocket = new Socket(hostname, port);//6543
        ipAddress = clientSocket.getLocalAddress().getHostAddress();
        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        DataInputStream inFromServer =
                new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        String textoEnviado = "Ola mundo";

        outToServer.writeChar('s');                 // operacao
        outToServer.writeInt(0);                    // indice
        outToServer.writeInt(textoEnviado.length());   // length
        outToServer.writeBytes(textoEnviado);          // data

        char type = inFromServer.readChar();
        int length = inFromServer.readInt();
        byte[] retornoSRV = inFromServer.readNBytes(length);

        String resposta = new String(retornoSRV);
        System.out.println("resposta do servidor: " + resposta);

        clientSocket.close();
        return "";
    }

    public ByteArrayOutputStream teste(String s) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(1);
        dos.writeChars(s); //Diretório a ser listado
        return bos;
    }

    public static void main(String[] args) throws Exception {
        TCPClienteServidor cli = new TCPClienteServidor(6789);
        cli.disconnect();

    }

    private void disconnect() throws Exception {
        if (started) this.processar("e");
    }
}
