/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.upm.fi.dia.oeg.geometry2rdf.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * MySQL implementation of DbConnector class.
 *
 * @author jonathangsc
 */
public class MySqlDbConnector implements DbConnector {

  private String host;
  private int port;
  private String dbName;
  private String username;
  private String password;
  private Connection connection;

  /*
   * Constructs a DbConnector Object.
   *
   * @param host - String with the IP where the database is hosted.
   * @param port - int with the port where the database is listening.
   * @param dbName - String with the name of the database.
   * @param username - String with the user name to access the database.
   * @param password - String with the password to access the database.
   */
  public MySqlDbConnector(String host, int port, String dbName,
                          String username, String password) {
    super();
    this.host = host;
    this.port = port;
    this.dbName = dbName;
    this.username = username;
    this.password = password;
  }

  @Override
  public String getDatabaseUrl() {
    return DbConstants.BASE_URL[DbConstants.MYSQL] + "//" + host + ":"
           + port + "/" + dbName;
  }

  @Override
  public Set<String> getUserEntities(DatabaseMetaData databaseMetadata)
         throws SQLException {
    ResultSet resultSet = databaseMetadata.getTables(
            null, databaseMetadata.getUserName(), "%", DbConstants.TABLE_TYPES);
    HashSet<String> userEntitiesSet = new HashSet<String>();
    while (resultSet.next()) {
      userEntitiesSet.add(resultSet.getString(DbConstants.TABLE_NAME));
    }
    return userEntitiesSet;
  }

  @Override
  public ResultSet executeQuery(String query) {
    ResultSet resultSet = null;
    try {
      Statement stmt = connection.createStatement();

      resultSet = stmt.executeQuery(query);

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return resultSet;
  }

  @Override
  public void dispose() {
    try {
      connection.close();
      connection = null;
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public String getTable(String fromItem) {
    return fromItem.substring(0, fromItem.indexOf(DbConstants.SEPARATOR));
  }

  /**
   * Returns a connection to the Database.
   *
   * @return connection to the database.
   */
  private Connection getConnection() {
    Connection connectionResult = null;
    try {
      Class.forName(DbConstants.DRIVERS[DbConstants.POSTGRESQL]);
      connectionResult = DriverManager.getConnection(
              getDatabaseUrl(), username, password);
    } catch (Exception ex) {
      //throw new SQLException ();
      ex.printStackTrace();
    }
    return connectionResult;
  }

}
