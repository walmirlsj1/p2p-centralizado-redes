package app.server;

import app.config.Config;

import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private int port;
    private String databaseName = null;
    private String localPort = null;

    protected ExecutorService threadPool =
            Executors.newFixedThreadPool(20);

    public ServidorDiretorio(int port) {
        this.port = port;
        this.carregaConfig();
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

    public void start() {
//        int port = 6543;
        ServerSocket socketServidor = null;
        try {
            socketServidor = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException(String.format("start: Falha ao criar socket na porta: %d ", this.port));
        }
        System.out.println(" ::::::: PORT: " + this.port + " ::::::: ");

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
        int portServidor = 6543;

        new ServidorDiretorio(portServidor).start();

    }

}
