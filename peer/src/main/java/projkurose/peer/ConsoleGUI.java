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
    static BufferedReader bfr;

    public ConsoleGUI(Long clientId, int clientSrvPort, String serverIpDir, int serverPortDir) throws IOException {
        client = new ServerHandle(clientId, clientSrvPort, serverIpDir, serverPortDir);
        client.registerShareServer(new SharedDAO().findAll());
    }

    public void run() throws IOException {
        System.out.println("Iniciando Cliente");
        bfr = new BufferedReader(new InputStreamReader(System.in));

        boolean flagContinue = true;
        while (flagContinue) {
            flagContinue = clientConsole();
        }
    }

    private boolean clientConsole() throws IOException {

        ClearConsole();

        System.out.println("===== Lista das operações possiveis ============");
        System.out.println("===  (R)EG: Registra Arquivo/Pasta           ===");
//      System.out.println("===  (G)ET: Solicita Download do arquivo     ===");
        System.out.println("=== (F)IND: Procura/Baixar arquivo no server ===");
        System.out.println("===  (D)EL: Remove um item compartilhado     ===");
        System.out.println("=== (E)XIT: Para sair                        ===");
        System.out.println("================================================");
        System.out.println("Informe OP: R | F | D | E");
        String op = bfr.readLine();

        if (op.length() == 0) return true;
        return this.processar(op.toUpperCase(Locale.ROOT).charAt(0));
    }

    private boolean processar(char op) throws IOException {
        /**
         *          * (s)eek         : share from clients
         *          * (r)egister     : share list need clientId*
         *          * (a)ply         : generate clientId* para client-serv
         *          * (d)isconnect   : from server need clientId*
         *          * (e)rror        : request
         *          * (o)K           : OK
         *          * (l)ist         : list all share or contains key*
         */
        boolean flagContinue = true;

        switch (op) {
            case 'R':
                registerShareConsole();
                /* @FIXME Estamos enviando todos os items compartilhados, pra adiantar */
                break;
            case 'F':
                findServerConsole();
                break;
            case 'D': // DEL: Para compartilhamento ConsoleGUI
                deleteShareConsole();
                break;
            case 'E':
                disconnect();
                flagContinue = false;
                break;
            default:
                System.out.println("Opção Incorreta!");
        }
        return flagContinue;
    }

    private void getListPeerForDownloadConsole() throws IOException {

        Long id = -1L;
        String path_dir = "";

        while (id == -1L) {
            try {
                System.out.println("****** Para voltar menu principal digite uma letra  ******\n" +
                        "Informe numero ID para requisitar download: ");
                id = Long.valueOf(bfr.readLine());

            } catch (NumberFormatException | IOException e) {
                return;
            }
        }
        while (path_dir.length() == 0) {
            try {
                System.out.println("Informe diretorio para salvar download: ");
                path_dir = String.valueOf(bfr.readLine());
            } catch (IOException e) {
                return;
            }
        }

        if (!client.getFromServer(id, path_dir)) System.out.println(String.format("ID: %d - não encontrado!", id));
    }


    private void findServerConsole() throws IOException {
        String title = "";

        while (title.equals("")) {
            try {
                System.out.println("Informe um titulo para pesquisa");
                title = bfr.readLine();
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

        String title;
        String directory;
        File file;

        try {
            do {
                System.out.println("Informe o diretorio ou arquivo a ser compartilhado: ");
                directory = bfr.readLine();
                file = new File(directory);
                /**
                 * @FIXME
                 * Não verifica se diretorio existe
                 */

                System.out.println("Informe o titulo: ");
                title = bfr.readLine();
            } while (!file.exists());
            SharedDAO sharedDAO = new SharedDAO();
            Shared shared = sharedDAO.registerShare(title, directory);
            if (shared != null) client.registerShareServer(shared);

        } catch (IOException e) {
            System.out.println("registerShareConsole: Error I/O");
        }
    }

    private void deleteShareConsole() {

        SharedDAO dao = new SharedDAO();
        List<Shared> shared = dao.findAll();
        if (shared.isEmpty()) {
            System.out.println("Não a arquivos ou pastas compartilhados");
            return;
        }

        for (Shared s : shared) {
            System.out.println(String.format("Id: %d - Title: %s - Size: %d", s.getId(), s.getTitle(), s.getSize()));
        }

        Long id = 1L;

        while (id == -1L) {
            try {
                id = Long.valueOf(bfr.readLine());

            } catch (NumberFormatException | IOException e) {
                return;
            }
        }

        SharedDAO sharedDAO = new SharedDAO();
        sharedDAO.delete(id);

    }

    public void disconnect() throws IOException {
        client.disconnectFromServerDirectory();
    }

    public static void ClearConsole() {
        try {
            String operatingSystem = System.getProperty("os.name"); //Check the current operating system
            if (operatingSystem.contains("Windows")) {
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "cls");
                Process startProcess = pb.inheritIO().start();
                startProcess.waitFor();
            } else {
                ProcessBuilder pb = new ProcessBuilder("clear");
                Process startProcess = pb.inheritIO().start();

                startProcess.waitFor();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
