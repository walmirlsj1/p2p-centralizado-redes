package app;

import app.base.SharedDAO;
import app.config.Config;
import app.base.Shared;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Ideias
 * enviar arquivo
 * para continuar download usar indice de ultimo pacote recebido
 * quando iniciar download enviar indice 0
 * para continuar download enviar indece do ultimo pacote recebido
 * salvar progresso do donwload, para que não corromper o arquivo.!!
 */

public class TCPClienteServidor {
    private Runnable threadSrv;
    private HashMap<Shared, String> shareList;
    private Integer portClientSrv, portServDir;
    private String databaseName;
    private String localPort;

    public TCPClienteServidor(int portClientSrv) throws IOException {
        this.carregaConfig();
        this.shareList = new HashMap<>();

        this.portServDir = 6543;
        this.portClientSrv = portClientSrv; //6789

        this.startThreadSrv();
        this.startClient();

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

    private void startThreadSrv() {
        threadSrv = () -> {
            try {
                System.out.println("Iniciando Servidor Local porta: " + portClientSrv);
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

        ServerSocket server = new ServerSocket(this.portClientSrv);

        while (true) {
            /**
             * Da forma que está atende um cliente por vez
             * seria interessante ter um spool de thread:? e tratar cliente em thread separadas
             * ex. 16 clientes de uma vez
             *
             */
            System.out.println("Aguardando clientes");

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

                respP2P = this.shareList.get(infoSrvDir[1]);

            }
            capitalizedSentence = respP2P + '\n';

            System.out.println("Data: " + capitalizedSentence.trim() +
                    " Tamanho: " + capitalizedSentence.length());

            outToClient.writeBytes(capitalizedSentence);

            cliente.close();
        }
    }

    private void startClient() throws IOException {
        System.out.println("Iniciando Cliente");
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Informe OP: GET/FIND/REG");
            String op = inFromUser.readLine();

            System.out.println("Informe um titulo para pesquisa");
            String title = inFromUser.readLine();

            String opProcessada = this.processar(op, title);

            String respSrvDir = this.conectar(opProcessada, "localhost", 6543);

//            System.out.println("Resposta ServidorDir" + resposta);

            String[] infoSrvDir = respSrvDir.trim().split(";");

            System.out.println("Resposta ServidorDir OP: " + opProcessada + " resposta: " + respSrvDir);

            if (infoSrvDir[0].equals("GET") && !infoSrvDir[2].equals("null")) {
                String[] hostP2P = infoSrvDir[2].split(":");

                String opP2P = "GET;" + title + ";";
                try {
                    String respP2P = this.conectar(opP2P, hostP2P[0], Integer.parseInt(hostP2P[1]));

                    System.out.println("Solicitei P2P OP: " + opP2P + " resposta: " + respP2P + " end: " + infoSrvDir[2]);
                } catch (Exception e) {
                    System.out.println("Ocorreu um erro ao solicitar dados do client-serv: " + opP2P + " IP: " + hostP2P[0] + " PORTA: " + Integer.parseInt(hostP2P[1]));
                }
            } else if (infoSrvDir[0].equals("GET") && infoSrvDir[2].equals("null")) {
                System.out.println("***************\nPesquisa não encontrada no servidor de diretorios!\n***************");
            }

        }
    }

    private String processar(String operacao, String key) {
//        "GET;" + msg + ";" + "127.0.0.1:6789"
        String temp = null;
        if (operacao.equals("FIND")) {
            temp = "FIND;" + key + ";" + "127.0.0.1:" + this.portServDir;
        } else if (operacao.equals("REG")) {
            temp = "REG;" + key + ";" + "127.0.0.1:" + this.portClientSrv;
            Shared shared = this.registrarCompartilhamento();
            this.shareList.put(shared, key);//"diretorioXYZ" + (new Random().nextInt() * 10)
        } else if (operacao.equals("GET")) {
            temp = "GET;" + key + ";" + "127.0.0.1:" + this.portClientSrv;
        }

        return temp;
    }

    private Shared registrarCompartilhamento() {
        BufferedReader in =
                new BufferedReader(new InputStreamReader(System.in));

        try {
            String title = in.readLine();
            String diretorio = in.readLine();

            Long size = FileManager.getSizeFolder(diretorio);

            Shared shared = new Shared(title, diretorio, size);
            SharedDAO dao = new SharedDAO();

            return dao.insert(shared);

        } catch (IOException e) {
            System.out.println("Error I/O");
            return null;
        }
    }

    private String conectar(String msg, String hostname, Integer port) throws IOException {
        String sentence;
        String respSrv;

//        BufferedReader inFromUser =
//                new BufferedReader(new InputStreamReader(System.in));

        Socket clientSocket = new Socket(hostname, port);//6543

        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        DataInputStream inFromServer =
                new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
//        BufferedReader inFromServer =
//                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String textoEnviado = "Ola mundo";

        outToServer.writeChar('s');                 // operacao
        outToServer.writeInt(0);                    // indice
        outToServer.writeInt(textoEnviado.length());   // length
        outToServer.writeBytes(textoEnviado);          // data

        char type = inFromServer.readChar();
        int length = inFromServer.readInt();
        byte[] retornoSRV = inFromServer.readNBytes(length);
//        StringBuilder dataString = new StringBuilder(length);
//        StringBuilder resposta = dataString.append(retornoSRV);
        String resposta = new String(retornoSRV);
        System.out.println("resposta do servidor: " + resposta);
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        outToServer.;
//        sentence = inFromUser.readLine();

//        outToServer.writeBytes(msg + '\n');

//        respSrv = inFromServer.readLine();

//        System.out.println("FROM SERVER: " + modifiedSentence);

        clientSocket.close();
//        return respSrv;
        return "";
    }

    public ByteArrayOutputStream teste(String s) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(1);
        dos.writeChars(s); //Diretório a ser listado
        return bos;
    }

    public static void main(String[] args) throws IOException {
        new TCPClienteServidor(6789);
    }
}
