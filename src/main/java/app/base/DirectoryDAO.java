package app.base;

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
                "INSERT INTO DIRECTORY(ID, TITLE, SIZE_PATH) VALUES (%d, '%s', %d)",
                directory.getId(), directory.getTitle(), directory.getSize()
        );
        Statement stmt;
        try {
            stmt = con.createStatement();

            stmt.execute(sql_insert);
            directory.setId(this.getLastId());

            System.out.println(directory.toString());
            return directory;
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

    public Directory findById(long id) throws SQLException {
        String querySql = String.format("select * from DIRECTORY WHERE ID=%d", id);

        PreparedStatement query = con.prepareStatement(querySql);
        ResultSet resultSet = query.executeQuery();

        if (resultSet.first())
            return resultSetToDirectory(resultSet);

        return null;
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

    public List<Directory> findAll() throws SQLException {
        PreparedStatement query = con.prepareStatement("select * from DIRECTORY");
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

}
