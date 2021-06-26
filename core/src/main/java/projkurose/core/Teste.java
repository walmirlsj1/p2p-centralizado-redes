package projkurose.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Teste {
    public static void main(String[] args) {
        try {
            System.out.println(InetAddress.getLocalHost().getHostName());
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
