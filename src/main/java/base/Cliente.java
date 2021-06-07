package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Cliente {

    public static void main(String[] args) throws IOException {


        //333-372 java
        Socket chatSocket = new Socket("127.0.0.1", 5000);
        PrintWriter writer = new PrintWriter(chatSocket.getOutputStream());
        writer.println("message to send");
        writer.print("another message");
    }

    public Cliente() throws IOException {





    }
}
