package app.server.model;

import app.base.SQLiteJDBCDriverConnection;
import app.base.SQLiteJSrv;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {
    Connection con;
    private boolean debug = false;

    public ClientDAO() {
        this.con = SQLiteJSrv.getConnection();
    }

    public Client insert(Client client) {
        String sql_insert = String.format(
                "INSERT INTO CLIENT(ADDRESS) VALUES ('%s')",
                client.getAddress()
        );

        try (Statement stmt = con.createStatement()) {

            stmt.execute(sql_insert);

            client.setId(this.getLastId());

            System.out.println(client.toString());
            return client;
        } catch (SQLException e) {
            System.out.println("Error insert: " + e.getMessage());
        }
        return null;
    }

    private Long getLastId() throws SQLException {
        try (Statement stmt = con.createStatement()) {

            ResultSet resultSet = stmt.getGeneratedKeys();
            return resultSet.getLong("last_insert_rowid()");
        } catch (SQLException e) {
            throw new SQLException("Falha ao recuperar ultimo id");
        }
    }


    public boolean delete(Long id) {
        boolean deleted = false;
        String sql = String.format("DELETE FROM CLIENT WHERE ID=%d", id);
        try (Statement stmt = con.createStatement()) {
            deleted = stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println("Error delete: " + e.getMessage());
        }
        return deleted;
    }

    public Client findById(Long id) {
        String sql = String.format("SELECT * FROM CLIENT WHERE ID=%d", id);

        try (PreparedStatement query = con.prepareStatement(sql)) {

            ResultSet resultSet = query.executeQuery();

            if (resultSet.next())
                return resultSetToClient(resultSet);
        } catch (SQLException e) {
            System.out.println("Error findById: " + e.getMessage());
        }
        return null;
    }

    public List<Client> findAll() throws SQLException {
        PreparedStatement query = con.prepareStatement("select * from CLIENT");
        ResultSet resultSet = query.executeQuery();

        return convertListClient(resultSet);
    }

    public List<Client> convertListClient(ResultSet resultSet) throws SQLException {
        ArrayList<Client> list = new ArrayList<>();
        Client client;

        while (resultSet.next()) {
            client = resultSetToClient(resultSet);
            list.add(client);
        }
        return list;
    }

    private Client resultSetToClient(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        String address = resultSet.getString("ADDRESS");

        return new Client(id, address);
    }

    public Client findByAddress(String address) {
        String sql = String.format("SELECT * FROM CLIENT WHERE ADDRESS='%s'", address);

        try (PreparedStatement query = con.prepareStatement(sql)) {
            ResultSet resultSet = query.executeQuery();

            if (resultSet.next())
                return resultSetToClient(resultSet);
        } catch (SQLException e) {
            System.out.println("Error findByAddress: " + e.getMessage());
        }
        return null;
    }
}
