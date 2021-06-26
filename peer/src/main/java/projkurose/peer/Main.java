package projkurose.peer;

import org.apache.commons.configuration2.PropertiesConfiguration;
import projkurose.core.Config;
import projkurose.core.SQLiteJDBCDriverConnection;

public class Main {

    public static void main(String[] args) throws Exception {

        String serverIpDir;
        int clientSrvPort, serverPortDir;
        Long clientId;

        try {
            PropertiesConfiguration config = Config.getConfiguracao();
            clientSrvPort = config.getInt("client_port"); //6789
            clientId = config.getLong("client_id");

            serverIpDir = config.getString("server_ip");
            serverPortDir = config.getInt("server_port");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Config.deleteConfig();
            throw new RuntimeException("Falha na configuração, tente novamente!");
        }

        String sql = "CREATE TABLE IF NOT EXISTS SHARED (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " TITLE VARCHAR(80), " +
                " SHARED_PATH VARCHAR(255), " +
                " SIZE_PATH INTEGER" +
                ")";
        System.out.println("******************** VERIFICANDO DATABASE ********************");

        SQLiteJDBCDriverConnection.database = "./database.db";
        SQLiteJDBCDriverConnection.checkDatabase(sql);

        new Thread(new PeerServer(clientSrvPort, 20)).start();

        Peer cli = new Peer(clientId, clientSrvPort, serverIpDir, serverPortDir);
        cli.run();
        cli.disconnect();
    }

}
