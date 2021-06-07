package base;

import java.io.*;
import java.net.*;

public class ServidorDiretorio {
    public static void main(String[] args) throws IOException {
        Socket chatSocket = new Socket("127.0.0.1", 5000);
        InputStreamReader stream = new InputStreamReader(chatSocket.getInputStream());

        BufferedReader reader = new BufferedReader(stream);

        String message = reader.readLine();
    }
    public ServidorDiretorio() throws IOException {



    }
}
