package old;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class TCPClient2 {
    private List<String> shareList;
//    private

    public void start() throws IOException {
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Informe OP: GET/FIND/REG");
            String op = inFromUser.readLine();

            System.out.println("Informe um titulo para pesquisa");
            String title = inFromUser.readLine();

            String opProcessada = this.processar(op, title);
            String respSrvDir;

            respSrvDir = this.conectar(opProcessada, "localhost", 6543);

//            System.out.println("Resposta ServidorDir" + resposta);

            String[] infoSrvDir = respSrvDir.trim().split(";");
            System.out.println("Resposta ServidorDir OP: " + opProcessada + " resposta: " + respSrvDir);
//            System.out.println(infoSrvDir[2] + infoSrvDir[2].equals("null"));
            if (infoSrvDir[0].equals("GET") && !infoSrvDir[2].equals("null")) {
                String[] hostP2P = infoSrvDir[2].split(":");

                String opP2P = "GET;" + title + ";";
                try {
                    String respP2P = this.conectar(opP2P, hostP2P[0], Integer.parseInt(hostP2P[1]));

                    System.out.println("Solicitei P2P OP: " + opP2P + " resposta: " + respP2P + " end: " + infoSrvDir[2]);
                } catch (Exception e) {
                    System.out.println("Ocorreu um erro ao solicitar dados do client-serv: " + opP2P + " IP: " + hostP2P[0] + " PORTA: " + Integer.parseInt(hostP2P[1]));
                }
            } else if(infoSrvDir[0].equals("GET") && infoSrvDir[2].equals("null")) {
                System.out.println("***************\nPesquisa não encontrada no servidor de diretorios!\n***************");
            }

        }
    }

    private String processar(String operacao, String key) {
//        "GET;" + msg + ";" + "127.0.0.1:6789"
        String temp = null;
        if (operacao.equals("FIND")) {
            temp = "FIND;" + key + ";" + "127.0.0.1:6543";
        } else if (operacao.equals("REG")) {
            temp = "REG;" + key + ";" + "127.0.0.1:6615";
        } else if (operacao.equals("GET")) {
            temp = "GET;" + key + ";" + "127.0.0.1:6615";
        }

        return temp;
    }

    private String conectar(String msg, String hostname, Integer port) throws IOException {
        String sentence;
        String respSrv;

//        BufferedReader inFromUser =
//                new BufferedReader(new InputStreamReader(System.in));

        Socket clientSocket = new Socket(hostname, port);//6543

        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer =
                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

//        sentence = inFromUser.readLine();

        outToServer.writeBytes(msg + '\n');

        respSrv = inFromServer.readLine();

//        System.out.println("FROM SERVER: " + modifiedSentence);

        clientSocket.close();
        return respSrv;
    }


    public static void main(String[] args) throws IOException {
        new TCPClient2().start();
    }
}
