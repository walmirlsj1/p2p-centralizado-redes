package projkurose.peer;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import projkurose.core.FileManager;
import projkurose.peer.model.Shared;
import projkurose.peer.model.SharedDAO;

/**
 * Ideias
 * enviar arquivo
 * para continuar download usar indice de ultimo pacote recebido
 * quando iniciar download enviar indice 0
 * para continuar download enviar indece do ultimo pacote recebido
 * salvar progresso do donwload, para que não corromper o arquivo.!!
 */

public class Peer {

    private final Integer clientSrvPort;
    private final Integer serverPortDir;

    private final String serverIpDir;
    private Long clientId;

    private Socket clientSocket;
    private DataOutputStream send;
    private DataInputStream receive;

    private boolean started = false;

    char type;
    int length = 0;
    byte[] received;

    protected ExecutorService threadPool =
            Executors.newFixedThreadPool(20);

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
        this.startClient();
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

        System.out.println("===== Lista das operações possiveis ============");
        System.out.println("===  (R)EG: Registra Arquivo/Pasta           ===");
//      System.out.println("===  (G)ET: Solicita Download do arquivo     ===");
        System.out.println("=== (F)IND: Procura/Baixar arquivo no server ===");
        System.out.println("===  (D)EL: Remove um item compartilhado     ===");
        System.out.println("=== (E)XIT: Para sair                        ===");
        System.out.println("================================================");
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

//        } else if (op.equals("GET") || op.equals("G")) {  // GET: Solicita Download do arquivo
//            getListPeerForDownload();

        } else if (op.equals("FIND") || op.equals("F")) { // FIND: Procura arquivo no server

            findServer();

        } else if (op.equals("DEL") || op.equals("D")) { // DEL: Para compartilhamento Peer
            deleteShareConsole();

        } else if (op.equals("EXIT") || op.equals("E")) {
            disconnectFromServerDirectory();

            flagContinue = this.started = false;
        }

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

    private void init() throws Exception {
        applyServer();
    }

    private void register() throws SQLException, IOException {
        registerShareServer();
    }


    private void applyServer() throws Exception {
        serverGo('a', String.valueOf(this.clientSrvPort));

        String received = new String(this.received);
        try {
            this.clientId = Long.valueOf(received);
        } catch (Exception e) {
            throw new Exception("Não foi possivel registrar cliente no servidor");
        }
        System.out.println("Cliente registrado no servidor");
    }

    private void registerShareServer() throws SQLException, IOException {
        List<Shared> shares = new SharedDAO().findAll();
        if (shares.isEmpty()) return;

        /**
         * Resposta do servidor:
         * clientId$title;size()|title;size()|...title;size()
         */

        String shareText = "";
        for (Shared share : shares) {
            shareText += String.format("%s;%d|", share.getTitle(), share.getSize());
        }

        shareText = shareText.substring(0, shareText.length() - 1);

        String messagem = String.format("%d$%s", clientId, shareText);

        serverGo('r', messagem);
    }

    private void disconnectFromServerDirectory() throws IOException {
        serverGo('d', String.valueOf(this.clientId));

        String resposta = new String(received);
        System.out.println("Desconectado do servidor: " + resposta);
    }

    private void getListPeerForDownload() throws IOException {
        BufferedReader scanner =
                new BufferedReader(new InputStreamReader(System.in));

        Long id = -1L;
        while (id == -1L) {
            try {
                System.out.println("****** Para voltar menu principal digite uma letra  ******\n" +
                        "Informe numero ID para requisitar download: ");
                id = Long.valueOf(scanner.readLine());
            } catch (NumberFormatException | IOException e) {
                return;
            }
        }
        serverGo('s', String.valueOf(id));

        getFromServer(id, new String(received));

    }

    private void getFromServer(Long id, String clients) {
        this.threadPool.execute(new PeerReceive(id, clients));
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

        String[] list = new String(received).split(";");

        if (list.length == 0 || this.type == 'n') {
            System.out.println("Titulo: " + title + " - Não foi encontrado no servidor");
            return;
        }

        String[] listHash;

        for (String item : list) {
            listHash = item.split("[|]");
            System.out.println(String.format("     Id: %s Item: %s Seeds: %s    ", listHash[0], listHash[1], listHash[2]));
        }

        getListPeerForDownload();
    }

    private void receiveFromServer() throws IOException {
        this.type = receive.readChar();
        this.length = receive.readInt();
        this.received = receive.readNBytes(length);
    }


    private void sendServer(char op, String message) throws IOException {
        send.writeChar(op);                 // operacao
        send.writeInt(message.length());   // length
        send.writeBytes(message);          // data
        send.flush();
    }

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

    private void deleteShareConsole() throws SQLException {
        BufferedReader in =
                new BufferedReader(new InputStreamReader(System.in));

        SharedDAO dao = new SharedDAO();
        List<Shared> shared = dao.findAll();
        if (shared.isEmpty()) {
            System.out.println("Não a arquivos ou pastas compartilhados");
            return;
        }

        for (Shared s : shared) {
            System.out.println(String.format("Id: %d - Title: %s - Size: %d", s.getId(), s.getTitle(), s.getSize()));
        }

        try {
            Long id = Long.valueOf(in.readLine());
            deleteShare(id);
        } catch (IOException e) {
            System.out.println("deleteShareConsole: Error I/O");
        }
    }

    private boolean deleteShare(Long id) {
        SharedDAO dao = new SharedDAO();
        return dao.delete(id);
    }

    public void disconnect() throws Exception {
        if (started) this.processar("e");
    }
}
