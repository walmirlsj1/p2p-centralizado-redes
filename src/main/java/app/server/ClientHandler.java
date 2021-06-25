package app.server;

import app.server.model.Client;
import app.server.model.ClientDAO;
import app.server.model.Directory;
import app.server.model.DirectoryDAO;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientHandle {

    private void processRequest(DataInputStream recebe, DataOutputStream envia) throws IOException, SQLException {
        /**
         * Recebe dados do cliente
         */
        char operation = recebe.readChar();
        int indice = recebe.readInt();
        int length = recebe.readInt();
        byte[] data = recebe.readNBytes(length);

        /**
         * Cria variaveis para ser usadas na resposta para o cliente
         */
        char operationReply = 'f';
        int lengthReply = 0;
        int indiceReply = 0;
//        byte[] dataReply;

        String messageReply = "Request Failed";
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
         * | operation | indice | length | byte[]                                    |
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
                /* @TODO falta implementar */

                break;
            case 'r':
                System.out.println("Register shareds in server");
                messageReply = "OK";
                operation = register(new String(data)) ? 'o' : 'f';

                if (operation == 'e') messageReply = "Failed process request";

                break;
            case 'a':
                System.out.println("Apply in server");
                //apply generate cliente_id
                messageReply = apply(new String(data));
                operationReply = 'o';
                break;
            case 'd':
                System.out.println("Disconnect from server");
                messageReply = disconnect(new String(data));
                operationReply = 'o';
                //delete from client_id
                break;
            case 'l':
                System.out.println("List all share or contains key");

                messageReply = seekContains(new String(data));

                if (messageReply.equals("")) {
                    operationReply = 'n';
                    messageReply = "Not found in shared list";
                } else operationReply = 'l';

                break;
            default:
                throw new IOException("Operation not found!");
        }

//        char operationReply = 'f';
//        int lengthReply = 0;
//        int indiceReply = 0;
//        byte[] dataReply;
        envia.writeChar(operationReply);
        envia.writeInt(indiceReply);
        envia.writeInt(messageReply.length());
        envia.writeBytes(messageReply);

//        return reply;
    }

    private String seekContains(String title) {
        DirectoryDAO dirDAO = new DirectoryDAO();
        List<Directory> list = dirDAO.findAllContainsTitle(title);
        String listDir = "";
        for (Directory d : list) {
            listDir += d.getTitle() + ";";
        }
        listDir = listDir.length() > 0 ? listDir.substring(0, listDir.length() - 1) : "";
        System.out.println("Seek contains " + title + " -- " + (listDir.length() > 0 ? listDir : "Não encontrado"));
        return listDir;
    }

    private String disconnect(String s) {
        ClientDAO clientDAO = new ClientDAO();

        Client client = clientDAO.findById(Long.valueOf(s));
        DirectoryDAO directoryDAO = new DirectoryDAO();

        directoryDAO.deleteAllDirectoryByClient(client);

        clientDAO.delete(client.getId());
        return "OK";
    }

    private String apply(String s) {  // s = meuIP:porta
        ClientDAO clientDAO = new ClientDAO();
        /*
         * Precisamos guardar o ip do cliente que foi recebido na conexão
         * Porque num primeiro momento, o cliente não sabe exatamente o ip externo
         * porém a porta de escuta é possivel obter
         * */
        Client client = clientDAO.findByAddress(s);

        if (client == null) {
            client = new Client();
            client.setAddress(s);
            client = clientDAO.insert(client);
        }
        return String.valueOf(client.getId());
    }

    private String seek(String text_id) {
        DirectoryDAO dirDAO = new DirectoryDAO();
        Directory dir;
        String clientList = "NULL";
        Long id = 0L;
        System.out.println("seek id recebido: " + text_id);

//        try {
        id = Long.valueOf(text_id);
        dir = dirDAO.findById(id);
//        } catch (NullPointerException e) {
//            System.out.println("Error seek: " + e.getMessage());
//            return clientList;
//        }

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

        System.out.println(Arrays.toString(pacote));

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

        System.out.println("Cliente: " + client.getId() + " Address: " + client.getAddress());
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

//            System.out.println(Arrays.toString(directoryTam));
            directory = new Directory();
            directory.setTitle(directoryTam[0]);
            directory.setSize(Long.valueOf(directoryTam[1]));

            if (directoriesClient.indexOf(directory) == -1) {
                directoryTemp = directoryDAO.findByTitle(directoryTam[0]);
                if (directoryTemp == null) {

                    directoryTemp = directoryDAO.insert(directory);

                    if (directoryTemp != null) return false;
                    System.out.println("Directory Inserido: " + directory.getTitle());
                }
                listDirectory.add(directoryTemp);
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
        System.out.println("IP externo do cliente: " + connectionSocket.getRemoteSocketAddress());
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
}
