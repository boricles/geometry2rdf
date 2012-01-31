/*
 * @(#) Constants.java	0.1	2010/08/05
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

public interface Constants {

  /**
   *
   *  @author boricles
   *
   */
  public static final int MSACCESS = 0;
  public static final int MYSQL = 1;
  public static final int ORACLE = 2;
  public static final int POSTGRESQL = 3;
  public static final String[] DRIVERS =
    {"sun.jdbc.odbc.JdbcOdbcDriver", "com.mysql.jdbc.Driver",
     "oracle.jdbc.driver.OracleDriver", "org.postgresql.Driver"};
  public static final String[] DBMS = {"MSACCESS", "MYSQL", "ORACLE", "POSTGRESQL"};
  public static final String[] BASEURL =
    {"jdbc:odbc:", "jdbc:mysql:", "jdbc:oracle:thin:", "jdbc:postgresql:"};
  public static final String[] LENPRIMITIVE = {"len", "length", "len"};
  public static final String SEPARATOR = ".";
}