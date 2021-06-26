package old;

import java.io.*;
import java.net.*;

public class TCPServidor {
    /**
     * TCP utiliza socket
     * TCP é orientado à conexão e fornece um canal de fluxo de bytes confiável
     * através do qual os dados fluem entre dois sistemas terminais.
     */
    public static void main(String argv[]) throws Exception {
        String clientSentence;
        String capitalizedSentence;

        ServerSocket welcomeSocket = new ServerSocket(6789);

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
