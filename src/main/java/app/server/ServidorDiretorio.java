package app.server;

import app.config.Config;
import app.server.model.Client;
import app.server.model.ClientDAO;
import app.server.model.Directory;
import app.server.model.DirectoryDAO;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;

public class ServidorDiretorio {
    /**
     * Conhece todos os clientes, e os seus compartilhamentos
     * <p>
     * key-nome-compartilhamento-uniq, ip:?
     * <p>
     * cliente solicita dados ao servidor de diretorio,
     * este servidor responde hashMap<key, valor>;;??
     * <p>
     * o "cliente" com esse informação solicita os dados para os servidores
     * que possuem esse compartilhamento ativo
     * <p>
     * quebrar o arquivo em pedacos de 16kb? e enviar esses pedaços..
     * montar no lado do cliente.. semelhante ao processo do zip: zip.001 ~ zip.120
     * <p>
     * sha1 ou md5 para verificar arquivos.. zip.001 - sha1...
     * hash de arquivo final?
     */
    private HashMap<String, String> shareListIp;
    private int port;
    private String databaseName = null;
    private String localPort = null;

    public ServidorDiretorio(int port) {
        this.port = port;
        this.carregaConfig();
        this.shareListIp = new HashMap<String, String>();

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

    private String processar(String operacao, String key, String ip) {
        String temp = "Mal Formatado";
        if (operacao.equals("FIND")) {
            temp = "GET;" + key + ";" + this.shareListIp.get(key);
        } else if (operacao.equals("REG")) {
            this.shareListIp.put(key, ip);
            temp = "OK";
        } else if (operacao.equals("REGALL")) {

        }

        return temp;
    }

    public void start() throws IOException, SQLException {
//        int port = 6543;
        ServerSocket welcomeSocket = new ServerSocket(this.port);
        System.out.println(" ::::::: PORT: " + this.port + " ::::::: ");
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            this.tratarRequisicao(connectionSocket);
        }
    }

    private void processRequest(DataInputStream recebe, DataOutputStream envia) throws IOException, SQLException {
        /**
         * Recebe dados do cliente
         */
        char operation = recebe.readChar();
        int length = recebe.readInt();
        int indice = recebe.readInt();
        byte[] data = recebe.readNBytes(length);

        /**
         * Cria variaveis para ser usadas na resposta para o cliente
         */
        char operationReply = 'f';
        int lengthReply = 0;
        int indiceReply = 0;
//        byte[] dataReply;

        String messageReply = "";
        /**
         * ~~~~~~~~~~~ SERVER ~~~~~~~~~~~
         * (s)eek         : share from clients
         * (n)ew          : seek client-serv offline
         * (r)egister     : share list need client_id*
         * (a)ply         : generate client_id* para client-serv
         * (d)isconnect   : from server need client_id*
         * (e)rror        : request
         * (o)K           : OK
         * (l)ist         : list all share or contains key*
         *
         * preciso do client_id...pra evitar duplicidade
         * e uma rotina para limpar clientes offline
         *
         * ~~~~~~~~~~~ CLIENT ~~~~~~~~~~~
         * (l)ist         : clientServer hold file IP:PORT;IP:PORT \\'';''// IP:PORT;
         * (e)rror        : failed process request
         * (n)ot          : found in shared list
         * (o)k           : OK
         * (k)ey          : cliente_id
         *
         * ===========================================================================
         * | operation | length | indice | byte[]                                    |
         * ===========================================================================
         */
        switch (operation) {
            case 's':
                System.out.println("Seek file shared from clients");

                messageReply = seek(new String(data));

                if (messageReply.equals("NULL")) {
                    operationReply = 'n';
                    messageReply = "Not found in shared list";
                } else operationReply = 'l';

                break;
            case 'n': // cliente não responde a solicitação
                System.out.println("New seek serverClient offline");
            case 'r':
                System.out.println("Register shareds in server");

                operation = register(new String(data)) ? 'o' : 'f';

                if (operation == 'e') messageReply = "Failed process request";

                break;
            case 'a':
                System.out.println("Apply in server");
                //apply generate cliente_id
                break;
            case 'd':
                System.out.println("Disconnect from server");
                //delete from client_id
                break;
            case 'l':
                System.out.println("List all share or contains key");
            default:
                throw new IOException("Operation not found!");
        }

//        char operationReply = 'f';
//        int lengthReply = 0;
//        int indiceReply = 0;
//        byte[] dataReply;
        envia.writeChar(operationReply);
        envia.writeInt(messageReply.length());
        envia.writeInt(indiceReply);
        envia.writeBytes(messageReply);

//        return reply;
    }

