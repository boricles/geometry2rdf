/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.upm.fi.dia.oeg.geometry2rdf;

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

import es.upm.fi.dia.oeg.geometry2rdf.db.DBConnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


/**
 *
 * @author boricles
 */
public class SHPToRDF {

    Model model;
    IDBConnection conn;
    String nsgeontology = "http://geo.linkeddata.es/ontology/";
    String nsgeoresource = "http://geo.linkeddata.es/resource/";
    String nsgeo = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    String nsgml = "http://loki.cae.drexel.edu/~wbs/ontology/2004/09/ogc-gml#";
    String nsdc = "http://purl.org/dc/terms/";
    String nsxsd = "http://www.w3.org/2001/XMLSchema#";

    String pointType;
    String linestringType;
    String polygonType;
    String formBy;
    
    String defaultLang="es";
    
    public SHPToRDF(String modelDirectory) throws ClassNotFoundException {

        File f = new File(modelDirectory);

        if (f.exists()){
            String[] ficheros = f.list();
            if (ficheros.length>0){
                for (int i=0;i<ficheros.length;i++){
                    File auxFile = new File(modelDirectory+"/"+ficheros[i]);
                    auxFile.delete();
                }
            }
            if (f.delete())
                System.out.println("El directorio " + modelDirectory + " ha sido borrado correctamente");
            else
                System.out.println("El directorio " + modelDirectory + " no se ha podido borrar");
        }

        boolean dirBool = f.mkdir();
    
        model = TDBFactory.createModel(modelDirectory);
        model.removeAll();
        model.setNsPrefix("geontology", nsgeontology);
        model.setNsPrefix("georesource", nsgeoresource);
        model.setNsPrefix("geo", nsgeo);
        model.setNsPrefix("dc", nsdc);
        model.setNsPrefix("xsd", nsxsd);

    }

