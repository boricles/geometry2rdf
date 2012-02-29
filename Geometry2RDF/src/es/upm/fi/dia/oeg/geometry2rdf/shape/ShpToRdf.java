/*
 * @(#) ShpToRdf.java	0.1	2012/02/14
 *
 * Copyright (C) 2012 jonbaraq
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
package es.upm.fi.dia.oeg.geometry2rdf.shape;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
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
import es.upm.fi.dia.oeg.geometry2rdf.utils.Configuration;
import es.upm.fi.dia.oeg.geometry2rdf.utils.UtilsConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureImpl;

/**
 * Class to convert shapefiles to RDF.
 *
 * @author jonbaraq
 */
public class ShpToRdf {

  private static final Logger LOG = Logger.getLogger(ShpToRdf.class.getName());

  private static final String STRING_TO_REPLACE = "+";
  private static final String REPLACEMENT = "%20";
  private static final String SEPARATOR = "_";

  private Model model;
  private Configuration configuration;
  private FeatureCollection featureCollection;

  public ShpToRdf(Configuration configuration) throws IOException {
    this.configuration = configuration;
    model = getModelFromConfiguration(configuration);
    featureCollection = getShapeFileFeatureCollection(configuration.inputFile,
                                                      configuration.featureString);
  }

  /**
   * Loads the shape file from the configuration path and returns the
   * feature collection associated according to the configuration.
   *
   * @param shapePath with the path to the shapefile.
   * @param featureString with the featureString to filter.
   *
   * @return FeatureCollection with the collection of features filtered.
   */
  private FeatureCollection getShapeFileFeatureCollection(
          String shapePath, String featureString) throws IOException {
    File file = new File(shapePath);

    // Create the map with the file URL to be passed to DataStore.
    Map map = new HashMap();
    try {
      map.put("url", file.toURL());
    } catch (MalformedURLException ex) {
      Logger.getLogger(ShpToRdf.class.getName()).log(Level.SEVERE, null, ex);
    }
    if (map.size() > 0) {
      DataStore dataStore = DataStoreFinder.getDataStore(map);
      FeatureSource featureSource = dataStore.getFeatureSource(featureString);
      return featureSource.getFeatures();
    }
    return null;
  }

  /**
   * Returns a Jena RDF model populated with the params from the configuration.
   *
   * @param configuration with all the configuration parameters.
   *
   * @return a Jena RDF model populated with the params from the configuration.
   */
  private Model getModelFromConfiguration(Configuration configuration) {
    removeDirectory(configuration.tmpDir);
    Model tmpModel = TDBFactory.createModel(configuration.tmpDir);
    tmpModel.removeAll();
    tmpModel.setNsPrefix("geontology", configuration.ontologyNS);
    tmpModel.setNsPrefix("georesource", configuration.nsUri);
    tmpModel.setNsPrefix("geo", URLConstants.NS_GEO);
    tmpModel.setNsPrefix("dc", URLConstants.NS_DC);
    tmpModel.setNsPrefix("xsd", URLConstants.NS_XSD);
    return tmpModel;
  }

