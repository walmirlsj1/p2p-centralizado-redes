package app.base;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class clientDAO {
    Connection con;
    private boolean debug = false;

    public clientDAO() {
        this.con = SQLiteJDBCDriverConnection.getConnection();
    }

    public Client insert(Client client) {
        String sql_insert = String.format(
                "INSERT INTO CLIENT(ID, ADDRESS) VALUES (%d, '%s')",
                client.getId(), client.getAddress()
        );

        try {
            Statement stmt = con.createStatement();

            stmt.execute(sql_insert);
            client.setId(this.getLastId());

            System.out.println(client.toString());
            return client;
        } catch (SQLException e) {
            System.out.println("Method insert error: " + e.getMessage());
            return null;
        }

    }

    private Long getLastId() throws SQLException {
        Statement stmt = con.createStatement();

        ResultSet resultSet = stmt.getGeneratedKeys();

        return resultSet.getLong("last_insert_rowid()");
    }


    public boolean delete(int id) throws SQLException {
        Statement stmt = con.createStatement();
        return stmt.execute("DELETE FROM CLIENT WHERE ID=" + id);
    }

    public List<Client> findAll() throws SQLException {
        PreparedStatement query = con.prepareStatement("select * from CLIENT");
        ResultSet resultSet = query.executeQuery();

        return convertListClient(resultSet);
    }

    private List<Client> convertListClient(ResultSet resultSet) throws SQLException {
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

}
