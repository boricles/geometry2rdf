/*
 * @(#) Connector.java	0.1	2010/08/05
 *
 * Copyright (C) 2010 vsaquicela,boricles
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package es.upm.fi.dia.oeg.geometry2rdf.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * Interface that defines all the methods to be implemented by any kind
 * of Database connector.
 *
 * @author Jonathan Gonzalez (jonathan@jonbaraq.eu)
 * @version 2nd Feb 2012.
 */
public interface DbConnector {

  /*
   * Returns the Database URL.
   *
   * @return databaseUrl with the URL of the database.
   */
  public String getDatabaseUrl();

  /**
   * Returns the user entities of the DatabaseMetadata.
   *
   * @return set of strings with the use entities.
   * @throws SQL Exception.
   */
  public Set<String> getUserEntities(DatabaseMetaData databaseMetadata)
         throws SQLException;

  /**
   * Returns the result of the query executed against the database.
   *
   * @param query - String with the query.
   * @return resultset with the result of the query.
   */
  public ResultSet executeQuery(String query);

  /**
   * Returns the table name given a string Item.
   *
   * @param fromItem
   * @return tableName
   */
  public String getTable(String fromItem);

  /**
   * Closes database connection.
   */
  public void dispose();

}
