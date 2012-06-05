/*
 * @(#) Constants.java	0.1	2012/02/14
 *
 * Copyright (C) 2012 Jonathan Gonzalez (jonathan@jonbaraq.eu)
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
package es.upm.fi.dia.oeg.geometry2rdf.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to parse configuration files used in the library.
 *
 * @author Jonathan Gonzalez (jonathan@jonbaraq.eu)
 */
public final class Configuration {

  public String path;
  public String inputFile;
  public String tmpDir;
  public String outputFile;
  public String resourceName;
  public String nsPrefix = "georesource";
  public String ontologyNSPrefix = "geontology";
  public String ontologyNS = "http://geo.linkeddata.es/ontology/";
  public String nsUri = "http://geo.linkeddata.es/resource/";
  public String pointType = "http://www.w3.org/2003/01/geo/wgs84_pos#Point";
  public String lineStringType = "http://geo.linkeddata.es/ontology/Curva";
  public String polygonType = "http://geo.linkeddata.es/ontology/Pol%C3%ADgono";
  public String formedBy = "formadoPor";
  public String defaultLang = "es";
  public String featureString;
  public String attribute;
  public String ignore = "";
  public String type;
  public String condition = "";
  public String operation = "";
  public String langAttribute = "";
  public String property = "";
  
  public String sourceRS;
  public String targetRS;

  private static final Logger LOG = Logger.getLogger(Configuration.class.getName());

  public Configuration(String path) {
    this.path = path;
    buildConfiguration();
  }

  /**
   * Loads the configuration
   */
  private void buildConfiguration() {
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(path));
    } catch (IOException io) {
      LOG.log(Level.WARNING, "Problems loading configuration file: {0}", io);
    }
    initializeParameters(properties, path.substring(0, path.lastIndexOf("/") + 1));
  }

  /**
   * Initializes all the parameters from the configuration.
   *
   * @param properties with the properties object.
   */
  private void initializeParameters(Properties properties, String path) {
    inputFile = path + properties.getProperty("inputFile");
    tmpDir = path + properties.getProperty("tempDir");
    outputFile = path + properties.getProperty("outputFile");
    resourceName = properties.getProperty("resourceName");

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("nsPrefix"))) {
      nsPrefix = properties.getProperty("nsPrefix");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("nsURI"))) {
      nsUri = properties.getProperty("nsURI");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("ontologyNSPrefix"))) {
      ontologyNSPrefix = properties.getProperty("ontologyNSPrefix");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("ontologyNS"))) {
      ontologyNS = properties.getProperty("ontologyNS");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("pointType"))) {
      pointType = properties.getProperty("pointType");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("linestringType"))) {
      lineStringType = properties.getProperty("linestringType");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("polygonType"))) {
      polygonType = properties.getProperty("polygonType");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("formedBy"))) {
      formedBy = properties.getProperty("formedBy");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("defaultLang"))) {
      defaultLang = properties.getProperty("defaultLang");
    }
    
    if (!UtilsLib.isNullOrEmpty(properties.getProperty("condition"))) {
        condition = properties.getProperty("condition");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("operation"))) {
    	operation = properties.getProperty("operation");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("langAttribute"))) {
    	langAttribute = properties.getProperty("langAttribute");
    }
  
    if (!UtilsLib.isNullOrEmpty(properties.getProperty("ignore"))) {
    	ignore = properties.getProperty("ignore");
    }

    if (!UtilsLib.isNullOrEmpty(properties.getProperty("property"))) {
    	property = properties.getProperty("property");
    }
    
    //Reference Systems parameters
    if (!UtilsLib.isNullOrEmpty(properties.getProperty("sourceRS"))) {
    	sourceRS = properties.getProperty("sourceRS");
    }
    
    if (!UtilsLib.isNullOrEmpty(properties.getProperty("targetRS"))) {
    	targetRS = properties.getProperty("targetRS");
    }

    featureString = properties.getProperty("featureString");
    attribute = properties.getProperty("attribute");
    type = properties.getProperty("type");
    
  }

}
