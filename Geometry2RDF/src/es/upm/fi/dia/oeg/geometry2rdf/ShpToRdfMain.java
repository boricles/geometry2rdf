/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.upm.fi.dia.oeg.geometry2rdf;

import es.upm.fi.dia.oeg.geometry2rdf.shape.ShpToRdf;
import es.upm.fi.dia.oeg.geometry2rdf.utils.Configuration;
import java.io.IOException;

/**
 *
 * @author jonathangsc
 */
public class ShpToRdfMain {
  public static void main(String [] args) throws IOException {
    Configuration configuration =
            new Configuration(
                    "/home/jonathangsc/Downloads/tmp_geo/shpoptions.properties");
    ShpToRdf shpConverter = new ShpToRdf(configuration);
    shpConverter.writeRdfModel();
  }

}
