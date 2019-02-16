/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.model;

import com.inubit.research.ISConverter.exporter.ISBPDExporter;
import com.inubit.research.ISConverter.importer.ISDiagramImporter;
import com.inubit.research.client.XmlHttpRequest;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.request.XMLHelper;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.UnknownHostException;

import com.inubit.research.testUtils.Base64Utils;
import net.frapu.code.converter.ConverterHelper;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.TextAnnotation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author fel, fpu, ff
 */
public class ISServerModel implements ServerModel {

    private ProcessModel model = null;
    private ISLocation host;

    private URI serverURI;
    private String modelName;
    private String group;
    private String userName;
    private String password;
    private String type;
    private String md5;
    private String comment;

    public ISServerModel(URI serverURI, String modelName, String group, String type,String user, String password, String md5, String comment) {
        this.serverURI = serverURI;
        this.modelName = modelName;
        this.group = group;
        this.userName = user;
        this.password = password;
        this.type = type;
        this.md5 = md5;
        this.comment = comment;
    }

    @Override
    public ProcessModel getModel() {
        // Check if already loaded
        if (model != null) {
            return model;
        }
        // Try to fetch model from cache
        model = ProcessEditorServerHelper.getPersistenceConnector().fetchFromModelCache(getModelName() + "." + getChecksum());
        if (model == null) {
            if (refresh()) {
                // Store updated model in cache
                ProcessEditorServerHelper.getPersistenceConnector().addToModelCache(getModelName() + "." + getChecksum(), model);
            }
        }
        return model;
    }

    @Override
    public ServerModel save( ProcessModel m, int version, String modelId, String comment, String folder ) {
        try {
            String group = folder.substring( folder.lastIndexOf("/") + 1 );
            if ( group.isEmpty() )
                group = "ProcessEditorExport";

            m.setProperty( ISDiagramImporter.PROP_GROUP, group );
            this.setModelGroup(group);
            File temp = new File( ProcessEditorServerHelper.TMP_DIR + "/" + "tmp" + modelId);
            if (temp.exists()) {
                temp.delete();
            }
            ISBPDExporter _exporter = new ISBPDExporter();
            _exporter.serialize(temp, m);
            //writing back to server
            FileInputStream _fis = new FileInputStream(temp);
            byte _fileContent[] = new byte[(int) temp.length()];
            _fis.read(_fileContent);
            String _base64 = Base64Utils.encode(_fileContent).toString();

            Document doc = this.buildSaveXML(_base64, comment);
            //REST request
            XmlHttpRequest request = new XmlHttpRequest( this.serverURI );
            
            request.executePostRequest(doc);
            System.out.println("Saving ProcessModel on IS - Status: " + request.getLastStatus() );

            if ( request.getLastStatus() == 201 ) {
                String uri = this.serverURI.toASCIIString();
                String base = uri.substring(0, uri.indexOf("/models"));
                uri = base + "/models/" + m.getProcessName().replace(" ", "%20");
                this.serverURI = new  URI( this.encodeCredentials(uri));
                this.model = m;
                return this;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

//    public String getChecksum() {
//        return this.md5;
//    }
//
    public URI getServerURI() {
        return this.serverURI;
    }
//
//    public String getType() {
//        return this.type;
//    }
//
//    public String getModelName() {
//        return this.modelName;
//    }
//
    public String getModelGroup() {
        return this.group;
    }
//
//    public String getUsername() {
//        return this.userName;
//    }
//
//    public String getPassword() {
//        return this.password;
//    }
//
//    public String getComment() {
//        return this.comment;
//    }
//
//    public void setServerURI( URI newUri ) {
//        this.this.serverURI = newUri;
//    }
//
    public void setModelGroup( String group ) {
        this.group = group;
    }

    @Override
    public void delete() {
        XmlHttpRequest req = new XmlHttpRequest(this.serverURI);
        try {
            req.executeDeleteRequest();
            System.out.println("Deletion returned: " + req.getLastStatus() );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean refresh() {
        try {
            // Fetch model from server here
            try {
                model = ConverterHelper.importModels(this.serverURI).get(0);
            } catch ( UnknownHostException ex ) {
                //this is a (nasty, but necessary) work-around for fel's development purposes
                String host = this.serverURI.getHost();
                String newHost = host + ".intra.inubit.com";
                String uriString = this.serverURI.toString().replaceFirst(host, newHost);
                this.serverURI = new URI(uriString);
                model = ConverterHelper.importModels(this.serverURI).get(0);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            BPMNModel _error = new BPMNModel();
            TextAnnotation _t = new TextAnnotation();
            _t.setText("Error fetching diagram '" + this.modelName + "'.");
            _t.setPos(200, 100);
            _t.setSize(200, 50);
            _error.addNode(_t);
            model = _error;
            return false;
        }
    }

    @Override
    public String getModelName() {
        return this.modelName;
    }

    @Override
    public String getChecksum() {
        return this.md5;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public String getAuthor() {
        return this.userName + "@" + this.serverURI.getHost();
    }

    @Override
    public String getCreationDate() {
        return "n/a";
    }

    @Override
    public String getLastUpdateDate() {
        return "n/a";
    }

    private String encodeCredentials(String uri) {
        // Check if user or password is already appended
        if (uri.contains("user=")) {
            return uri;
        }
        return uri + "?user=" + this.userName + "&password=" + this.password;
    }

    private Document buildSaveXML( String base64model, String comment ) {
        Document doc = XMLHelper.newDocument();

        Element docEl = XMLHelper.addDocumentElement(doc, "model");
        XMLHelper.addElement(doc, docEl, "user").setTextContent( this.userName );
        XMLHelper.addElement(doc, docEl, "comment").setTextContent(comment);
        XMLHelper.addElement(doc, docEl, "workflow").setTextContent(base64model);

        return doc;
    }
}