    public static void main(String[] args) throws Exception {
    	if (args.length == 1){ 
	    	Properties properties = new Properties();

	    	properties.load(new FileInputStream(args[0]));

	    	String inputFile = properties.getProperty("inputFile");
	    	String tempDir = properties.getProperty("tempDir");
			String outputFile = properties.getProperty("outputFile");

			SHPToRDF gr1 = new SHPToRDF(tempDir);
			
			String resourceName = properties.getProperty("resourceName");

			// Namespace parameters
			String namespacePrefix = properties.getProperty("nsPrefix");
			if (emptyString(namespacePrefix))
				namespacePrefix = "georesource";
			String namespace = properties.getProperty("nsURI");
			if (emptyString(namespace))
				namespace = "http://geo.linkeddata.es/resource/";
			String ontologyNSPrefix = properties.getProperty("ontologyNSPrefix");
			if (emptyString(ontologyNSPrefix))
				ontologyNSPrefix = "geontology";
			String ontologyNamespace = properties.getProperty("ontologyNS");
			if (emptyString(ontologyNamespace))
				ontologyNamespace = "http://geo.linkeddata.es/ontology/";

			gr1.model.setNsPrefix(ontologyNSPrefix, ontologyNamespace);
			gr1.model.setNsPrefix(namespacePrefix, namespace);
			gr1.nsgeontology = ontologyNamespace;
			gr1.nsgeoresource = namespace;
			
			
			// Types parameters
			gr1.pointType = properties.getProperty("pointType");
			if (emptyString(gr1.pointType))
				gr1.pointType="http://www.w3.org/2003/01/geo/wgs84_pos#Point";
			gr1.linestringType = properties.getProperty("linestringType");
			if (emptyString(gr1.linestringType))
				gr1.pointType="http://geo.linkeddata.es/ontology/Curva";
			gr1.polygonType = properties.getProperty("polygonType");
			if (emptyString(gr1.polygonType))
				gr1.pointType="http://geo.linkeddata.es/ontology/Pol%C3%ADgono";
			gr1.formBy = properties.getProperty("formBy");
			if (emptyString(gr1.formBy))
				gr1.formBy = "formadoPor";
			
			// Other parameters
			gr1.defaultLang = properties.getProperty("defaultLang");
			if (emptyString(gr1.defaultLang))
				gr1.defaultLang="es";
			
			String featureString = properties.getProperty("featureString");
			String attribute = properties.getProperty("attribute");
			String ignore = properties.getProperty("ignore");
			String type = properties.getProperty("type");
			
			gr1.SHPtoRDF(null, null, inputFile, featureString, outputFile, attribute, ignore,type);
	        
    	} else {
    		System.out.println("Incorrect arguments number. Properties file required.");
    	}
    	
    	/*
         //WatrcrsA
        String modelDirectory = "shp";
        SHPToRDF gr1 = new SHPToRDF(modelDirectory);
        String fileString = "Sample-PAI-Naturel/PAI_ESPACE_NATUREL.SHP";
        String featureString = "PAI_ESPACE_NATUREL";
        String outputFile = "ghis.rdf";
        String attribute = "ID";
        String ignore = "UNK";
        String type = "anormalidad";
        *String modelDirectory = "D://Proyectos/GeoLinkedData/librerias/shp/TDB";
        SHPToRDF gr1 = new SHPToRDF(modelDirectory);
        String fileString = "D://Proyectos/GeoLinkedData/librerias/shp/ESP_Adm0.shp";
        String featureString = "ESP_Adm0";
        String outputFile = "D://Proyectos/GeoLinkedData/librerias/shp/TDB_ESP_Adm0.rdf";
        String attribute = "NAME_SPANI";
        String ignore = "N_P";*/
        /*String modelDirectory = "D://Proyectos/GeoLinkedData/librerias/shp/TDB/";
        SHPToRDF gr1 = new SHPToRDF(modelDirectory);
        String fileString = "D://Proyectos/GeoLinkedData/librerias/shp/ESP_Adm0.shp";
        String featureString = "ESP_Adm0";
        String outputFile = "D://Proyectos/GeoLinkedData/librerias/shp/Test_TDB_ESP_Adm0.rdf";
        String attribute = "NAME_SPANI";
        String ignore = "N_P";*
        gr1.SHPtoRDF(null, null, fileString, featureString, outputFile, attribute, ignore,type);
        //gr1.SHPtoRDF(null, null);
		*/
    }

