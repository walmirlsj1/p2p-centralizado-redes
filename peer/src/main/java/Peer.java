import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.configuration2.PropertiesConfiguration;

import projkurose.core.FileManager;
import projkurose.core.Config;
import model.Shared;
import model.SharedDAO;
import projkurose.core.SQLiteJDBCDriverConnection;

/**
 * Ideias
 * enviar arquivo
 * para continuar download usar indice de ultimo pacote recebido
 * quando iniciar download enviar indice 0
 * para continuar download enviar indece do ultimo pacote recebido
 * salvar progresso do donwload, para que não corromper o arquivo.!!
 */

public class Peer {

    private Integer clientSrvPort, serverPortDir;

    private String serverIpDir;
    private Long clientId;


    private Socket clientSocket;
    private DataOutputStream send;
    private DataInputStream receive;

    private boolean started = false;

    char type;
    int length = 0;
    byte[] retornoSRV;


    public Peer(Long clientId, int clientSrvPort, String serverIpDir, int serverPortDir) {
        this.clientSrvPort = clientSrvPort; //6789
        this.clientId = clientId;
        this.serverIpDir = serverIpDir;
        this.serverPortDir = serverPortDir;
    }

    public void run() throws Exception {
        init();
        register();

        this.started = true;
        this.startThreadSrv();
        this.startClient();
    }

