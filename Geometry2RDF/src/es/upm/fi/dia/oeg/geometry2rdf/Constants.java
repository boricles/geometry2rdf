/*
 * @(#) Constants.java	0.1	2011/01/31
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

package es.upm.fi.dia.oeg.geometry2rdf;

public class Constants {
  // TODO(jonbaraq): Add comments to all these constants.
  public static final String NSGEO = "http://www.w3.org/2003/01/geo/wgs84_pos#";
  public static final String NSGML = "http://loki.cae.drexel.edu/~wbs/ontology/2004/09/ogc-gml#";
  public static final String NSXSD = "http://www.w3.org/2001/XMLSchema#";

  public static final String LINE_STRING = "LineString";
  public static final String MULTI_LINE_STRING = "MultiLineString";
  public static final String POLYGON = "Polygon";
  public static final String MULTI_POLYGON = "MultiPolygon";
  public static final String POINT = "Point";
  public static final String LATITUDE = "lat";
  public static final String LONGITUDE = "long";
  public static final String GML = "gml";
}