    private void SHPtoRDF(String source, String target, String fileString, String featureString, String outputFile, String attribute, String ignore, String type) throws MalformedURLException, IOException, NoSuchAuthorityCodeException, FactoryException, TransformException{

        File file = new File(fileString);
        Map map = new HashMap();
        map.put("url", file.toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        FeatureSource featureSource = dataStore.getFeatureSource(featureString);
        FeatureCollection collection = featureSource.getFeatures();
        Iterator iterator = collection.iterator();

        int num = 0;

        while (iterator.hasNext()) {
            SimpleFeatureImpl feature = (SimpleFeatureImpl) iterator.next();
            Geometry o = (Geometry) feature.getDefaultGeometry();

            String namn1 = "featureWithoutName";
            
            if (feature.getAttribute(attribute)!=null)
            	namn1 = feature.getAttribute(attribute).toString();
            
            System.out.println("###################El valor de FEATURENAME2 is -->"+ namn1);

            // Si el elemento tiene de nombre N_P, no sabemos como actuar con el
            //if (!namn1.equals(ignore) && (namn1.startsWith("Río") || namn1.startsWith("Riu") || namn1.startsWith("Rio"))){
            if (!namn1.equals(ignore)){
                String tipo = type;
                //String resource = "España";
                String resource = namn1;
                String defaultLang = "es";
                if (source != null && target != null) {
                    //Quiere decir que hay que transformar
                    CoordinateReferenceSystem sourceCRS = CRS.decode(source);
                    CoordinateReferenceSystem targetCRS = CRS.decode(target);
                    MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
                    Geometry targetGeometry = JTS.transform(o, transform);
                    o = targetGeometry;


                }
                String hash = HashGeometry.getHash(o.toText());
                //URLEncoder.encode("Río"+name, "utf-8");
                String encTipo = URLEncoder.encode(tipo,"utf-8").replace("+", "%20");
                String encResource = URLEncoder.encode(resource,"utf-8").replace("+", "%20");
                String aux = encTipo + "/" + encResource;
                insertarResourceTypeResource(nsgeoresource + aux, nsgeontology + URLEncoder.encode(tipo, "utf-8").replace("+", "%20"));
                defaultLang=detectLang(resource);
                insertarLabelResource(nsgeoresource + aux, resource, defaultLang);
                System.out.println("GeometryType-->"+o.getGeometryType());
                if (o.getGeometryType().equals("Point")) 
                	insertPoint((Point) o,aux);
                else if (o.getGeometryType().equals("LineString"))
                    insertarLineString(aux, hash, o);
                else if (o.getGeometryType().equals("Polygon"))
                    insertarPolygon(aux, hash, o);
                else if (o.getGeometryType().equals("MultiPolygon")){
                    MultiPolygon mp= (MultiPolygon) o;
                    int numero = mp.getNumGeometries();
                    for (int y=0; y<numero; y++){
                        Geometry c = mp.getGeometryN(y);
                        String newHash = HashGeometry.getHash(c.toText());
                        if (c.getGeometryType().equals("Polygon")) {
                            insertarPolygon(aux, newHash, c);
                        } else if (c.getGeometryType().equals("LineString")){
                            insertarLineString(aux, newHash, c);
                        }
                    }
                }
                else if (o.getGeometryType().equals("MultiLineString")){
                    MultiLineString mp= (MultiLineString) o;
                    int numero = mp.getNumGeometries();
                    for (int y=0; y<numero; y++){
                        Geometry c = mp.getGeometryN(y);
                        String newHash = HashGeometry.getHash(c.toText());
                        if (c.getGeometryType().equals("Polygon")) {
                            insertarPolygon(aux, newHash, c);
                        } else if (c.getGeometryType().equals("LineString")){
                            insertarLineString(aux, newHash, c);
                        }
                    }
                }
            }
            else {
                System.out.println("No transformamos el elemento número-->"+num);
            }
            num++;
            /*if (num>2)
                break;*/
        }
        FileOutputStream out = new FileOutputStream(outputFile);
        model.write(out);

    }

    private void insertarLineString(String resource, String hash, Geometry geo){
        insertarResourceTypeResource(nsgeoresource + hash, nsgeontology + "Curva");
        insertarTripletaResource(nsgeoresource + resource, nsgeo + "geometry", nsgeoresource + hash);
        insertarTripletaLiteral(nsgeoresource + hash, nsgeontology + "gml" , geo.toText(), null);
        LineString ls = (LineString) geo;
        insertarCurva(ls, hash);
    }

    private void insertarPolygon(String resource, String hash, Geometry geo) throws UnsupportedEncodingException{
        insertarResourceTypeResource(nsgeoresource + hash, nsgeontology + URLEncoder.encode("Polígono", "utf-8").replace("+", "%20"));
        insertarTripletaResource(nsgeoresource + resource, nsgeo + "geometry", nsgeoresource + hash);
        insertarTripletaLiteral(nsgeoresource + hash, nsgeontology + "gml" , geo.toText(), null);
        Polygon po = (Polygon) geo;
        insertarPoligono(po,hash);
    }

    private void OldSHPtoRDF(String source, String target, String fileString, String featureString, String outputFile, String attribute, String ignore) throws MalformedURLException, IOException, NoSuchAuthorityCodeException, FactoryException, TransformException{

        File file = new File(fileString);
        Map map = new HashMap();
        map.put("url", file.toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        FeatureSource featureSource = dataStore.getFeatureSource(featureString);
        FeatureCollection collection = featureSource.getFeatures();
        Iterator iterator = collection.iterator();

        int num = 0;

        while (iterator.hasNext()) {
            SimpleFeatureImpl feature = (SimpleFeatureImpl) iterator.next();
            Geometry o = (Geometry) feature.getDefaultGeometry();

            String namn1 = feature.getAttribute(attribute).toString();
            System.out.println("###################El valor de FEATURENAME2 is -->"+ namn1);

            // Si el elemento tiene de nombre N_P, no sabemos como actuar con el
            if (!namn1.equals(ignore)){
                String tipo = "Maya";
                //String resource = "España";
                String resource = namn1;
                String defaultLang = "es";
                if (source != null && target != null) {
                    //Quiere decir que hay que transformar
                    CoordinateReferenceSystem sourceCRS = CRS.decode(source);
                    CoordinateReferenceSystem targetCRS = CRS.decode(target);
                    MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
                    Geometry targetGeometry = JTS.transform(o, transform);
                    o = targetGeometry;


                }
                String hash = HashGeometry.getHash(o.toText());
                //URLEncoder.encode("Río"+name, "utf-8");
                String encTipo = URLEncoder.encode(tipo,"utf-8").replace("+", "%20");
                String encResource = URLEncoder.encode(resource,"utf-8").replace("+", "%20");
                String aux = encTipo + "/" + encResource;
                //insertarResourceTypeResource(nsgeoresource + URLEncoder.encode(tipo + "/" + resource, "utf-8").replace("+", "%20"), nsgeontology + URLEncoder.encode(tipo, "utf-8").replace("+", "%20"));
                insertarResourceTypeResource(nsgeoresource + aux, nsgeontology + URLEncoder.encode(tipo, "utf-8").replace("+", "%20"));
                if (o.getGeometryType().equals("LineString"))
                    insertarResourceTypeResource(nsgeoresource + hash, nsgeontology + "Curva");
                else if (o.getGeometryType().equals("Polygon"))
                    insertarResourceTypeResource(nsgeoresource + hash, nsgeontology + URLEncoder.encode("Polígono", "utf-8").replace("+", "%20"));
                else if (o.getGeometryType().equals("MultiPolygon"))
                    insertarResourceTypeResource(nsgeoresource + hash, nsgeontology + URLEncoder.encode("Multipolígono", "utf-8").replace("+", "%20"));
                else
                    insertarResourceTypeResource(nsgeoresource + hash, nsgeontology + o.getGeometryType());

                //insertarTripletaResource(nsgeoresource + URLEncoder.encode(tipo + "/" + resource, "utf-8").replace("+", "%20"), nsgeo + "geometry", nsgeoresource + hash);
                if (o.getGeometryType().equals("MultiPolygon"))
                    insertarTripletaResource(nsgeoresource + aux, nsgeo + "multigeometry", nsgeoresource + hash);
                else
                    insertarTripletaResource(nsgeoresource + aux, nsgeo + "geometry", nsgeoresource + hash);
                //insertarTripletaLiteral(nsgeoresource + hash, nsgml + "gid", o.toText(), null);
                //Mientras se decide que hacer con este campo, pongo un texto normal y corriente
                if (o.getGeometryType().equals("MultiPolygon"))
                    insertarTripletaLiteral(nsgeoresource + hash, nsgeontology + "gml" , "texto para gml en multigeometrias", null);
                else
                    insertarTripletaLiteral(nsgeoresource + hash, nsgeontology + "gml" , o.toText(), null);


                //insertarLabelResource(nsgeoresource + URLEncoder.encode(tipo + "/" + resource, "utf-8").replace("+", "%20"), resource);
                defaultLang=detectLang(resource);
                insertarLabelResource(nsgeoresource + aux, resource, defaultLang);
                if (o.getGeometryType().equals("Polygon")) {
                    Polygon po = (Polygon) o;
                    insertarPoligono(po,hash);
                }
                if (o.getGeometryType().equals("MultiPolygon")) {
                    MultiPolygon mp= (MultiPolygon) o;
                    int numero = mp.getNumGeometries();
                    for (int y=0; y<numero; y++){
                        Geometry c = mp.getGeometryN(y);
                         String newHash = HashGeometry.getHash(c.toText());
                         insertarTripletaResource(nsgeoresource + hash, nsgeo + "geometry", nsgeoresource + newHash);
                         insertarTripletaLiteral(nsgeoresource + newHash, nsgeontology + "gml" , c.toText(), null);
                        if (c.getGeometryType().equals("Polygon")) {
                            insertarResourceTypeResource(nsgeoresource + newHash, nsgeontology + URLEncoder.encode("Polígono", "utf-8").replace("+", "%20"));
                            Polygon po = (Polygon) c;
                            insertarPoligono(po,newHash);

                        } else if (c.getGeometryType().equals("LineString")){
                            LineString ls = (LineString) c;
                            insertarCurva(ls,newHash);
                        }
                    }

                }

                if (o.getGeometryType().equals("LineString")) {
                    LineString ls = (LineString) o;
                    insertarCurva(ls,hash);
                }
            }
            else {
                System.out.println("No transformamos el elemento número-->"+num);
            }
            num++;
        }
        FileOutputStream out = new FileOutputStream(outputFile);
        model.write(out);
        
    }

    private void SHPtoRDF(String source, String target) throws MalformedURLException, IOException, NoSuchAuthorityCodeException, FactoryException, TransformException{

        File file = new File("D://Proyectos/GeoLinkedData/librerias/ERM/WatrcrsA.shp");
        Map map = new HashMap();
        map.put("url", file.toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        FeatureSource featureSource = dataStore.getFeatureSource("WatrcrsA");
        FeatureCollection collection = featureSource.getFeatures();
        Iterator iterator = collection.iterator();

        int num = 0;

        while (iterator.hasNext()) {
            SimpleFeatureImpl feature = (SimpleFeatureImpl) iterator.next();
            Geometry o = (Geometry) feature.getDefaultGeometry();
            //String tipo = "";
            //String resource = "";
            String featureName = feature.getAttribute("NAMA1").toString();
            String featureName2 = feature.getAttribute("NAMN1").toString();
            Object attribute = feature.getAttribute("NAMA1");
            System.out.println("###################El valor de FEATURENAME is -->"+ featureName);
            System.out.println("###################El valor de FEATURENAME2 is -->"+ featureName2);
            System.out.println("###################La clase de FEATURENAME is -->"+ attribute.getClass().getName().toString());
            String tipo = "Maya";
            //String resource = "España";
            String resource = featureName2;
            String defaultLang = "es";
            if (source != null && target != null) {
                //Quiere decir que hay que transformar
                CoordinateReferenceSystem sourceCRS = CRS.decode(source);
                CoordinateReferenceSystem targetCRS = CRS.decode(target);
                MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
                Geometry targetGeometry = JTS.transform(o, transform);
                o = targetGeometry;


            }
            String hash = HashGeometry.getHash(o.toText());
            //URLEncoder.encode("Río"+name, "utf-8");
            String encTipo = URLEncoder.encode(tipo,"utf-8").replace("+", "%20");
            String encResource = URLEncoder.encode(resource,"utf-8").replace("+", "%20");
            String aux = encTipo + "/" + encResource;
            //insertarResourceTypeResource(nsgeoresource + URLEncoder.encode(tipo + "/" + resource, "utf-8").replace("+", "%20"), nsgeontology + URLEncoder.encode(tipo, "utf-8").replace("+", "%20"));
            insertarResourceTypeResource(nsgeoresource + aux, nsgeontology + URLEncoder.encode(tipo, "utf-8").replace("+", "%20"));
            if (o.getGeometryType().equals("LineString"))
                insertarResourceTypeResource(nsgeoresource + hash, nsgeontology + "Curva");
            else if (o.getGeometryType().equals("Polygon"))
                insertarResourceTypeResource(nsgeoresource + hash, nsgeontology + URLEncoder.encode("Polígono", "utf-8").replace("+", "%20"));
            else if (o.getGeometryType().equals("MultiPolygon"))
                insertarResourceTypeResource(nsgeoresource + hash, nsgeontology + URLEncoder.encode("Multipolígono", "utf-8").replace("+", "%20"));
            else
                insertarResourceTypeResource(nsgeoresource + hash, nsgeontology + o.getGeometryType());

            //insertarTripletaResource(nsgeoresource + URLEncoder.encode(tipo + "/" + resource, "utf-8").replace("+", "%20"), nsgeo + "geometry", nsgeoresource + hash);
            if (o.getGeometryType().equals("MultiPolygon"))
                insertarTripletaResource(nsgeoresource + aux, nsgeo + "multigeometry", nsgeoresource + hash);
            else
                insertarTripletaResource(nsgeoresource + aux, nsgeo + "geometry", nsgeoresource + hash);
            //insertarTripletaLiteral(nsgeoresource + hash, nsgml + "gid", o.toText(), null);
            //Mientras se decide que hacer con este campo, pongo un texto normal y corriente
            if (o.getGeometryType().equals("MultiPolygon"))
                insertarTripletaLiteral(nsgeoresource + hash, nsgeontology + "gml" , "texto para gml en multigeometrias", null);
            else
                insertarTripletaLiteral(nsgeoresource + hash, nsgeontology + "gml" , o.toText(), null);


            //insertarLabelResource(nsgeoresource + URLEncoder.encode(tipo + "/" + resource, "utf-8").replace("+", "%20"), resource);
            defaultLang=detectLang(resource);
            insertarLabelResource(nsgeoresource + aux, resource, defaultLang);
            if (o.getGeometryType().equals("Polygon")) {
                Polygon po = (Polygon) o;
                insertarPoligono(po,hash);
                /*int i = 0;
                for (Coordinate c : po.getCoordinates()) {//Si queremos tratar la Z
                    //insertarTripletaResource(nsgeoresource + hash, nsdc + "hasPart", nsgeoresource + "wgs84/" + c.y + "_" + c.x);
                    insertarTripletaResource(nsgeoresource + hash, nsgeontology + "formadoPor", nsgeoresource + "wgs84/" + c.y + "_" + c.x);
                    insertarResourceTypeResource(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeo + "Point");
                    //insertarTripletaLiteral(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeo + "lat", String.valueOf(c.y), XSDDatatype.XSDdecimal);
                    //insertarTripletaLiteral(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeo + "long", String.valueOf(c.x), XSDDatatype.XSDdecimal);
                    insertarTripletaLiteral(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeo + "lat", String.valueOf(c.y), XSDDatatype.XSDdouble);
                    insertarTripletaLiteral(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeo + "long", String.valueOf(c.x), XSDDatatype.XSDdouble);
                    insertarTripletaLiteral(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeontology + "orden", String.valueOf(i), XSDDatatype.XSDint);

                    i++;
                }*/
            }
            if (o.getGeometryType().equals("MultiPolygon")) {
                MultiPolygon mp= (MultiPolygon) o;
                int numero = mp.getNumGeometries();
                /*if (numero>2)
                    numero = 1;*/
                for (int y=0; y<numero; y++){
                    Geometry c = mp.getGeometryN(y);
                     String newHash = HashGeometry.getHash(c.toText());
                     insertarTripletaResource(nsgeoresource + hash, nsgeo + "geometry", nsgeoresource + newHash);
                     insertarTripletaLiteral(nsgeoresource + newHash, nsgeontology + "gml" , c.toText(), null);
                    if (c.getGeometryType().equals("Polygon")) {
                        insertarResourceTypeResource(nsgeoresource + newHash, nsgeontology + URLEncoder.encode("Polígono", "utf-8").replace("+", "%20"));
                        //System.out.println(y + " es un Polygon");
                        Polygon po = (Polygon) c;
                        insertarPoligono(po,newHash);
                        int j = 0;
                        /*for (Coordinate co : po.getCoordinates()) {//Si queremos tratar la Z
                            //System.out.println("El punto "+j+" del polygono "+y+","+num);
                            insertarTripletaResource(nsgeoresource + newHash, nsgeontology + "formadoPor", nsgeoresource + "wgs84/" + co.y + "_" + co.x);
                            insertarResourceTypeResource(nsgeoresource + "wgs84/" + co.y + "_" + co.x, nsgeo + "Point");
                            insertarTripletaLiteral(nsgeoresource + "wgs84/" + co.y + "_" + co.x, nsgeo + "lat", String.valueOf(co.y), XSDDatatype.XSDdouble);
                            insertarTripletaLiteral(nsgeoresource + "wgs84/" + co.y + "_" + co.x, nsgeo + "long", String.valueOf(co.x), XSDDatatype.XSDdouble);
                            insertarTripletaLiteral(nsgeoresource + "wgs84/" + co.y + "_" + co.x, nsgeontology + "orden", String.valueOf(j), XSDDatatype.XSDint);

                            j++;
                        }*/
                    } else if (c.getGeometryType().equals("LineString")){
                        //System.out.println(y + " es un LineString");
                        LineString ls = (LineString) c;
                        insertarCurva(ls,newHash);
                        /*for (int j = 0; j < ls.getNumPoints(); j++) { //puntos de la geometria X,Y
                            Point p = ls.getPointN(j);
                            //System.out.println("El punto "+j+" de la curva "+y+","+num);
                            insertarTripletaResource(nsgeoresource + newHash, nsgeontology + "formadoPor", nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
                            insertarResourceTypeResource(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "Point");
                            insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "lat", String.valueOf(p.getY()), XSDDatatype.XSDdouble);
                            insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "long", String.valueOf(p.getX()), XSDDatatype.XSDdouble);
                            insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeontology + "order", String.valueOf(j), XSDDatatype.XSDint);
                        }*/
                    }
                }

            }

            if (o.getGeometryType().equals("LineString")) {
                LineString ls = (LineString) o;
                insertarCurva(ls,hash);
                /*for (int i = 0; i < ls.getNumPoints(); i++) { //puntos de la geometria X,Y
                    Point p = ls.getPointN(i);
                    //System.out.println(p.getX() + "," + p.getY());
                    //insertarTripletaResource(nsgeoresource + hash, nsdc + "hasPart", nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
                    insertarTripletaResource(nsgeoresource + hash, nsgeontology + "formadoPor", nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
                    insertarResourceTypeResource(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "Point");
                    //insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "lat", String.valueOf(p.getY()), XSDDatatype.XSDdecimal);
                    //insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "long", String.valueOf(p.getX()), XSDDatatype.XSDdecimal);
                    insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "lat", String.valueOf(p.getY()), XSDDatatype.XSDdouble);
                    insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "long", String.valueOf(p.getX()), XSDDatatype.XSDdouble);
                    insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeontology + "order", String.valueOf(i), XSDDatatype.XSDint);

//                Literal l = model.createTypedLiteral("20", XSDDatatype.XSDint);
//                Resource r = model.createResource(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
//                Property p1 = model.createProperty(nsgeontology + "order");
//                r.addLiteral(p1, l);

                    //insertarTripletaLiteral(nsgeontology+"order",nsrdf+"datatype","&xsd;integer");
                }
                for (Coordinate c : ls.getCoordinates()) {//Si queremos tratar la Z
                }*/
            }
            //break;
            num++;
        }
        FileOutputStream out = new FileOutputStream("D://Proyectos/GeoLinkedData/librerias/ERM/TDB_WatrcrsA.rdf");
        model.write(out);
        
    }

    private void insertarResourceTypeResource(String r1, String r2) {

        Resource resource1 = model.createResource(r1);
        Resource resource2 = model.createResource(r2);
        model.add(resource1, RDF.type, resource2);
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
        //   model.write(System.out);
    }

    private void insertarTripletaResource(String s, String p, String o) {
        //Permite ingresar una tripleta en el rdf
        Resource rGeometry = model.createResource(s);
        Property P = model.createProperty(p);
        Resource r2 = model.createResource(o);
        rGeometry.addProperty(P, r2);
        // model.write(System.out);
    }

    private void insertarLabelResource(String r1, String label, String lang) {

        Resource resource1 = model.createResource(r1);
        model.add(resource1, RDFS.label, model.createLiteral(label, lang));
    }

    private String detectLang(String r1){
        String defaultLang = "es";
        //('Barranco%')('Barranquillo%')('Barranc%')('Barrancu%')('Barranquet%')('Barranqueira%')
        if (r1.startsWith("Riu "))
            defaultLang="ca";
        return defaultLang;
    }

    private void insertarPoligono(Polygon po, String hash){
        //Polygon po = (Polygon) o;
        int i = 0;
        for (Coordinate c : po.getCoordinates()) {//Si queremos tratar la Z
            //insertarTripletaResource(nsgeoresource + hash, nsdc + "hasPart", nsgeoresource + "wgs84/" + c.y + "_" + c.x);
            insertarTripletaResource(nsgeoresource + hash, nsgeontology + "formadoPor", nsgeoresource + "wgs84/" + c.y + "_" + c.x);
            insertarResourceTypeResource(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeo + "Point");
            //insertarTripletaLiteral(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeo + "lat", String.valueOf(c.y), XSDDatatype.XSDdecimal);
            //insertarTripletaLiteral(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeo + "long", String.valueOf(c.x), XSDDatatype.XSDdecimal);
            insertarTripletaLiteral(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeo + "lat", String.valueOf(c.y), XSDDatatype.XSDdouble);
            insertarTripletaLiteral(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeo + "long", String.valueOf(c.x), XSDDatatype.XSDdouble);
            insertarTripletaLiteral(nsgeoresource + "wgs84/" + c.y + "_" + c.x, nsgeontology + "orden", String.valueOf(i), XSDDatatype.XSDint);

            i++;
        }
    }

    private void insertarCurva(LineString ls, String hash){
        for (int i = 0; i < ls.getNumPoints(); i++) { //puntos de la geometria X,Y
            Point p = ls.getPointN(i);
            //System.out.println(p.getX() + "," + p.getY());
            //insertarTripletaResource(nsgeoresource + hash, nsdc + "hasPart", nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
            insertarTripletaResource(nsgeoresource + hash, nsgeontology + "formadoPor", nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
            insertarResourceTypeResource(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "Point");
            //insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "lat", String.valueOf(p.getY()), XSDDatatype.XSDdecimal);
            //insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "long", String.valueOf(p.getX()), XSDDatatype.XSDdecimal);
            insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "lat", String.valueOf(p.getY()), XSDDatatype.XSDdouble);
            insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "long", String.valueOf(p.getX()), XSDDatatype.XSDdouble);
            insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeontology + "order", String.valueOf(i), XSDDatatype.XSDint);

//                Literal l = model.createTypedLiteral("20", XSDDatatype.XSDint);
//                Resource r = model.createResource(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
//                Property p1 = model.createProperty(nsgeontology + "order");
//                r.addLiteral(p1, l);

            //insertarTripletaLiteral(nsgeontology+"order",nsrdf+"datatype","&xsd;integer");
        }
        for (Coordinate c : ls.getCoordinates()) {//Si queremos tratar la Z
        }
    }
    
    private void insertPoint(Point p, String resource){
		insertarTripletaResource(nsgeoresource + resource, nsgeo + "geometry", nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
        //insertarTripletaResource(dtr.nsgeoresource + resource, dtr.nsgeontology + dtr.formBy, dtr.nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX());
        insertarResourceTypeResource(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "Point");
        insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "lat", String.valueOf(p.getY()), XSDDatatype.XSDdouble);
        insertarTripletaLiteral(nsgeoresource + "wgs84/" + p.getY() + "_" + p.getX(), nsgeo + "long", String.valueOf(p.getX()), XSDDatatype.XSDdouble);
    }

    private static boolean emptyString(String text){
    	if (text == null && text == "")
    		return true;
    	else
    		return false;
    }
    
}
