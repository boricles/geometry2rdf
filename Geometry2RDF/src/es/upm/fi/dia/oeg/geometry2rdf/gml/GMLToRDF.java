/*
 * @(#) GMLToRDF.java	0.1	2010/08/05
 *
 * Copyright (C) 2010 vsaquicela
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
package es.upm.fi.dia.oeg.geometry2rdf.gml;

import es.upm.fi.dia.oeg.geometry2rdf.Constants;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import es.upm.fi.dia.oeg.geometry2rdf.Constants;
import es.upm.fi.dia.oeg.geometry2rdf.HashGeometry;
import es.upm.fi.dia.oeg.geometry2rdf.db.DbConnector;
import es.upm.fi.dia.oeg.geometry2rdf.db.DbConstants;
import es.upm.fi.dia.oeg.geometry2rdf.db.MsAccessDbConnector;
import es.upm.fi.dia.oeg.geometry2rdf.db.MySqlDbConnector;
import es.upm.fi.dia.oeg.geometry2rdf.db.OracleDbConnector;
import es.upm.fi.dia.oeg.geometry2rdf.db.PostgresqlDbConnector;
import es.upm.fi.dia.oeg.geometry2rdf.utils.UtilsLib;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.util.Properties;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 *
 * @author vsaquicela
 * @author magarcia
 *
 */
public class GMLToRDF {

  Model model;
  IDBConnection conn;
  String nsgeontology ;
  String nsgeoresource ;
  String sourceRS;
  String targetRS;
  String gmlSourceRS;
  String gmlTargetRS;
  String pointType;
  String linestringType;
  String polygonType;
  String formBy;
  static GMLToRDF dtr;
  private String defaultLang = "es";


  public GMLToRDF(String dir) throws ClassNotFoundException {

    if (!dir.endsWith("/")) {
      dir = dir + "/";
    }

    File f = new File(dir);

    if (!f.isDirectory()) {
      f.mkdir();
    }

    dir = dir + "TDB/";

    f = new File(dir);

    if (f.isDirectory()) {
      if (f.exists()) {
        String[] ficheros = f.list();
        if (ficheros.length > 0) {
          for (int i = 0; i < ficheros.length; i++) {
            File auxFile = new File(dir + ficheros[i]);
            auxFile.delete();
          }
        }
        f.delete();
      }
    }

    f.mkdir();

    model = TDBFactory.createModel(dir);
    model.setNsPrefix("geo", Constants.NSGEO);
    model.setNsPrefix("xsd", Constants.NSXSD);
  }

