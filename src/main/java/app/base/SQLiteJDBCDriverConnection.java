package app.base;

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

        System.out.println("******************** CREATE TABLE SHARED ********************");
        stmt.execute("CREATE TABLE IF NOT EXISTS SHARED (" +
                "ID INTEGER AUTO_INCREMENT, " +
                "TITLE VARCHAR(80), " +
                "SHARED_PATH VARCHAR(255), " +
                "SIZE_PATH INTEGER" +
                ")"
        );

        System.out.println("******************** CREATE TABLE DIRECTORY ********************");
        stmt.execute("" +
                "CREATE TABLE IF NOT EXISTS DIRECTORY (" +
                " ID INTEGER AUTO_INCREMENT, " +
                " TITLE VARCHAR(80), " +
                " SHARED_PATH VARCHAR(255), " +
                " SIZE_PATH INTEGER" +
                ")"
        );
//                " FOREIGN KEY (candy_num) REFERENCES all_candy\n" +
//                "    ON DELETE CASCADE " +

        System.out.println("******************** CREATE TABLE CLIENT ********************");
        stmt.execute("" +
                "CREATE TABLE IF NOT EXISTS CLIENT (" +
                " ID INTEGER AUTO_INCREMENT, " +
                " ADDRESS VARCHAR(21)" +
                ")"
        );

        System.out.println("******************** CREATE TABLE CLIENT_DIRECTORY ********************");
        stmt.execute("" +
                "CREATE TABLE IF NOT EXISTS CLIENT_DIRECTORY (" +
                " ID INTEGER AUTO_INCREMENT, " +
                " CLIENT_ID INTEGER NOT_NUL, " +
                " DIRECTORY_ID INTEGER NOT_NUL, " +
                " FOREIGN KEY (CLIENT_ID) REFERENCES CLIENT (ID)" +
                "    ON DELETE CASCADE, " +
                " FOREIGN KEY (DIRECTORY_ID) REFERENCES DIRECTORY (ID)" +
                "    ON DELETE CASCADE " +
                ")"
        );

        /**
         * +
         *                 " FOREIGN KEY (candy_num) REFERENCES all_candy\n" +
         *                 "    ON DELETE CASCADE" +
         */
        stmt.execute("CREATE TABLE IF NOT EXISTS CLIENT(USER_ID VARCHAR(255), LAST_UPDATE DATETIME)");

        stmt.execute("DELETE FROM SHARED"); //LIMPA ANTERIOR PARA FINS DE TESTE

        String sqlA0 ="INSERT INTO CLIENT(ID, ADDRESS) VALUES (1, 'CLIENTE-PC'), (2, 'GHOST-PC');";
        String sqlA1 = "INSERT INTO DIRECTORY( ID, TITLE, SHARED_PATH, SIZE_PATH ) VALUES " +
                "(1, 'TESTE', 'DIRETORIO', 0), (2, 'TESTE2', 'DIRETORIO2', 0);";
        String sqlB = "INSERT INTO CLIENT_DIRECTORY (ID, CLIENT_ID, DIRECTORY_ID) VALUES (0,1,2)";
        String sqlB1 = "INSERT INTO CLIENT_DIRECTORY (ID, CLIENT_ID, DIRECTORY_ID) VALUES (0,2,2);";
        String sqlB2 = "INSERT INTO CLIENT_DIRECTORY (ID, CLIENT_ID, DIRECTORY_ID) VALUES (0,1,1);";
        stmt.execute(sqlA0);
        stmt.execute(sqlA1);
        stmt.execute(sqlB);
        stmt.execute(sqlB1);
        stmt.execute(sqlB2);

//        stmt.execute("" +
//                "INSERT INTO SHARED( ID, TITLE, SHARED_PATH, SIZE_PATH ) VALUES " +
//                "(1, 'TESTE', 'DIRETORIO', 0)," +
//                "(2, 'TESTE2', 'DIRETORIO2', 0)"
//        );
//        stmt.execute(

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
