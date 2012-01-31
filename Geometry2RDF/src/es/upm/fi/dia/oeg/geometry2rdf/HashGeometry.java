/*
 * @(#) HashGeometry.java	0.1	2010/08/05
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
package es.upm.fi.dia.oeg.geometry2rdf;

import java.security.MessageDigest;

/**
 *
 * @author vsaquicela
 */
public class HashGeometry {

  public HashGeometry() {
  }

  public static void main(String[] args) throws Exception {
    System.out.println(HashGeometry.getHash("<htl>algunacosa"));
  }

  public static String getHash(String message) {
    MessageDigest md;
    byte[] buffer, digest;
    String hash = "";

    try {
      buffer = message.getBytes("UTF-8");
      md = MessageDigest.getInstance("SHA1");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    md.update(buffer);
    digest = md.digest();
    for (byte aux : digest) {
      int b = aux & 0xff;
      String s = Integer.toHexString(b);
      if (s.length() == 1) {
        hash += "0";
      }
      hash += s;
    }
    return hash;
  }
}
