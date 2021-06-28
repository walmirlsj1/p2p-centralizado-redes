package projkurose.server;

import projkurose.core.Config;
import projkurose.core.SQLiteJDBCDriverConnection;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    private static void start() throws SQLException {
        System.out.println("******************** VERIFICANDO CONFIG ********************");

        String database = "./databaseSrv.db";
        int port = 7000;
        try {
            port = Config.getConfiguracao().getInt("srv_port");
            database = Config.getConfiguracao().getString("srv_database");
        } catch (Exception e) {
            /* @FIXME pode ficar em loop infinito? melhorar */
            Config.deleteConfig();
            // throw new RuntimeException("Falha na configuração, tente novamente!");
            start();
            return;
        }

        System.out.println("******************** VERIFICANDO DATABASE ********************");

        SQLiteJDBCDriverConnection.setDatabase(database);

        SQLiteJDBCDriverConnection.executeSQL("CREATE TABLE IF NOT EXISTS DIRECTORY (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE VARCHAR(80), " +
                " SHARED_PATH VARCHAR(255), SIZE_PATH INTEGER );");

        SQLiteJDBCDriverConnection.executeSQL("CREATE TABLE IF NOT EXISTS CLIENT (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT, ADDRESS VARCHAR(21) );");

        SQLiteJDBCDriverConnection.executeSQL("CREATE TABLE IF NOT EXISTS CLIENT_DIRECTORY (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT, CLIENT_ID INTEGER NOT_NUL, " +
                " DIRECTORY_ID INTEGER NOT_NUL, " +
                " FOREIGN KEY (CLIENT_ID) REFERENCES CLIENT (ID)" +
                "    ON DELETE CASCADE, " +
                " FOREIGN KEY (DIRECTORY_ID) REFERENCES DIRECTORY (ID)" +
                "    ON DELETE CASCADE " +
                ");");

        System.out.println(" ::::::: Servidor DIRETORIOS ::::::: ");

        new ServerDirectory(port, 20).start(); // não é um thread
    }

    public static void main(String[] args) throws SQLException {

        start();
        System.exit(0);
    }
}
