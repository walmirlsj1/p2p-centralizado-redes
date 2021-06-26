package projkurose.server;

import projkurose.core.Config;
import projkurose.core.SQLiteJDBCDriverConnection;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws IOException, SQLException {
        System.out.println(" ::::::: Servidor DIRETORIOS ::::::: ");
        int port = 7000;
        try {
            port = Config.getConfiguracao().getInt("srv_port");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Config.deleteConfig();
            throw new RuntimeException("Falha na configuração, tente novamente!");
        }
        String sql =
                "CREATE TABLE IF NOT EXISTS DIRECTORY (" +
                        " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        " TITLE VARCHAR(80), " +
                        " SHARED_PATH VARCHAR(255), " +
                        " SIZE_PATH INTEGER" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS CLIENT (" +
                        " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        " ADDRESS VARCHAR(21)" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS CLIENT_DIRECTORY (" +
                        " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        " CLIENT_ID INTEGER NOT_NUL, " +
                        " DIRECTORY_ID INTEGER NOT_NUL, " +
                        " FOREIGN KEY (CLIENT_ID) REFERENCES CLIENT (ID)" +
                        "    ON DELETE CASCADE, " +
                        " FOREIGN KEY (DIRECTORY_ID) REFERENCES DIRECTORY (ID)" +
                        "    ON DELETE CASCADE " +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS CLIENT(USER_ID VARCHAR(255), LAST_UPDATE DATETIME);";

        SQLiteJDBCDriverConnection.database = "./databaseSrv.db";
        SQLiteJDBCDriverConnection.checkDatabase(sql);

        new ServerDirectory(port, 20).start(); // não é um thread

    }
}
