/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.config;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.manager.FileSystemLocation;
import com.inubit.research.server.manager.IntegratedUserManager;
import com.inubit.research.server.manager.Location;
import com.inubit.research.server.manager.UserManager;
import com.inubit.research.server.persistence.DatabaseConnector;
import com.inubit.research.server.persistence.FileSystemConnector;
import com.inubit.research.server.persistence.PersistenceConnector;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author fel
 */
public class ServerConfig {
    private static final XPathFactory xpathFactory = XPathFactory.newInstance();
    private static final XPath xpath = xpathFactory.newXPath();

    private Document document;

    private UserManager userManager;
    private PersistenceConnector persistenceConnector;
    private DatabaseConnector databaseConnector;

    private ServerConfig() {}

    public String getStartUpOtion( String name ) {
        String query = "//startup/option[@name='" + name + "']";
        try {
            Node n = (Node) xpath.evaluate(query, document, XPathConstants.NODE);
            return n != null ? n.getTextContent() : null;
        } catch ( Exception ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public String getNodeImageFileName( Class c ) {
        String query = "//node-images/node[@class='" + c.getName() + "']";
        try {
            Node n = (Node) xpath.evaluate(query, document, XPathConstants.NODE);

            return n != null ? n.getTextContent() : null;
        } catch ( Exception ex ) {
            return null;
        }
    }

    public UserManager getUserManager( ) {
        if ( this.userManager == null ) {
            String query = "//components/component[@type='user']";

            try {
                Node n = ( Node ) xpath.evaluate( query, document, XPathConstants.NODE );
                
                Class<? extends UserManager> c = (Class<? extends UserManager>) Class.forName( n.getTextContent() );

                this.userManager = c.newInstance();
            } catch ( Exception ex ) {
                this.userManager = IntegratedUserManager.getInstance();
            }
        }

        return this.userManager;
    }


     public UsersConfig loadUsersConfig() {
        String query = "//components/component[@type='usersconfig']";

        try {
            Node n = ( Node ) xpath.evaluate( query, document, XPathConstants.NODE );

            Class<? extends UsersConfig> c = (Class<? extends UsersConfig>) Class.forName( n.getTextContent() );

            return c.newInstance();
        } catch ( Exception ex ) {
            try {
                return FileSystemUsersConfig.fromConfigFile( new File(ProcessEditorServerHelper.SERVER_HOME_DIR + "/users.xml") );
            } catch ( Exception e ) {
//                  e.printStackTrace();
                  return null;
            }
        }
    }

    public PersistenceConnector getPersistenceConnector( ) {
        if ( this.persistenceConnector == null ) {
            String query = "//components/component[@type='persistence']";

            try {
                Node n = ( Node ) xpath.evaluate( query, document, XPathConstants.NODE );

                Class<? extends PersistenceConnector> c = (Class<? extends PersistenceConnector>) Class.forName( n.getTextContent() );

                this.persistenceConnector = c.newInstance();
            } catch ( Exception ex ) {
//                ex.printStackTrace();
                this.persistenceConnector = new FileSystemConnector();
            }
        }

        return this.persistenceConnector;
    }

    public DatabaseConnector getDatabaseConnector( ) {
        if ( this.databaseConnector == null ) {
            String query = "//components/component[@type='database']";

            try {
                Node n = ( Node ) xpath.evaluate( query, document, XPathConstants.NODE );

                Class<? extends DatabaseConnector> c = (Class<? extends DatabaseConnector>) Class.forName( n.getTextContent() );

                this.databaseConnector = c.newInstance();
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
        }

        return this.databaseConnector;
    }

    public Class<? extends Location> getDefaultLocationClass() {
        String query = "//components/component[@type='defaultlocation']";
        try {
            Node n = ( Node ) xpath.evaluate( query, document, XPathConstants.NODE );

            return (Class<? extends Location>) Class.forName( n.getTextContent() );
        } catch ( Exception ex ) {
//            ex.printStackTrace();
            return FileSystemLocation.class;
        }
    }

    public static void createServerConfig( File file ) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write("<config>");
            fw.write(   "<startup>");
            fw.write(       "<option name='ServerPortletMode'>false</option>");
            fw.write(   "</startup>");
            fw.write(   "<components>");
            fw.write(           "<component type='user'>com.inubit.research.server.manager.IntegratedUserManager</component>");
            fw.write(           "<!--<component type='user'>com.inubit.research.server.manager.LiferayUserManager</component>-->");
            fw.write(   "</components>");
            fw.write(   "<node-images>");
            fw.write(       "<!--");
            fw.write(           "<node class='net.frapu.code.visualization.bpmn.Task'>/pic/task.png</node>");
            fw.write(       "-->");
            fw.write(   "</node-images>");
            fw.write("</config>");

            fw.close();
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
    }

    public static ServerConfig fromConfigFile( File file ) {
        if ( !file.exists() )
            return null;

        try {
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            xmlFactory.setNamespaceAware(false);
            DocumentBuilder builder = xmlFactory.newDocumentBuilder();

            ServerConfig sc = new ServerConfig();
            sc.document = builder.parse(file);

            return sc;
        } catch ( Exception ex ) {
            ex.printStackTrace();
            return null;
        }
    }
}
