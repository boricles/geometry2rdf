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
 * @author Jonathan Gonzalez (jonathan@jonbaraq.eu)
 */
public class ShpToRdfMain {

	public static void main(String [] args) throws IOException {
	  
	  if (args.length == 1) {  
            String propertiesFilePath = args[0];
	    Configuration configuration = new Configuration(propertiesFilePath);
	    ShpToRdf shpConverter = new ShpToRdf(configuration);
	    shpConverter.writeRdfModel();
	  } else {
		System.err.println("Incorrect number of arguments. Please specify the properties file location.");
          }
        }

}
