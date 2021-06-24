package app.client.model;

import app.base.SQLiteJDBCDriverConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SharedDAO {
    Connection con;
    private boolean debug = false;

    public SharedDAO() {
        this.con = SQLiteJDBCDriverConnection.getConnection();
    }

    public Shared insert(Shared shared) {
        try {
            Statement stmt = con.createStatement();
            String sql_insert = String.format(
                    "INSERT INTO SHARED(TITLE, SHARED_PATH, SIZE_PATH) VALUES ('%s', '%s', %d)",
                    shared.getTitle(), shared.getPath(), shared.getSize()
            );

            stmt.execute(sql_insert);
            shared.setId(this.getLastId());

            System.out.println(shared.toString());
            return shared;
        } catch (SQLException e) {
            System.out.println("Error insert: " + e.getMessage());
            return null;
        }
    }

    private Long getLastId() throws SQLException {
        Statement stmt = con.createStatement();

        ResultSet resultSet = stmt.getGeneratedKeys();

        return resultSet.getLong("last_insert_rowid()");
    }

    public Shared findById(long id) throws SQLException {
        String querySql = String.format("select * from SHARED WHERE ID=%d", id);

        PreparedStatement query = con.prepareStatement(querySql);
        ResultSet resultSet = query.executeQuery();

        if (resultSet.next())
            return resultSetToShared(resultSet);

        return null;
    }

    public boolean delete(Long id) {
        try {
            Statement stmt = con.createStatement();
            return stmt.execute("DELETE FROM SHARED WHERE ID=" + id);
        } catch (SQLException e) {
            System.out.println("Error delete: " + e.getMessage());
        }
        return false;
    }

    public void deleteAll() throws SQLException {
        if (this.debug) return;
        Statement stmt = con.createStatement();
        stmt.execute("DELETE FROM SHARED");
    }

    public void update(Shared shared) throws Exception {
        throw new Exception("Não implementado");
    }

    public List<Shared> findAll() throws SQLException {
        PreparedStatement query = con.prepareStatement("select * from SHARED");
        ResultSet resultSet = query.executeQuery();

        return convertListShared(resultSet);
    }

    private List<Shared> convertListShared(ResultSet resultSet) throws SQLException {
        ArrayList<Shared> list = new ArrayList<>();
        Shared shared;

        while (resultSet.next()) {
            shared = resultSetToShared(resultSet);
            list.add(shared);
        }
        return list;
    }

    private Shared resultSetToShared(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        String title = resultSet.getString("TITLE");
        String path = resultSet.getString("SHARED_PATH");
        Long size = resultSet.getLong("SIZE_PATH");

        return new Shared(id, title, path, size);
    }

}
