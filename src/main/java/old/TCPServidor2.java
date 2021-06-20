package old;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServidor2 {
    /**
     * TCP utiliza socket
     * TCP é orientado à conexão e fornece um canal de fluxo de bytes confiável
     * através do qual os dados fluem entre dois sistemas terminais.
     */
    public static void main(String argv[]) throws Exception {
        String clientSentence;
        String capitalizedSentence;

        ServerSocket welcomeSocket = new ServerSocket(6615);

        while (true) {

            Socket connectionSocket = welcomeSocket.accept();

            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            DataOutputStream outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());

            clientSentence = inFromClient.readLine();

            capitalizedSentence = clientSentence.toUpperCase() + '\n';

            System.out.println("Data: " + capitalizedSentence.trim() +
                    " Tamanho: " + capitalizedSentence.length());

            outToClient.writeBytes(capitalizedSentence);
        }
    }
}