    private String seek(String text_id) {
        DirectoryDAO dirDAO = new DirectoryDAO();
        Directory dir;
        String clientList = "NULL";

        Long id = Long.getLong(text_id);

        dir = dirDAO.findById(id);
        List<Client> listClients = dirDAO.findAllClientsByDirectory(dir);

        if (!listClients.isEmpty()) {
            clientList = "";

            for (Client c : listClients) {
                clientList += c.getAddress() + ";";
            }

            clientList = clientList.substring(0, clientList.length() - 1); // remove ultimo ';'
        }
        return clientList;
    }

    private boolean register(String pacoteRecebido) {
        String pacote[] = pacoteRecebido.split("$");

        Long client_id = Long.valueOf(pacote[0]);

        ClientDAO clientDAO = new ClientDAO();
        DirectoryDAO directoryDAO = new DirectoryDAO();

        Client client = clientDAO.findById(client_id); // clientDAO.find //

        if (client == null) return false;

        Directory directory;

        List<Directory> listDirectory = new ArrayList<>();

        String[] list = pacote[1].split(";");

        List<Directory> directoriesClient = directoryDAO.getAllDirectoryByClient(client);
        /**
         * Podemos ter 3 casos aqui
         * 1 - Compartilhamentos cadastro já realizado
         * 2 - Compartilhamentos Parcialmente cadastrado
         * 3 - Compartilhamentos Não cadastrado
         */

        for (String l : list) {
            String[] directoryTam = l.split("|");

            directory = new Directory();
            directory.setTitle(directoryTam[0]);
            directory.setSize(Long.valueOf(directoryTam[0]));

            if (directoriesClient.indexOf(directory) > 0) {
                directory = directoryDAO.findByTitle(directoryTam[0]);
                if (directory == null) {
                    if (directoryDAO.insert(directory) != null) return false;
                }
                listDirectory.add(directory);
            }
        }


        for (Directory d1 : listDirectory) {
            directoryDAO.insertClientDirectory(d1, client);
        }

        return true;
    }

    private void tratarRequisicao(Socket connectionSocket) throws IOException, SQLException {
        String clientSentence;
        String capitalizedSentence;

//        BufferedReader inFromClient =
//                new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        DataInputStream recebe =
                new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));

        DataOutputStream envia =
                new DataOutputStream(connectionSocket.getOutputStream());

        this.processRequest(recebe, envia);
//        ByteArrayInputStream bos = new ByteArrayInputStream();

//        StringBuilder dataString = new StringBuilder(length);
//        clientSentence = dataString.append(messagem).toString();
//        clientSentence = new String(messagem);
//
//        System.out.println(clientSentence);
//        String clientSentenceResposta = clientSentence + " recebido!";
//        outToClient.writeChar('r');
//        outToClient.writeInt(clientSentenceResposta.length());
//        outToClient.writeBytes(clientSentenceResposta);

//        clientSentence = inFromClient.readLine();
//
//        capitalizedSentence = clientSentence.toUpperCase() + '\n';
//
//        String[] info = clientSentence.trim().split(";");
//        String resp = "recusado";
//        if (info.length > 1)
//            resp = this.processar(info[0], info[1], info[2]);
//
//        System.out.println("Host: " + connectionSocket.getRemoteSocketAddress()
//                + " Data: " + capitalizedSentence.trim()
//                + " Tamanho: " + capitalizedSentence.length());
//
//        outToClient.writeBytes(resp + '\n');
    }

    public static void main(String[] args) throws IOException, SQLException {
//        Socket chatSocket = new Socket("127.0.0.1", 5000);
//        InputStreamReader stream = new InputStreamReader(chatSocket.getInputStream());
//
//        BufferedReader reader = new BufferedReader(stream);
//
//        String message = reader.readLine();
        System.out.println(" ::::::: Servidor DIRETORIOS ::::::: ");
        int portServidor = 6543;

        new ServidorDiretorio(portServidor).start();

    }

}