  public void writeRdfModel() throws UnsupportedEncodingException, FileNotFoundException {
    FeatureIterator iterator = featureCollection.features();
    try {
      int position = 0;
      while(iterator.hasNext()) {
        SimpleFeatureImpl feature = (SimpleFeatureImpl) iterator.next();
        Geometry geometry = (Geometry) feature.getDefaultGeometry();

        String featureAttribute = "featureWithoutName";

        if (feature.getAttribute(configuration.attribute) != null) {
          featureAttribute = feature.getAttribute(configuration.attribute).toString();
        }

        LOG.log(Level.INFO,
                "writeRdfModel: Processing feature attribute {0}",
                featureAttribute);

        if (!featureAttribute.equals(configuration.ignore)) {
          String hash = HashGeometry.getHash(geometry.toText());
          String encodingType =
                  URLEncoder.encode(configuration.type,
                                    UtilsConstants.UTF_8).replace(STRING_TO_REPLACE,
                                                                  REPLACEMENT);
          String encodingResource =
                  URLEncoder.encode(featureAttribute,
                                    UtilsConstants.UTF_8).replace(STRING_TO_REPLACE,
                                                                  REPLACEMENT);
          String aux = encodingType + "/" + encodingResource;
          insertResourceTypeResource(
             configuration.nsUri + aux,
             configuration.ontologyNS + URLEncoder.encode(
                 configuration.type, UtilsConstants.UTF_8).replace(
                     STRING_TO_REPLACE, REPLACEMENT));
          insertLabelResource(configuration.nsUri + aux,
                              featureAttribute, configuration.defaultLang);
          LOG.log(Level.INFO,
                  "writeRdfModel: GeometryType--> {0}",
                  geometry.getGeometryType());
          if (geometry.getGeometryType().equals(Constants.POINT)) {
            insertPoint((Point) geometry, aux);
          } else if (geometry.getGeometryType().equals(Constants.LINE_STRING)) {
            insertLineString(aux, hash, geometry);
          } else if (geometry.getGeometryType().equals(Constants.POLYGON)) {
            insertPolygon(aux, hash, geometry);
          } else if (geometry.getGeometryType().equals(Constants.MULTI_POLYGON)) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            for (int i = 0; i < multiPolygon.getNumGeometries(); ++i) {
              Geometry tmpGeometry = multiPolygon.getGeometryN(i);
              String newHash = HashGeometry.getHash(tmpGeometry.toText());
              if (tmpGeometry.getGeometryType().equals(Constants.POLYGON)) {
                insertPolygon(aux, newHash, tmpGeometry);
              } else if (tmpGeometry.getGeometryType().equals(Constants.LINE_STRING)) {
                insertLineString(aux, newHash, tmpGeometry);
              }
            }
          } else if (geometry.getGeometryType().equals(Constants.MULTI_LINE_STRING)) {
            MultiLineString multiLineString = (MultiLineString) geometry;
            for (int i = 0; i < multiLineString.getNumGeometries(); ++i) {
              Geometry tmpGeometry = multiLineString.getGeometryN(i);
              String newHash = HashGeometry.getHash(tmpGeometry.toText());
              if (tmpGeometry.getGeometryType().equals(Constants.POLYGON)) {
                insertPolygon(aux, newHash, tmpGeometry);
              } else if (tmpGeometry.getGeometryType().equals(Constants.LINE_STRING)) {
                insertLineString(aux, newHash, tmpGeometry);
              }
            }
          }
        } else {
          LOG.log(Level.INFO,
                  "writeRdfModel: Not processing feature attribute in position {0}",
                  position);
        }
        ++position;
      }
    }
    finally {
      iterator.close();
    }
    FileOutputStream out = new FileOutputStream(configuration.outputFile);
    model.write(out);
  }


  private void removeDirectory(String path) {
    File filePath = new File(path);
    if (filePath.exists()) {
      for (String fileInDirectory : filePath.list()) {
        File tmpFile = new File(path + "/" + fileInDirectory);
        tmpFile.delete();
      }
      filePath.delete();
    }
  }

  private void insertLineString(String resource, String hash, Geometry geo) {
    insertResourceTypeResource(configuration.nsUri + hash,
                               configuration.ontologyNS + "Curva");
    insertResourceTriplet(configuration.nsUri + resource,
                          URLConstants.NS_GEO + "geometry",
                          configuration.nsUri + hash);
    insertLiteralTriplet(configuration.nsUri + hash,
                         configuration.ontologyNS + Constants.GML,
                         geo.toText(), null);
    LineString lineString = (LineString) geo;
    insertCurve(lineString, hash);
  }

  private void insertPolygon(String resource, String hash, Geometry geo)
          throws UnsupportedEncodingException {
    insertResourceTypeResource(
            configuration.nsUri + hash,
            configuration.ontologyNS
            + URLEncoder.encode("Polígono", UtilsConstants.UTF_8).replace(
                 STRING_TO_REPLACE, REPLACEMENT));
    insertResourceTriplet(configuration.nsUri + resource,
                          URLConstants.NS_GEO + "geometry",
                          configuration.nsUri + hash);
    insertLiteralTriplet(configuration.nsUri + hash,
                         configuration.ontologyNS + Constants.GML,
                         geo.toText(), null);
    Polygon polygon = (Polygon) geo;
    insertPolygon(polygon, hash);
  }

  private void insertResourceTypeResource(String r1, String r2) {
    Resource resource1 = model.createResource(r1);
    Resource resource2 = model.createResource(r2);
    model.add(resource1, RDF.type, resource2);
  }

  private void insertLiteralTriplet(String s, String p, String o, XSDDatatype x) {
    Resource resourceGeometry = model.createResource(s);
    Property property = model.createProperty(p);
    if (x != null) {
      Literal literal = model.createTypedLiteral(o, x);
      resourceGeometry.addLiteral(property, literal);
    } else {
      resourceGeometry.addProperty(property, o);
    }
  }

  private void insertResourceTriplet(String s, String p, String o) {
    Resource resourceGeometry = model.createResource(s);
    Property property = model.createProperty(p);
    Resource resourceGeometry2 = model.createResource(o);
    resourceGeometry.addProperty(property, resourceGeometry2);
  }

  private void insertLabelResource(String resource, String label, String lang) {
    Resource resource1 = model.createResource(resource);
    model.add(resource1, RDFS.label, model.createLiteral(label, lang));
  }

  private void insertPolygon(Polygon po, String hash) {
    int i = 0;
    for (Coordinate c : po.getCoordinates()) {
      insertResourceTriplet(
          configuration.nsUri + hash, configuration.ontologyNS + "formadoPor",
          configuration.nsUri + UtilsConstants.WGS84 + c.y + SEPARATOR + c.x);
      insertResourceTypeResource(
          configuration.nsUri + UtilsConstants.WGS84 + c.y + SEPARATOR + c.x,
          URLConstants.NS_GEO + Constants.POINT);
      insertLiteralTriplet(
          configuration.nsUri + UtilsConstants.WGS84 + c.y + "_" + c.x,
          URLConstants.NS_GEO + Constants.LATITUDE,
          String.valueOf(c.y), XSDDatatype.XSDdouble);
      insertLiteralTriplet(
          configuration.nsUri + UtilsConstants.WGS84 + c.y + "_" + c.x,
          URLConstants.NS_GEO + Constants.LONGITUDE,
          String.valueOf(c.x), XSDDatatype.XSDdouble);
      insertLiteralTriplet(
          configuration.nsUri + UtilsConstants.WGS84 + c.y + "_" + c.x,
          configuration.ontologyNS + "orden", String.valueOf(i), XSDDatatype.XSDint);
      i++;
    }
  }

  private void insertCurve(LineString ls, String hash) {
    for (int i = 0; i < ls.getNumPoints(); i++) { //puntos de la geometria X,Y
      Point p = ls.getPointN(i);
      insertResourceTriplet(
          configuration.nsUri + hash, configuration.ontologyNS + "formadoPor",
          configuration.nsUri + UtilsConstants.WGS84 + p.getY() + SEPARATOR + p.getX());
      insertResourceTypeResource(
          configuration.nsUri + UtilsConstants.WGS84 + p.getY() + SEPARATOR + p.getX(),
          URLConstants.NS_GEO + Constants.POINT);
      insertLiteralTriplet(
          configuration.nsUri + UtilsConstants.WGS84 + p.getY() + "_" + p.getX(),
          URLConstants.NS_GEO + Constants.LATITUDE,
          String.valueOf(p.getY()), XSDDatatype.XSDdouble);
      insertLiteralTriplet(
          configuration.nsUri + UtilsConstants.WGS84 + p.getY() + "_" + p.getX(),
          URLConstants.NS_GEO + Constants.LONGITUDE,
          String.valueOf(p.getX()), XSDDatatype.XSDdouble);
      insertLiteralTriplet(
          configuration.nsUri + UtilsConstants.WGS84 + p.getY() + "_" + p.getX(),
          configuration.ontologyNS + "order", String.valueOf(i), XSDDatatype.XSDint);
    }
  }

  private void insertPoint(Point p, String resource) {
    insertResourceTriplet(
        configuration.nsUri + resource, URLConstants.NS_GEO + "geometry",
        configuration.nsUri + UtilsConstants.WGS84 + p.getY() + SEPARATOR + p.getX());
    insertResourceTypeResource(
       configuration.nsUri + UtilsConstants.WGS84 + p.getY() + SEPARATOR + p.getX(),
       URLConstants.NS_GEO + Constants.POINT);
    insertLiteralTriplet(
        configuration.nsUri + UtilsConstants.WGS84 + p.getY() + SEPARATOR + p.getX(),
        URLConstants.NS_GEO + Constants.LATITUDE,
        String.valueOf(p.getY()), XSDDatatype.XSDdouble);
    insertLiteralTriplet(
        configuration.nsUri + UtilsConstants.WGS84 + p.getY() + "_" + p.getX(),
        URLConstants.NS_GEO + Constants.LONGITUDE,
        String.valueOf(p.getX()), XSDDatatype.XSDdouble);
  }

}