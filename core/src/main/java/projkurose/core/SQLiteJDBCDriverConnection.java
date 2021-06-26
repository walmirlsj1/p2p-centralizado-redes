package projkurose.core;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SQLiteJDBCDriverConnection {
    public static Connection con = null;
    public static Statement stm = null;
    public static String database = "./database.db";

    public static void checkDatabase(String sql) throws SQLException {
        Statement stmt = getConnection().createStatement();
        stmt.execute(sql);
    }

    public static Connection getConnection() {
        try {
            if (con != null && !con.isClosed()) {
                return con;
//                con.close();
            }

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
