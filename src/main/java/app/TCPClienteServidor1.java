package app;

import app.client.TCPClienteServidor;

import java.io.IOException;

public class TCPClienteServidor1 {
    public static void main(String[] args) throws Exception {
        TCPClienteServidor client = new TCPClienteServidor(6799);
        client.run();
        client.disconnect();

    }
}
