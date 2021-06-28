package projkurose.peer;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

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

public class ConsoleGUI {
    ServerHandle client;

    public ConsoleGUI(Long clientId, int clientSrvPort, String serverIpDir, int serverPortDir) throws IOException {
        client = new ServerHandle(clientId, clientSrvPort, serverIpDir, serverPortDir);
        client.registerShareServer(new SharedDAO().findAll());
    }

    public void run() throws IOException {
        System.out.println("Iniciando Cliente");
        boolean flagContinue = true;
        while (flagContinue) {
            flagContinue = clientConsole();
        }
    }

    private boolean clientConsole() throws IOException {

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

    private boolean processar(String op) throws IOException {
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


//        } else if (op.equals("GET") || op.equals("G")) {  // GET: Solicita Download do arquivo
//            getListPeerForDownload();

        } else if (op.equals("FIND") || op.equals("F")) { // FIND: Procura arquivo no server
            findServerConsole();

        } else if (op.equals("DEL") || op.equals("D")) { // DEL: Para compartilhamento ConsoleGUI
            deleteShareConsole();

        } else if (op.equals("EXIT") || op.equals("E")) {
            disconnect();

            flagContinue = false;
        }

        return flagContinue;
    }

    private void getListPeerForDownloadConsole() throws IOException {
        BufferedReader scanner =
                new BufferedReader(new InputStreamReader(System.in));

        Long id = -1L;
        String path_dir = "";

        while (id == -1L) {
            try {
                System.out.println("****** Para voltar menu principal digite uma letra  ******\n" +
                        "Informe numero ID para requisitar download: ");
                id = Long.valueOf(scanner.readLine());

            } catch (NumberFormatException | IOException e) {
                return;
            }
        }
        while (path_dir.length() == 0) {
            try {
                System.out.println("Informe diretorio para salvar download: ");
                path_dir = String.valueOf(scanner.readLine());
            } catch (IOException e) {
                return;
            }
        }

        if (!client.getFromServer(id, path_dir)) System.out.println(String.format("ID: %d - não encontrado!", id));
    }


    private void findServerConsole() throws IOException {
        BufferedReader scanner =
                new BufferedReader(new InputStreamReader(System.in));
        String title = "";

        while (title.equals("")) {
            try {
                System.out.println("Informe um titulo para pesquisa");
                title = scanner.readLine();
            } catch (IOException e) {
                System.out.println("Error getListPeerForDownload: " + e.getMessage());
            }
        }
        String[] list = client.findTitleServer(title);
        if (list == null || list.length == 0) {
            System.out.println("Titulo: " + title + " - Não foi encontrado no servidor");
            return;
        }

        String[] listHash;

        for (String item : list) {
            listHash = item.split("[|]");
            System.out.println(String.format("     Id: %s Item: %s Seeds: %s    ", listHash[0], listHash[1], listHash[2]));
        }
        getListPeerForDownloadConsole();

    }

    private void registerShareConsole() {
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
                System.out.println("Informe o titulo: ");
                title = in.readLine();
            } while (!file.exists());
            SharedDAO sharedDAO = new SharedDAO();
            Shared shared = sharedDAO.registerShare(title, directory);
            if (shared != null) client.registerShareServer(shared);

        } catch (IOException e) {
            System.out.println("registerShareConsole: Error I/O");
        }
    }

    private void deleteShareConsole() {
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
            SharedDAO sharedDAO = new SharedDAO();
            sharedDAO.delete(id);
        } catch (IOException e) {
            System.out.println("deleteShareConsole: Error I/O");
        }
    }

    public void disconnect() throws IOException {
        client.disconnectFromServerDirectory();
    }



}