    private void startThreadSrv() {
        Runnable threadSrv = () -> {
            try {
                System.out.println("Iniciando Servidor Local porta: " + clientSrvPort);
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

    private void startClient() throws Exception {
        System.out.println("Iniciando Cliente");
        boolean flagContinue = true;
        while (flagContinue) {
            flagContinue = clientConsole();
        }
        System.exit(0);
    }

    private boolean clientConsole() throws Exception {

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        System.out.println("===== Lista das operações possiveis ========");
        System.out.println("===  (R)EG: Registra Arquivo/Pasta       ===");
        System.out.println("===  (G)ET: Solicita Download do arquivo ===");
        System.out.println("=== (F)IND: Procura arquivo no server    ===");
        System.out.println("===  (D)EL: Remove um item compartilhado ===");
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
         *          * (r)egister     : share list need clientId*
         *          * (a)ply         : generate clientId* para client-serv
         *          * (d)isconnect   : from server need clientId*
         *          * (e)rror        : request
         *          * (o)K           : OK
         *          * (l)ist         : list all share or contains key*
         */
        boolean flagContinue = true;


        if (op.equals("REG") || op.equals("R")) {
            registerShareConsole();
            /* @FIXME Estamos enviando todos os items compartilhados, pra adiantar */
            registerShareServer();

        } else if (op.equals("GET") || op.equals("G")) {  // GET: Solicita Download do arquivo
            getListPeerForDownload();

        } else if (op.equals("FIND") || op.equals("F")) { // FIND: Procura arquivo no server
            findServer();
        } else if (op.equals("DEL") || op.equals("D")) { // FIND: Procura arquivo no server
            /* @TODO falta implementar: deletar um item do compartilhamento */
            throw new Exception("Delete não implementado");

        } else if (op.equals("EXIT") || op.equals("E")) {
            disconnectFromServerDirectory();

            flagContinue = this.started = false;
        }

//        clientSocket.close();
        return flagContinue;
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

        this.type = receive.readChar();
        this.length = receive.readInt();
        this.retornoSRV = receive.readNBytes(length);

    }

    private void init() throws Exception {
        applyServer();
    }

    private void register() throws SQLException, IOException {
        registerShareServer();
    }


    private void applyServer() throws Exception {
        serverGo('a', String.valueOf(this.clientSrvPort));
//        getConnectionServerDir();
//        this.sendServer('a', String.valueOf(this.clientSrvPort));
//        this.receiveFromServer();
//        clientSocket.close();

        String received = new String(retornoSRV);
        try {
            this.clientId = Long.valueOf(received);
        } catch (Exception e) {
            throw new Exception("Não foi possivel registrar cliente no servidor");
        }
        System.out.println("Meu ID: " + this.clientId);
    }

    private void registerShareServer() throws SQLException, IOException {
        List<Shared> shares = new SharedDAO().findAll();
        if (shares.isEmpty()) return;

//        clientId.toString() + "$" + share.getTitle() + ";" + share.getSize()
        String shareText = "";
        for (Shared share : shares) {
            shareText += String.format("%s;%d|", share.getTitle(), share.getSize());
        }
        System.out.println("Lista vazia: " + shares.isEmpty());
        shareText = shareText.substring(0, shareText.length() - 1);

        String messagem = String.format("%d$%s", clientId, shareText);

        serverGo('r', messagem);

        clientSocket.close();
    }

    private void disconnectFromServerDirectory() throws IOException {
        serverGo('d', String.valueOf(this.clientId));

        String resposta = new String(retornoSRV);
        System.out.println("Desconectado do servidor: " + resposta);
    }

    private void getListPeerForDownload() throws IOException {
        BufferedReader scanner =
                new BufferedReader(new InputStreamReader(System.in));

        Long id = -1L;
        while (id == -1L) {
            try {
                System.out.println("Informe o ID para requisitar download: ");
                id = Long.valueOf(scanner.readLine());
            } catch (IOException e) {
                System.out.println("Error getListPeerForDownload: " + e.getMessage());
                id = -1L;
            }
        }
//        System.out.println("OPERATION GET: " + id);

        serverGo('s', String.valueOf(id));

        String resposta = new String(retornoSRV);
        System.out.println("resposta do servidor: " + resposta);

    }

    private void findServer() throws IOException {
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
        serverGo('l', title);
        if(this.type == 'n') {
            System.out.println(new String(retornoSRV));
            return;
        }

        String[] list = new String(retornoSRV).split(";");
        String[] listHash;
        if (list.length == 0) {
            System.out.println("Titulo: " + title + " - Não foi encontrado no servidor");
            return;
        }

        for (String item : list) {
            listHash = item.split("[|]");
            System.out.println(String.format("Id: %s Item: %s Seeds: %s",listHash[0], listHash[1], listHash[2]));
        }
    }

    private void receiveFromServer() throws IOException {
        this.type = receive.readChar();
        this.length = receive.readInt();
        this.retornoSRV = receive.readNBytes(length);
    }


    private void sendServer(char op, String message) throws IOException {
        send.writeChar(op);                 // operacao
        send.writeInt(message.length());   // length
        send.writeBytes(message);          // data
        send.flush();
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
//            temp = "FIND;" + key + ";" + "127.0.0.1:" + this.serverPortDir;
//        } else if (operacao.equals("REG")) {
//            temp = "REG;" + key + ";" + "127.0.0.1:" + this.clientSrvPort;
//            Shared shared = this.registerShare();
//            this.shareList.put(shared, key);//"diretorioXYZ" + (new Random().nextInt() * 10)
//        } else if (operacao.equals("GET")) {
//            temp = "GET;" + key + ";" + "127.0.0.1:" + this.clientSrvPort;
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

        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        DataInputStream inFromServer =
                new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        String textoEnviado = "Ola mundo";

        outToServer.writeChar('s');                 // operacao
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

    public void disconnect() throws Exception {
        if (started) this.processar("e");
    }

    public static void main(String[] args) throws Exception {

        String serverIpDir;
        int clientSrvPort, serverPortDir;
        Long clientId;

        try {
            PropertiesConfiguration config = Config.getConfiguracao();
            clientSrvPort = config.getInt("client_port"); //6789
            clientId = config.getLong("client_id");

            serverIpDir = config.getString("server_ip");
            serverPortDir = config.getInt("server_port");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Config.deleteConfig();
            throw new RuntimeException("Falha na configuração, tente novamente!");
        }

        SQLiteJDBCDriverConnection.geraDB();

        Peer cli = new Peer(clientId, clientSrvPort, serverIpDir, serverPortDir);
        cli.run();
        cli.disconnect();

    }
}
