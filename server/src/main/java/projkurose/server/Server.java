package projkurose.server;

import java.io.*;
import java.net.*;
import java.sql.SQLException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import projkurose.core.Config;
import projkurose.core.SQLiteJDBCDriverConnection;

public class Server {
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
    private int serverPort;

    protected ExecutorService threadPool =
            Executors.newFixedThreadPool(20);

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public void start() {
//        int port = 6543;
        ServerSocket socketServidor = null;
        try {
            socketServidor = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException(String.format("start: Falha ao criar socket na porta: %d ", this.serverPort));
        }
        System.out.println(" ::::::: PORT: " + this.serverPort + " ::::::: ");

        while (!socketServidor.isClosed()) {
            Socket client = null;
            System.out.println("Aguardando clientes");
            try {
                client = socketServidor.accept();
            } catch (IOException e) {
                throw new RuntimeException("start: Falha ao aceitar conexão do cliente");
            }
            this.threadPool.execute(new ClientHandler(client));
        }
    }


    public static void main(String[] args) throws IOException, SQLException {
        System.out.println(" ::::::: Servidor DIRETORIOS ::::::: ");
        int serverPort = 7000;
        try {
            serverPort = Config.getConfiguracao().getInt("srv_port");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Config.deleteConfig();
            throw new RuntimeException("Falha na configuração, tente novamente!");
        }
        String sql =
                "CREATE TABLE IF NOT EXISTS DIRECTORY (" +
                        " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        " TITLE VARCHAR(80), " +
                        " SHARED_PATH VARCHAR(255), " +
                        " SIZE_PATH INTEGER" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS CLIENT (" +
                        " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        " ADDRESS VARCHAR(21)" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS CLIENT_DIRECTORY (" +
                        " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        " CLIENT_ID INTEGER NOT_NUL, " +
                        " DIRECTORY_ID INTEGER NOT_NUL, " +
                        " FOREIGN KEY (CLIENT_ID) REFERENCES CLIENT (ID)" +
                        "    ON DELETE CASCADE, " +
                        " FOREIGN KEY (DIRECTORY_ID) REFERENCES DIRECTORY (ID)" +
                        "    ON DELETE CASCADE " +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS CLIENT(USER_ID VARCHAR(255), LAST_UPDATE DATETIME);";

        SQLiteJDBCDriverConnection.database = "./databaseSrv.db";
        SQLiteJDBCDriverConnection.checkDatabase(sql);

        new Server(serverPort).start();

    }

}
