/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.persistence;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.config.FileSystemUsersConfig;
import com.inubit.research.server.config.ServerConfig;
import com.inubit.research.server.config.UsersConfig;
import com.inubit.research.server.meta.DirectoryMetaDataHandler;
import com.inubit.research.server.request.XMLHelper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author fel
 */
public class FileSystemConnector extends PersistenceConnector {

    private static final String MODEL_DIR = ProcessEditorServerHelper.SERVER_HOME_DIR + "/models";
    private static final String USER_PIC_DIR = ProcessEditorServerHelper.SERVER_HOME_DIR + "/userpics";
    private static final String MISC_DIR = ProcessEditorServerHelper.SERVER_HOME_DIR + "/misc";
//    private static final String LOGGING_DIR = ProcessEditorServerHelper.SERVER_HOME_DIR + "/log";
//    private static final String CONF_DIR = ProcessEditorServerHelper.SERVER_HOME_DIR + "";
    private static final String META_SUFFIX = ".meta";
    private static final String MAPPING_FILE_NAME = "id-mapping.xml";
    private static final String DB_PROPERTY_FILE_NAME = "database.properties";
    private static final String[] DIRS = {ProcessEditorServerHelper.SERVER_HOME_DIR, MODEL_DIR, USER_PIC_DIR, MISC_DIR };

    private File modelDir;
    private File picDir;
    private DirectoryMetaDataHandler metaHandler;

    private XPathFactory xpathFactory = XPathFactory.newInstance();
    private XPath xpath = xpathFactory.newXPath();
    
