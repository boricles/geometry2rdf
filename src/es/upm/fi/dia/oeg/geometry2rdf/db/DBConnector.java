/*
 * @(#) DBConnector.java	0.1	2010/08/05
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;


/**
*
*  @author boricles
* 
*/


public class DBConnector extends Connector implements Constants {

	protected String username;
    protected String password;
    protected String driver;
    protected String url;
    protected String dbms;
    protected String host;
    protected String port;
    protected String dbname;

    protected int type;

    protected Connection connection;

    protected DatabaseMetaData md;

    protected Statement stmt;

    public DBConnector() {
    	super();
    }

    public DBConnector(int type, String dbname, String username, String password, String host, String port) {
    	super();
    	connection = getConnection(type,dbname, username,password,host,port);
    }

    protected Connection getConnection(int type,String dbname, String username, String password, String host, String port) {
    	Connection con = null;
    	this.username = username;
    	this.password = password;
    	this.dbname = dbname;
    	this.host = host;
    	this.port = port;
    	this.dbms = DBMS[type];
    	this.driver = DRIVERS[type];
    	this.url = getDatabaseURL(type);
    	this.type = type;
    	try {
    		Class.forName(driver);
    		con = DriverManager.getConnection(url, username, password);
    	}
    	catch (Exception ex) {
    		//throw new SQLException ();
    		ex.printStackTrace();
    	}
    	return con;
    }

    protected String getDatabaseURL(int type) {
    	String url="";
    	if (type == MSACCESS) {
    		url += BASEURL[type] + dbname + ";" + "UID=" + username + ";" + "PWD=" +  password;
    	}

    	if (type == MYSQL) {
    		url += BASEURL[type] + "//" + host + ":" + port + "/" + dbname;
    	}

    	if (type == ORACLE) {
    		url += BASEURL[type] + "@" + host + ":" + port + ":" + dbname;
    	}
    	
    	if (type == POSTGRESQL) {
    		url += BASEURL[type] + "//" + host + ":" + port + ":" + dbname;
    	}

    	return url;
    }


    protected Set<String> getUserEntities() throws SQLException {

    	if (type != MSACCESS)
    		return getStandardJDBCUserEntities(md.getUserName());
    	return getStandardJDBCUserEntities(null);
    }

    protected Set<String> getStandardJDBCUserEntities(String user) throws SQLException {
    	String[] types = {"TABLE"};
    	ResultSet rs = md.getTables(null, user, "%", types);
    	HashSet<String> userEntitiesSet = new HashSet<String>();
    	while (rs.next()) {
    		userEntitiesSet.add(rs.getString("TABLE_NAME"));
    	}
    	return userEntitiesSet;

    }

    public ResultSet executeQuery(String query) {
    	ResultSet rs = null;
    	try {
    		stmt = connection.createStatement();
    		//System.out.println(query);
    		//logger.debug("QUERY: \n" + query);


			rs = stmt.executeQuery(query);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;

    }

    protected String getTable(String fromItem) {
    	return fromItem.substring(0,fromItem.indexOf(SEPARATOR));
    }


    public void dipose() {
    	try {
        	connection.close();
        	connection = null;
    	}
    	catch (SQLException ex) {

    		ex.printStackTrace();
    	}
    }


    public static void main (String [] args) {
    	DBConnector db = new DBConnector();
    	db.executeQuery("");


    }


}