  public static void main(String[] args) throws Exception {
    if (args.length == 1) {
      Properties properties = new Properties();

      properties.load(new FileInputStream(args[0]));

      String inputDir = properties.getProperty("inputDir");
      String outputFile = properties.getProperty("outputFile");

      dtr = new GMLToRDF(inputDir);

      int dbType = Integer.valueOf(properties.getProperty("dbType"));
      String dbName = properties.getProperty("dbName");
      String dbUserName = properties.getProperty("dbUserName");
      String dbPassword = properties.getProperty("dbPassword");
      String dbHost = properties.getProperty("dbHost");
      int dbPort = Integer.getInteger(properties.getProperty("dbPort"));

      DbConnector databaseConnector = null;

      switch(dbType) {
        case DbConstants.MSACCESS:
          databaseConnector = new MsAccessDbConnector(
                  dbHost, dbPort, dbName, dbUserName, dbPassword);
          break;
        case DbConstants.MYSQL:
          databaseConnector = new MySqlDbConnector(
                  dbHost, dbPort, dbName, dbUserName, dbPassword);
          break;
        case DbConstants.ORACLE:
          databaseConnector = new OracleDbConnector(
                  dbHost, dbPort, dbName, dbUserName, dbPassword);
          break;
        case DbConstants.POSTGRESQL:
          databaseConnector = new PostgresqlDbConnector(
                  dbHost, dbPort, dbName, dbUserName, dbPassword);
          break;
      }


      String resourceName = properties.getProperty("resourceName");

      String tableName = properties.getProperty("tableName");
      String condition = properties.getProperty("condition");
      String labelColumnName = properties.getProperty("labelColumnName");
      String geometryColumnName = properties.getProperty("geometryColumnName");

      // Namespace parameters
      String namespacePrefix = properties.getProperty("nsPrefix");
      if (UtilsLib.isNullOrEmpty(namespacePrefix)) {
        namespacePrefix = "georesource";
      }
      String namespace = properties.getProperty("nsURI");
      if (UtilsLib.isNullOrEmpty(namespace)) {
        namespace = "http://geo.linkeddata.es/resource/";
      }
      String ontologyNSPrefix = properties.getProperty("ontologyNSPrefix");
      if (UtilsLib.isNullOrEmpty(ontologyNSPrefix)) {
        ontologyNSPrefix = "geontology";
      }
      String ontologyNamespace = properties.getProperty("ontologyNS");
      if (UtilsLib.isNullOrEmpty(ontologyNamespace)) {
        ontologyNamespace = "http://geo.linkeddata.es/ontology/";
      }
      dtr.model.setNsPrefix(ontologyNSPrefix, ontologyNamespace);
      dtr.model.setNsPrefix(namespacePrefix, namespace);
      dtr.nsgeontology = ontologyNamespace;
      dtr.nsgeoresource = namespace;

      // Reference systems parameters
      dtr.gmlSourceRS = properties.getProperty("gmlSourceRS");
      dtr.gmlTargetRS = properties.getProperty("gmlTargetRS");
      dtr.sourceRS = properties.getProperty("sourceRS");
      dtr.targetRS = properties.getProperty("targetRS");

      // Types parameters
      dtr.pointType = properties.getProperty("pointType");
      if (UtilsLib.isNullOrEmpty(dtr.pointType)) {
        dtr.pointType = "http://www.w3.org/2003/01/geo/wgs84_pos#Point";
      }
      dtr.linestringType = properties.getProperty("linestringType");
      if (UtilsLib.isNullOrEmpty(dtr.linestringType)) {
        dtr.pointType = "http://geo.linkeddata.es/ontology/Curva";
      }
      dtr.polygonType = properties.getProperty("polygonType");
      if (UtilsLib.isNullOrEmpty(dtr.polygonType)) {
        dtr.pointType = "http://geo.linkeddata.es/ontology/Pol%C3%ADgono";
      }
      dtr.formBy = properties.getProperty("formBy");
      if (UtilsLib.isNullOrEmpty(dtr.formBy)) {
        dtr.formBy = "formadoPor";
      }

      // Other parameters
      dtr.defaultLang = properties.getProperty("defaultLang");
      if (UtilsLib.isNullOrEmpty(dtr.defaultLang)) {
        dtr.defaultLang = "es";
      }
      dtr.executeParser(databaseConnector, tableName, resourceName, condition, outputFile,
              labelColumnName, geometryColumnName);
    } else {
      System.out.println("Incorrect arguments number. Properties file required.");
    }
  }

  private void executeParser(DbConnector dbConn, String tableName, String resource,
                            String condition, String outputFile, String labelColumnName,
                            String geometryColumnName) throws Exception {
    int totalRows;
    int from = 1;
    int until;

    String sql = "select count(*) as total from " + tableName + " where " + labelColumnName
                 + "<>' ' and (" + condition + ") order by " + labelColumnName;

    ResultSet rs = dbConn.executeQuery(sql);
    rs.next();
    totalRows = rs.getInt("total"); //total de filas de la tabla
    System.out.println("Number of records in DDBB to be processed: " + totalRows);
    int records = 500;
    int numRec = 1;
    until = records;
    if (records > totalRows) {
      records = totalRows;
    }

    if (until > totalRows) {
      until = totalRows;
    }

    while (until <= totalRows) {

      System.out.println("Processing records from " + from + " to " + until);

      sql = "select * from (select e1.*, rownum rnum from (select e." + labelColumnName
            + " e , SDO_UTIL.TO_GMLGEOMETRY(" + geometryColumnName + ") GmlGeometry from "
            + tableName + " e where e." + labelColumnName + "<>' ' and (" + condition
            + ") order by " + labelColumnName + ") e1 where rownum<=" + until
            + " ) where rnum>=" + from;
      rs = dbConn.executeQuery(sql);

      while (rs.next()) {
        String gml = rs.getString("GmlGeometry");
        if (!UtilsLib.isNullOrEmpty(dtr.gmlSourceRS) && !UtilsLib.isNullOrEmpty(dtr.gmlTargetRS)) {
          gml = rs.getString("GmlGeometry").replace(dtr.gmlSourceRS, dtr.gmlTargetRS);
        }
        dtr.parseGML2RDF(resource, rs.getString("e"), gml, dtr.sourceRS, dtr.targetRS);

        numRec++;
      }

      from = from + records;
      until = until + records;
      if (from > totalRows) {
        break;
      }
      if (until > totalRows) {
        until = totalRows;
      }
    }

    FileOutputStream out = new FileOutputStream(outputFile);
    dtr.model.write(out);
    System.out.print("Process finished");
  }

