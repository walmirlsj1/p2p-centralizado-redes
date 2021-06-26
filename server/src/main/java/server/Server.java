package server;

import java.io.*;
import java.net.*;
import java.sql.SQLException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import projkurose.core.Config;
import projkurose.core.SQLiteJSrv;


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
        SQLiteJSrv.geraDB();
        new Server(serverPort).start();

    }

}
