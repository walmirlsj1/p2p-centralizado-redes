package projkurose.core;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SQLiteJDBCDriverConnection {
    public static Connection con = null;
    public static Statement stm = null;

    public static void geraDB() throws SQLException {
        Connection con = getConnection();
        Statement stmt = con.createStatement();

        System.out.println("******************** CREATE TABLE SHARED ********************");
        stmt.execute("CREATE TABLE IF NOT EXISTS SHARED (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " TITLE VARCHAR(80), " +
                " SHARED_PATH VARCHAR(255), " +
                " SIZE_PATH INTEGER" +
                ")"
        );
    }

    public static Connection getConnection() {
        try {
            if (con != null && !con.isClosed()) {
                return con;
//                con.close();
            }
            String database = "./database.db";

            checkDbPath(database);


//            Class.forName("org.xerial.sqlite-jdbc");
            con = DriverManager.getConnection("jdbc:sqlite:" + database);
            return con;
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

    }
}
