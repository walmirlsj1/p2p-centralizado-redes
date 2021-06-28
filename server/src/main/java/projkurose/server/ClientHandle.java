package projkurose.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import projkurose.server.model.ClientDAO;
import projkurose.server.model.DirectoryDAO;
import projkurose.server.model.Client;
import projkurose.server.model.Directory;


public class ClientHandle implements Runnable {
    private final Socket connectionSocket;
    private String clientIP;

    public ClientHandle(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {
        this.tratarRequisicao();
    }

    private void tratarRequisicao() {

        try {
            DataInputStream recebe =
                    new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));

            DataOutputStream envia =
                    new DataOutputStream(connectionSocket.getOutputStream());

            this.clientIP = connectionSocket.getInetAddress().getHostAddress();

            this.processRequest(recebe, envia);
        } catch (IOException e) {
            throw new RuntimeException("tratarRequisição: Falha no tratamento da requisição - " + clientIP);
        }
    }

    private void processRequest(DataInputStream recebe, DataOutputStream envia) throws IOException {
        /**
         * Recebe dados do cliente
         */
        char operation = recebe.readChar();
        int length = recebe.readInt();
//        byte[] data = recebe.readNBytes(length);
        String data = new String(recebe.readNBytes(length));

        /**
         * Cria variaveis para ser usadas na resposta para o cliente
         */
        char operationReply = 'f';
        String messageReply = "Request Failed";
        /**
         * ~~~~~~~~~~~ SERVER ~~~~~~~~~~~
         * (s)eek         : share from clients
         * (r)egister     : share list need client_id*
         * (a)ply         : generate client_id* para client-serv
         * (d)isconnect   : from server need client_id*
         * (e)rror        : request
         * (o)K           : OK
         * (l)ist         : list all share or contains key*
         * dele(t)e       :
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
         * | operation | length | byte[]                                    |
         * ===========================================================================
         */
        switch (operation) {
            case 's':
                System.out.println("Seek file shared from clients " + clientIP);

                messageReply = seek(data);

                if (messageReply.length() == 0) {
                    operationReply = 'n';
                    messageReply = "Not found in shared list";
                } else operationReply = 'l';

                break;
            case 't': // cliente não responde a solicitação
                System.out.println("Delete client share " + clientIP);
                deleteClientShare(data);
                break;
            case 'r':
                System.out.println("Register shareds in server: " + clientIP);
                messageReply = "OK";
                operation = register(data) ? 'o' : 'f';

                if (operation == 'e') messageReply = "Failed process request";

                break;
            case 'a':
                System.out.println("Apply in server " + clientIP);
                messageReply = apply(data);
                operationReply = 'o';
                break;
            case 'd':
                System.out.println("Disconnect from server " + clientIP);
                messageReply = disconnect(data);
                operationReply = 'o';
                break;
            case 'l':
                System.out.println("List all share or contains key " + clientIP);

                messageReply = seekContains(data);

                if (messageReply.equals("")) {
                    operationReply = 'n';
                    messageReply = "Not found in shared list";
                } else operationReply = 'l';

                break;
            default:
                throw new IOException("Operation not found!");
        }

        envia.writeChar(operationReply);
        envia.writeInt(messageReply.length());
        envia.writeBytes(messageReply);
        envia.flush();
    }

    private void deleteClientShare(String s) {
        throw new NotImplementedException("Não implementado");
        /**
         * usar o hashcode para deletar?
         * mudar table para guardar hash, e titulo
         * @FIXME
         */
    }

    private String seekContains(String title) {
        DirectoryDAO dirDAO = new DirectoryDAO();
        List<Directory> list = dirDAO.findAllContainsTitle(title);
        String listDir = "";
        Long seeds = 0L;
        for (Directory d : list) {
            seeds = dirDAO.countUseDirectory(d);
            listDir += String.format("%d|%s|%d;", d.getId(), d.getTitle(), seeds); //d.getTitle() + ";"
            System.out.println(String.format("%d|%s|%d;", d.getId(), d.getTitle(), seeds));
        }
        listDir = listDir.length() > 0 ? listDir.substring(0, listDir.length() - 1) : "";
        return listDir;
    }

    private String disconnect(String id) {
        ClientDAO clientDAO = new ClientDAO();

        Client client = clientDAO.findById(Long.valueOf(id));
        DirectoryDAO directoryDAO = new DirectoryDAO();

        directoryDAO.deleteAllDirectoryByClient(client);

        clientDAO.delete(client.getId());
        return "OK";
    }

    private String apply(String porta) {  // s = meuIP:porta
        ClientDAO clientDAO = new ClientDAO();

        String clientPeer = String.format("%s:%s", clientIP, porta);

        Client client = clientDAO.findByAddress(clientPeer);
        System.out.println("Cliente " + client == null);

        if (client == null) {
            client = new Client();
            client.setAddress(clientPeer);
            client = clientDAO.insert(client);
        }

        System.out.println("New client connected: " + clientIP);

        return String.valueOf(client.getId());
    }

    private String seek(String text_id) {
        DirectoryDAO dirDAO = new DirectoryDAO();
        Directory dir;
        String clientList = "";
        Long id = 0L;

        id = Long.valueOf(text_id);
        dir = dirDAO.findById(id);

        if(dir == null) return clientList;

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

        String[] pacote;
        pacote = pacoteRecebido.split("[$]");

        Long client_id;
        try {
            client_id = Long.valueOf(pacote[0]);
        } catch (Exception e) {
            System.out.println("Error register: " + e.getMessage());
            return false;
        }
        ClientDAO clientDAO = new ClientDAO();
        DirectoryDAO directoryDAO = new DirectoryDAO();

        Client client = clientDAO.findById(client_id); // clientDAO.find //

        if (client == null) return false;

        Directory directory;
        Directory directoryTemp;

        List<Directory> listDirectory = new ArrayList<>();

        String[] list = pacote[1].split("[|]");

        List<Directory> directoriesClient = directoryDAO.getAllDirectoryByClient(client);
        /**
         * Podemos ter 3 casos aqui
         * 1 - Compartilhamentos cadastro já realizado
         * 2 - Compartilhamentos Parcialmente cadastrado
         * 3 - Compartilhamentos Não cadastrado
         */

        for (String l : list) {
            String[] directoryTam = l.split(";");

            directory = new Directory();
            directory.setTitle(directoryTam[0]);
            directory.setSize(Long.valueOf(directoryTam[1]));

            if (!directoriesClient.contains(directory)) {
                directoryTemp = directoryDAO.findByTitle(directoryTam[0]);
                if (directoryTemp == null) {

                    directoryTemp = directoryDAO.insert(directory);

                    if (directoryTemp != null) return false;
                }
                listDirectory.add(directoryTemp);
            }
        }

        for (Directory d1 : listDirectory) {
            directoryDAO.insertClientDirectory(d1, client);
        }
        return true;
    }


}