  private void parseGML2RDF(String tipo, String resource,
                            String s, String source, String target) {
    try {
      InputStream is = UtilsLib.convertStringToInputStream(s);
      org.geotools.xml.Configuration configuration = new org.geotools.gml2.GMLConfiguration();
      org.geotools.xml.Parser parser = new org.geotools.xml.Parser(configuration);
      Geometry o = (Geometry) parser.parse(is);

      if (!UtilsLib.isNullOrEmpty(source) && !UtilsLib.isNullOrEmpty(target)) {
        //Quiere decir que hay que transformar
        CoordinateReferenceSystem sourceCRS = CRS.decode(source);
        CoordinateReferenceSystem targetCRS = CRS.decode(target);
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
        Geometry targetGeometry = JTS.transform(o, transform);
        o = targetGeometry;
      }

      String hash = HashGeometry.getHash(s);
      String encTipo = URLEncoder.encode(tipo, "utf-8").replace("+", "%20");
      String encResource = URLEncoder.encode(resource, "utf-8").replace("+", "%20");
      String aux = encTipo + "/" + encResource;
      System.out.println("New element-->" + dtr.nsgeoresource + aux);

      insertarResourceTypeResource(dtr.nsgeoresource + aux, dtr.nsgeontology
                                   + URLEncoder.encode(tipo, "utf-8").replace("+", "%20"));
      insertarLabelResource(dtr.nsgeoresource + aux, resource, dtr.defaultLang);

      if (o.getGeometryType().equals("LineString")) {
        insertarGeometry(aux, hash, o);
      } else if (o.getGeometryType().equals("Polygon")) {
        insertarGeometry(aux, hash, o);
      } else if (o.getGeometryType().equals("MultiPolygon")) {
        MultiPolygon mp = (MultiPolygon) o;
        int numero = mp.getNumGeometries();
        for (int y = 0; y < numero; y++) {
          Geometry c = mp.getGeometryN(y);
          String newHash = HashGeometry.getHash(c.toText());
          insertarGeometry(aux, newHash, o);
        }
      } else if (o.getGeometryType().equals("MultiLineString")) {
        MultiLineString mp = (MultiLineString) o;
        int numero = mp.getNumGeometries();
        for (int y = 0; y < numero; y++) {
          Geometry c = mp.getGeometryN(y);
          String newHash = HashGeometry.getHash(c.toText());
          insertarGeometry(aux, newHash, o);
        }
      } else if (o.getGeometryType().equals("Point")) {
        insertPoint((Point) o, aux);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

  }

  private void insertarTripletaLiteral(String s, String p, String o, XSDDatatype x) {
    //Permite ingresar una tripleta en el rdf
    if (x != null) {
      Literal l = model.createTypedLiteral(o, x);
      Resource rGeometry = model.createResource(s);
      Property P = model.createProperty(p);
      rGeometry.addLiteral(P, l);
    } else {
      Resource rGeometry = model.createResource(s);
      Property P = model.createProperty(p);
      rGeometry.addProperty(P, o);
    }
  }

  private void insertarTripletaResource(String s, String p, String o) {
    //Permite ingresar una tripleta en el rdf
    Resource rGeometry = model.createResource(s);
    Property P = model.createProperty(p);
    Resource r2 = model.createResource(o);
    rGeometry.addProperty(P, r2);

  }

  private void insertarResourceTypeResource(String r1, String r2) {

    Resource resource1 = model.createResource(r1);
    Resource resource2 = model.createResource(r2);
    model.add(resource1, RDF.type, resource2);
  }

  private void insertarLabelResource(String r1, String label, String lang) {

    Resource resource1 = model.createResource(r1);
    model.add(resource1, RDFS.label, model.createLiteral(label, lang));
  }

  private void insertarGeometry(String resource, String hash, Geometry geo) throws UnsupportedEncodingException {
    if (geo.getGeometryType().equals("LineString")) {
      insertarResourceTypeResource(dtr.nsgeoresource + hash, dtr.nsgeontology + "Curva");
    } else if (geo.getGeometryType().equals("Polygon")) {
      insertarResourceTypeResource(dtr.nsgeoresource + hash, dtr.nsgeontology
                                   + URLEncoder.encode("Pol�gono", "utf-8").replace("+", "%20"));
    }

    insertarTripletaResource(dtr.nsgeoresource + resource, Constants.NSGEO + "geometry",
                             dtr.nsgeoresource + hash);
    insertarTripletaLiteral(dtr.nsgeoresource + hash, dtr.nsgeontology + "gml",
                            geo.toText(), null);

    if (geo.getGeometryType().equals("LineString")) {
      insertLinestring((LineString) geo, hash);
    } else if (geo.getGeometryType().equals("Polygon")) {
      insertPolygon((Polygon) geo, hash);
    }
  }

  private void insertPoint(Point p, String resource) {
    insertarTripletaResource(dtr.nsgeoresource + resource, Constants.NSGEO + "geometry",
                             dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
    //insertarTripletaResource(dtr.nsgeoresource + resource, dtr.nsgeontology + dtr.formBy, dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
    insertarResourceTypeResource(dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(),
                                 Constants.NSGEO + "Point");
    insertarTripletaLiteral(dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(),
                            Constants.NSGEO + "lat", String.valueOf(p.getY()), XSDDatatype.XSDdouble);
    insertarTripletaLiteral(dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(),
                            Constants.NSGEO + "long",
                            String.valueOf(p.getX()), XSDDatatype.XSDdouble);
  }

  private void insertPolygon(Polygon po, String hash) {
    int i = 0;
    for (Coordinate c : po.getCoordinates()) {//Si queremos tratar la Z
      insertarTripletaResource(dtr.nsgeoresource + hash,
                               dtr.nsgeontology + dtr.formBy,
                               dtr.nsgeoresource + "wgs84/" + c.y + "_" + c.x);
      insertarResourceTypeResource(dtr.nsgeoresource + "wgs84/" + c.y + "_" + c.x,
                                   Constants.NSGEO + "Point");
      insertarTripletaLiteral(dtr.nsgeoresource + "wgs84/" + c.y + "_" + c.x,
                              Constants.NSGEO + "lat", String.valueOf(c.y),
                              XSDDatatype.XSDdouble);
      insertarTripletaLiteral(dtr.nsgeoresource + "wgs84/" + c.y + "_" + c.x,
                              Constants.NSGEO + "long",
                              String.valueOf(c.x), XSDDatatype.XSDdouble);
      insertarTripletaLiteral(dtr.nsgeoresource + "wgs84/" + c.y + "_" + c.x,
                              dtr.nsgeontology + "orden", String.valueOf(i), XSDDatatype.XSDint);

      i++;
    }
  }

  private void insertLinestring(LineString ls, String hash) {
    for (int i = 0; i < ls.getNumPoints(); i++) { //puntos de la geometria X,Y
      Point p = ls.getPointN(i);
      insertarTripletaResource(dtr.nsgeoresource + hash, dtr.nsgeontology + dtr.formBy,
                               dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
      insertarResourceTypeResource(dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(),
                                   Constants.NSGEO + "Point");
      insertarTripletaLiteral(dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(),
                              Constants.NSGEO + "lat",
                              String.valueOf(p.getY()), XSDDatatype.XSDdouble);
      insertarTripletaLiteral(dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(),
                              Constants.NSGEO + "long",
                              String.valueOf(p.getX()), XSDDatatype.XSDdouble);
      insertarTripletaLiteral(dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(),
                              dtr.nsgeontology + "order", String.valueOf(i), XSDDatatype.XSDint);
    }
  }


}