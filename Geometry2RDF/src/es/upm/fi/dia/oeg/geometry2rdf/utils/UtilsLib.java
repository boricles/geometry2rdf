/*
 * @(#) UtilsLib.java	0.1	2012/02/03
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Utils Library for geometry2rdf
 *
 * @author Jonathan Gonzalez (jonathan@jonbaraq.eu)
 * @version 2nd Feb 2012.
 */
public class UtilsLib {

  /**
   * Returns true if the parameter is null or empty. false otherwise.
   *
   * @param text
   * @return true if the parameter is null or empty.
   */
  public static boolean isNullOrEmpty(String text) {
    if (text == null || text.equals("")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns a String with the content of the InputStream
   * @param is with the InputStream
   * @return string with the content of the InputStream
   * @throws IOException
   */
  public static String convertInputStreamToString(InputStream is)
          throws IOException {
    if (is != null) {
      StringBuilder sb = new StringBuilder();
      String line;
      try {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, UtilsConstants.UTF_8));
        while ((line = reader.readLine()) != null) {
          sb.append(line).append(UtilsConstants.LINE_SEPARATOR);
        }
      } finally {
        is.close();
      }
      return sb.toString();
    } else {
      return "";
    }
  }

  /**
   * Returns am InputStream with the parameter.
   *
   * @param string
   * @return InputStream with the string value.
   */
  public static InputStream convertStringToInputStream(String string) {
    InputStream is = null;
    try {
      is = new ByteArrayInputStream(string.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return is;
  }

}
