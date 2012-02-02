/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author jonathangsc
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
    if (text == null && text.equals("")) {
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
