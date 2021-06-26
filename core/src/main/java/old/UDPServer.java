package old;
import java.net.*;

class UDPServer {
    /**
     * UDP utiliza DatagramSocket
     *
     * O UDP não tem conexão e envia pacotes de dados independentes
     * de um sistema final para outro, sem nenhuma garantia de entrega.
     *
     */

    public static void main(String args[]) throws Exception
    {

        DatagramSocket serverSocket = new DatagramSocket(9876);

        byte[ ] receiveData = new byte[1024];
        byte[ ] sendData  = new byte[1024];

        while(true)
        {

            DatagramPacket receivePacket =
                    new DatagramPacket(receiveData, receiveData.length);

            serverSocket.receive(receivePacket);

            String sentence = new String(receivePacket.getData());

            InetAddress IPAddress = receivePacket.getAddress();

            int port = receivePacket.getPort();

            String capitalizedSentence = sentence.toUpperCase();

            sendData = capitalizedSentence.getBytes();

            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress,
                            port);

            System.out.println("Data: " + capitalizedSentence.trim() +
                            " Tamanho: " + sendData.length +
                            " IPAddress: " + IPAddress +
                            " Port: " + port);
            serverSocket.send(sendPacket);
        }
    }
}