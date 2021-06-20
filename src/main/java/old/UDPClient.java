package old;

import java.io.*;
import java.net.*;

class UDPClient {
    public static void main(String args[]) throws Exception {

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        DatagramSocket clientSocket = new DatagramSocket();

        InetAddress IPAddress = InetAddress.getByName("localhost"); //hostname

        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        while (true) {// modificacao
            System.out.println("Escreva a msg para enviar ao servidor: \\exit para sair");
            String sentence = inFromUser.readLine(); //String alterado

            if(sentence.equals("\\exit")) break;

            sendData = sentence.getBytes();

            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress, 9876);

            clientSocket.send(sendPacket);

            DatagramPacket receivePacket =
                    new DatagramPacket(receiveData, receiveData.length);

            clientSocket.receive(receivePacket);

            String modifiedSentence =
                    new String(receivePacket.getData());

            System.out.println("FROM SERVER:" + modifiedSentence.trim());

        }
        clientSocket.close();

    }
}
