package projkurose.core;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SQLiteJDBCDriverConnection {
    private static Connection con = null;
    //    public static Statement stm = null;
    private static String database = "./database.db";

    public static void setDatabase(String db) {
        database = db;
        checkDbPath();
    }

    public static void executeSQL(String sql) throws SQLException {
        Statement stmt = getConnection().createStatement();
        stmt.execute(sql);
        con.close();
    }

    public static Connection getConnection() {
        try {
            if (con != null && !con.isClosed()) {
                return con;
            }
//            Class.forName("org.xerial.sqlite-jdbc");
            con = DriverManager.getConnection("jdbc:sqlite:" + database);
            return con;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeDatabase() throws SQLException {
        if (con != null && !con.isClosed()) {
            con.close();
        }
    }

    private static boolean checkDbPath() {

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