    //create directory structure
    static {
        for (String directory : DIRS) {
            File dir = new File(directory);

            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    public FileSystemConnector() {
        super();
        this.modelDir = new File(MODEL_DIR);
        this.picDir = new File(USER_PIC_DIR);
    }

    public static ServerConfig loadServerConfig() {
        File f = new File ( ProcessEditorServerHelper.SERVER_HOME_DIR + "/WebModeler.xml");

        if ( !f.exists() )
            ServerConfig.createServerConfig(f);

        return ServerConfig.fromConfigFile(f);
    }

    public static Properties loadDatabaseProperties() throws IOException {
        File f = new File ( ProcessEditorServerHelper.SERVER_HOME_DIR + "/" + DB_PROPERTY_FILE_NAME );
        Properties props = new Properties();
        props.load( new FileReader(f) );
        return props;
    }

    public static File createFile( String fileName ) {
        File f = new File( MISC_DIR, fileName );
        if ( f.exists() )
            return null;

        return f;
    }

    public static File openFile( String fileName ) {
        File f = new File( MISC_DIR, fileName );
        if ( f.exists() )
            return f;

        return null;
    }
    
    public static void createDir( String dirName ) {
    	File f = new File( MISC_DIR, dirName );
    	if ( !f.exists() )
    		f.mkdirs();
    }
    
    @Override
    public String saveUserImage( String id, ImageType imageType, byte[] pic) {
        
        if ( id == null )
            id = this.getUnusedImageID();
        
        File picFile = new File(this.picDir, id + "." + imageType.toString());

        if (pic != null) {
            try {
                OutputStream fw = new FileOutputStream(picFile);
                fw.write(pic);
                fw.flush();
                fw.close();

                return id;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public String getUnusedImageID() {
        Random rand = new Random(System.currentTimeMillis());

        String[] fileNames = this.picDir.list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg") || name.endsWith(".png");
            }
        });

        Set<String> currIds = new HashSet<String>();
        for (String fName : fileNames) {
            int startIndex = fName.lastIndexOf("/");
            if (startIndex < 0) {
                startIndex = 0;
            }

            int endIndex = fName.lastIndexOf(".");
            if (endIndex < 0) {
                endIndex = fName.length();
            }

            currIds.add(fName.substring(startIndex, endIndex));
        }

        String newId;
        while (currIds.contains((newId = String.valueOf(rand.nextInt()))));

        return newId;
    }

    public Map<String, String> getIDMapping(String uri) {
        Map<String, String> mapping = new HashMap<String, String>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document mappingDoc = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            File mapFile = new File( ProcessEditorServerHelper.SERVER_HOME_DIR + "/" + MAPPING_FILE_NAME );

            if ( mapFile.exists() ) {
                mappingDoc = db.parse(mapFile);
                String query = "/mapping/is[@uri='" + uri + "']/model";
                NodeList nl = (NodeList) this.xpath.evaluate(query, mappingDoc, XPathConstants.NODESET);

                for ( int i = 0; i < nl.getLength(); i++ ) {
                    Node node = nl.item(i);
                    String modelUri = node.getAttributes().getNamedItem("uri").getNodeValue();
                    String id = node.getTextContent();

                    mapping.put( modelUri, id );
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return mapping;
    }

    public void storeIDMapping(String uri, Map<String, String> mapping) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document mappingDoc = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            File mapFile = new File( ProcessEditorServerHelper.SERVER_HOME_DIR + "/" + MAPPING_FILE_NAME );
            if ( mapFile.exists() )
                mappingDoc = db.parse(mapFile);
            else {
                mappingDoc = XMLHelper.newDocument();
                XMLHelper.addDocumentElement(mappingDoc, "mapping");
            }

            Element docEl = mappingDoc.getDocumentElement();
            Element isEl = XMLHelper.addElement(mappingDoc, docEl, "is");
            isEl.setAttribute("uri", uri);

            for ( Map.Entry<String, String> e : mapping.entrySet() ) {
                Element modelEl = XMLHelper.addElement(mappingDoc, isEl, "model");

                modelEl.setAttribute("uri", e.getValue());
                modelEl.setTextContent(e.getKey());
            }

            FileOutputStream fos = new FileOutputStream( mapFile );
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
            ProcessEditorServerUtils.writeXMLtoStream(osw,mappingDoc);
            fos.close();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public void addToIDMapping(String uri, Map<String, String> mapping) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document mappingDoc = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            File mapFile = new File( ProcessEditorServerHelper.SERVER_HOME_DIR + "/" + MAPPING_FILE_NAME );
            if ( mapFile.exists() ) {
                mappingDoc = db.parse(mapFile);

                NodeList nl = (NodeList) this.xpath.evaluate("/mapping/is[@uri='" + uri + "']", mappingDoc, XPathConstants.NODESET);

                if ( nl.getLength() > 0 ) {
                    Node n = nl.item(0);

                    for ( Map.Entry<String, String> e : mapping.entrySet() ) {
                        Element modelEl = XMLHelper.addElement(mappingDoc, (Element) n, "model");
                        modelEl.setAttribute("uri", e.getValue());
                        modelEl.setTextContent(e.getKey());
                    }

                    FileOutputStream fos = new FileOutputStream( mapFile );
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
                    ProcessEditorServerUtils.writeXMLtoStream(osw,mappingDoc);
                    fos.close();

                    return;
                }
            }

            //if the file does not exist or no iS with the given URI exists, create one
            this.storeIDMapping(uri, mapping);
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public Set<String> getAllMappedIDs() {
        Set<String> ids = new HashSet<String>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document mappingDoc = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            File mapFile = new File( ProcessEditorServerHelper.SERVER_HOME_DIR + "/" + MAPPING_FILE_NAME );

            if ( mapFile.exists() ) {
                mappingDoc = db.parse(mapFile);

                NodeList nl = (NodeList) this.xpath.evaluate("//model", mappingDoc, XPathConstants.NODESET);
                for ( int i = 0; i < nl.getLength(); i++ ) {
                    ids.add( nl.item(i).getTextContent());
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }       

        return ids;
    }

    public UsersConfig loadUsersConfig() throws Exception {
        return FileSystemUsersConfig.fromConfigFile(new File(ProcessEditorServerHelper.SERVER_HOME_DIR + "/users.xml"));
    }

    public BufferedImage loadUserImage(String id) {
        String[] extensions = {".png", ".PNG", ".jpg", ".JPG"};

        File pic = null;

        for (String ext : extensions) {
            pic = new File(this.picDir, id + ext);
            if (pic.exists()) {
                break;
            }
        }

        if (pic != null) {
            try {
                return ImageIO.read(pic);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }
}
