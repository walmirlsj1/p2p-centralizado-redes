package app.server.model;

import app.base.SQLiteJDBCDriverConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DirectoryDAO {
    Connection con;
    private boolean debug = false;

    public DirectoryDAO() {
        this.con = SQLiteJDBCDriverConnection.getConnection();
    }

    public Directory insert(Directory directory) {
        String sql_insert = String.format(
                "INSERT INTO DIRECTORY(TITLE, SIZE_PATH) VALUES ('%s', %d)",
                directory.getTitle(), directory.getSize()
        );
        Statement stmt;
        try {
            stmt = con.createStatement();

            stmt.execute(sql_insert);
            directory.setId(this.getLastId());

            System.out.println(directory.toString());
            return directory;
        } catch (SQLException e) {
            System.out.println("Error insert: " + e.getMessage());
        }
        return null;
    }

    private Long getLastId() throws SQLException {
        Statement stmt = con.createStatement();

        ResultSet resultSet = stmt.getGeneratedKeys();

        return resultSet.getLong("last_insert_rowid()");
    }

    public boolean delete(int id) throws SQLException {
        Statement stmt = con.createStatement();
        return stmt.execute("DELETE FROM DIRECTORY WHERE ID=" + id);
    }

    public void deleteAll() throws SQLException {
        if (this.debug) return;
        Statement stmt = con.createStatement();
        stmt.execute("DELETE FROM DIRECTORY");
    }

    public void update(Directory directory) throws Exception {
        throw new Exception("Não implementado");
    }

    public Directory findById(long id) {
        String querySql = String.format("SELECT * FROM DIRECTORY WHERE ID=%d", id);
        try {
            PreparedStatement query = con.prepareStatement(querySql);
            ResultSet resultSet = query.executeQuery();

            if (resultSet.next())
                return resultSetToDirectory(resultSet);
        } catch (SQLException e) {
            System.out.println("Error findById: " + e.getMessage());
        }
        return null;
    }

    public Directory findByTitle(String title) {

        String sql = String.format("SELECT * FROM DIRECTORY WHERE TITLE='%s'", title);
        try {
            PreparedStatement query = con.prepareStatement(sql);
            ResultSet resultSet = query.executeQuery();

            if (resultSet.next())
                return resultSetToDirectory(resultSet);
        } catch (SQLException e) {
            System.out.println("Error findByTitle: " + e.getMessage());
        }
        return null;
    }

    public List<Directory> findAll() throws SQLException {
        return find("SELECT * FROM DIRECTORY");
    }

    public List<Directory> findAllContainsTitle(String title) {
        try {
            String sql = String.format("SELECT * FROM DIRECTORY WHERE TITLE LIKE '%%%s%%'", title);
            return find(sql);
        } catch (SQLException e) {
            System.out.println("Error findAllContainsTitle: " + e.getMessage());
        }
        return null;
    }

    private List<Directory> find(String sql) throws SQLException {
        PreparedStatement query = con.prepareStatement(sql);
        ResultSet resultSet = query.executeQuery();

        return convertListDirectory(resultSet);
    }

    private List<Directory> convertListDirectory(ResultSet resultSet) throws SQLException {
        ArrayList<Directory> list = new ArrayList<>();
        Directory directory;

        while (resultSet.next()) {
            directory = resultSetToDirectory(resultSet);
            list.add(directory);
        }
        return list;
    }

    private Directory resultSetToDirectory(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        String title = resultSet.getString("TITLE");
        Long size = resultSet.getLong("SIZE_PATH");

        return new Directory(id, title, size);
    }

    public Boolean insertClientDirectory(Directory directory, Client client) {
        String sql_insert = String.format(
                "INSERT INTO CLIENT_DIRECTORY(CLIENT_ID, DIRECTORY_ID) VALUES ('%d', %d);",
                client.getId(), directory.getId()
        );
        Statement stmt;
        try {
            stmt = con.createStatement();

            stmt.execute(sql_insert);
            directory.setId(this.getLastId());

            System.out.println(directory.toString());
            return true;
        } catch (SQLException e) {
            System.out.println("Error insert: " + e.getMessage());
        }
        return false;
    }

    public List<Client> findAllClientsByDirectory(Directory dir) {
        ClientDAO clientDAO = new ClientDAO();
        String sql = "SELECT C.* FROM CLIENT_DIRECTORY AS F, CLIENT AS C, DIRECTORY AS D " +
                "WHERE F.CLIENT_ID=C.ID AND F.DIRECTORY_ID=D.ID AND D.ID=%d;";
        sql = String.format(sql, dir.getId());
        try {
            PreparedStatement query = con.prepareStatement(sql);
            ResultSet resultSet = query.executeQuery();

            return clientDAO.convertListClient(resultSet);
        } catch (SQLException e) {
            System.out.println("Error findAllClientsByDirectory: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<Directory> getAllDirectoryByClient(Client client) {

        String sql = "SELECT D.* FROM CLIENT_DIRECTORY AS F, CLIENT AS C, DIRECTORY AS D " +
                "WHERE F.CLIENT_ID=C.ID AND F.DIRECTORY_ID=D.ID AND C.ID=%d;";
        sql = String.format(sql, client.getId());

        try {
            PreparedStatement query = con.prepareStatement(sql);
            ResultSet resultSet = query.executeQuery();

            return convertListDirectory(resultSet);
        } catch (SQLException e) {
            System.out.println("Error getAllDirectoryByClient: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public Boolean deleteAllDirectoryByClient(Client client) {
        String sql = "DELETE FROM CLIENT_DIRECTORY AS CD WHERE CD.CLIENT_ID=%d;";
        sql = String.format(sql, client.getId());

        try {
            PreparedStatement query = con.prepareStatement(sql);
            ResultSet resultSet = query.executeQuery();

            return true;
        } catch (SQLException e) {
            System.out.println("Error deleteAllDirectoryByClient: " + e.getMessage());
        }
        return false;
    }
}
