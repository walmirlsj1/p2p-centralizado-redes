package projkurose.peer;

import org.apache.commons.configuration2.PropertiesConfiguration;

import projkurose.core.Config;
import projkurose.core.SQLiteJDBCDriverConnection;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    private static void start() throws SQLException, IOException {
        System.out.println("******************** VERIFICANDO CONFIG ********************");
        String serverIpDir;
        int clientSrvPort, serverPortDir;
        Long clientId;
        String database = "./database.db";

        try {
            PropertiesConfiguration config = Config.getConfiguracao();
            clientSrvPort = config.getInt("client_port"); //6789
            clientId = config.getLong("client_id");
            database = Config.getConfiguracao().getString("client_database");
            serverIpDir = config.getString("server_ip");
            serverPortDir = config.getInt("server_port");
        } catch (Exception e) {
            /* @FIXME pode ficar em loop infinito? melhorar */
            Config.deleteConfig();
            // throw new RuntimeException("Falha na configuração, tente novamente!");
            start();
            return;
        }

        System.out.println("******************** VERIFICANDO DATABASE ********************");

        SQLiteJDBCDriverConnection.setDatabase(database);

        SQLiteJDBCDriverConnection.executeSQL("CREATE TABLE IF NOT EXISTS SHARED (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE VARCHAR(80), " +
                " SHARED_PATH VARCHAR(255), SIZE_PATH INTEGER, HASH_CODE INTEGER );");

        SQLiteJDBCDriverConnection.executeSQL("CREATE TABLE IF NOT EXISTS" +
                " CLIENT(USER_ID VARCHAR(255), LAST_UPDATE DATETIME);");


        /**
         * @FIXME Falhas não corrigidas
         * Falha porta obtida da configuração se é maior que 1024.
         * Falha por porta em uso.
         *
         */

        Thread peerServer = new Thread(new PeerServer(clientSrvPort, 20));
        peerServer.start();

        ConsoleGUI cli = new ConsoleGUI(clientId, clientSrvPort, serverIpDir, serverPortDir);
        cli.run();
        cli.disconnect();



    }

    public static void main(String[] args) throws SQLException, IOException {

        start();

        System.exit(0);
    }

}
