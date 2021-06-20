package app.base;

import app.FileManager;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SQLiteJDBCDriverConnection {
    public Connection con = null;
    public Statement stm = null;

    public static Connection getConnection() {
        String database = "./database.db";

        checkDbPath(database);

        try {
//            Class.forName("org.xerial.sqlite-jdbc");
            return DriverManager.getConnection("jdbc:sqlite:" + database);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean checkDbPath(String database) {

        File file = new File(database);
        if (file.exists()) return true;

        try {
            return file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) throws Throwable {
        Connection con = SQLiteJDBCDriverConnection.getConnection();
        Statement stmt = con.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS SHARED(ID INTEGER AUTO_INCREMENT, TITLE VARCHAR(80), SHARED_PATH VARCHAR(255), SIZE_PATH INTEGER)");
        stmt.execute("" +
                "CREATE TABLE Client \n" +
                "   (candy_num INT, \n" +
                "    candy_flavor CHAR(20),\n" +
                "    FOREIGN KEY (candy_num) REFERENCES all_candy\n" +
                "    ON DELETE CASCADE" +
                "")
        stmt.execute("CREATE TABLE IF NOT EXISTS CLIENT(USER_ID VARCHAR(255), LAST_UPDATE DATETIME)");

        stmt.execute("DELETE FROM SHARED"); //LIMPA ANTERIOR PARA FINS DE TESTE

        stmt.execute("" +
                "INSERT INTO SHARED( ID, TITLE, SHARED_PATH, SIZE_PATH ) VALUES " +
                "(1, 'TESTE', 'DIRETORIO', 0)," +
                "(2, 'TESTE2', 'DIRETORIO2', 0)"
        );


        System.out.println("--------------------- Select ---------------------");
        PreparedStatement query = con.prepareStatement("select * from SHARED");
        ResultSet resultSet = query.executeQuery();
        while (resultSet.next()) {
            Integer id = resultSet.getInt("ID");
            String title = resultSet.getString("TITLE");
            String path = resultSet.getString("SHARED_PATH");
            String size = resultSet.getString("SIZE_PATH");
            System.out.println(id + " - " + title + " - " + path);
        }
        //Long id, String title, String path, boolean isFolder
        //		con.finalize();
    }
}
