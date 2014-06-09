/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.errors.AccessViolationException;
import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.multipart.MultiPartObject;
import com.inubit.research.server.multipart.SimpleMultipartParser;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handler for handling requests about model locations
 * @author fel
 */
public class PersistentModelLocationsRequestHandler extends PersistentModelRequestHandler {

    @Override
    public void handleGetRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        int statusCode = 200;
        String requestUri = req.getRequestURI();
        LoginableUser user = RequestUtils.getCurrentUser(req);

        if (requestUri.matches("/models/locations")) {
            Document doc = XMLHelper.newDocument();
            Element locationsEl = XMLHelper.addDocumentElement(doc, "locations");
            this.addFolderStructure(( SingleUser ) user, doc, locationsEl);
            ResponseUtils.respondWithXML(resp, doc, statusCode);
            return;
        } else if (requestUri.matches("/models/locations\\?user=.+")) {
            String userName = RequestUtils.getQueryParameters(req).get("user");

            SingleUser u;
            if ( userName.equals("this") )
                u = ( SingleUser ) user;
            else
                u = ProcessEditorServerHelper.getUserManager().getUserForName(userName);

            Set<String> locs = this.modelManager.getLocationsForUser(u);
            Document doc = XMLHelper.newDocument();

            Element locationsEl = XMLHelper.addDocumentElement(doc, "locations");
            int i = 0;
            
            for ( String s : locs ) {
                Element locEl = XMLHelper.addElement(doc, locationsEl, "location");
                XMLHelper.addElement(doc, locEl, "id").setTextContent( String.valueOf(i++) );
                XMLHelper.addElement(doc, locEl, "name").setTextContent(s);
            }

            ResponseUtils.respondWithXML(resp, doc, statusCode);
            return;
        } else if ( requestUri.matches( "/models/locations\\?rootpath=.+" )) {
            String rootpath = RequestUtils.getQueryParameters(req).get("rootpath");

            this.modelManager.updateLocationAtPath( rootpath );
            ResponseUtils.respondWithStatus(statusCode, "<updated>" + rootpath + "</updated>", resp, false);
            return;
        }

        ResponseUtils.respondWithStatus(404, "Resource not found!", resp, true);
    }

    @Override
    public void handlePostRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        int statusCode = 200;
        String requestUri = req.getRequestURI();
        LoginableUser user = RequestUtils.getCurrentUser(req);
        String response = "";

        if (requestUri.matches("/models/locations")) {
            //add a new location or move an existing one
            try {
                String contentType = req.getHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE);

                if (contentType.contains(HttpConstants.CONTENT_TYPE_MULTIPART)) {
                    //if multipart was delivered --> create new ISLocation from form data
                    StringBuilder b = new StringBuilder(300);
                    BufferedReader r = new BufferedReader(new InputStreamReader(req.getInputStream()));
                    String l;

                    while ((l = r.readLine()) != null) {
                        b.append(l);
                        b.append("\n");
                    }

                    //get connection components from multipart
                    MultiPartObject m = new SimpleMultipartParser().parseSource(b.toString());
                    String usr = m.getItemByName("username").getContent().trim();
                    String pwd = m.getItemByName("passwd").getContent().trim();
                    String url = m.getItemByName("isurl").getContent().trim();
    
                    ISLocation ism = new ISLocation(URI.create(url), usr, pwd);

                    //try to connect
                    if (ism.checkConnection() && modelManager.addISConnection(ism, (SingleUser) user)) {
                        response = "{success:true}";
                    } else {
                        response = "{success:false}";
                        statusCode = 500;
                    }
                } 
            } catch (Exception e) {
                response = "{success:false}";
                statusCode = 500;
                e.printStackTrace();
            }
        }

        if (response.equals("")) {
            statusCode = 500;
            response = "An error occured";
        }
        ResponseUtils.respondWithStatus(statusCode, response,HttpConstants.CONTENT_TYPE_TEXT_HTML, resp, false);
    }

    @Override
    public void handlePutRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        if (req.getRequestURI().matches("/models/locations")) {
            try {
                Document doc = RequestUtils.getXML(req);
                Map<String, String> props = XMLHelper.parseProperties(doc.getDocumentElement());

                String target = props.get("target");
                String source = props.get("source");

                boolean moved = modelManager.moveLocation(source, target, (SingleUser) RequestUtils.getCurrentUser(req));

                String response = "<update>" + moved + "</update>";
                ResponseUtils.respondWithStatus(200, response, resp, false);
            } catch (Exception ex ) {
                ResponseUtils.respondWithStatus(500, "Error while parsing XML.", resp, true);
            }
        }
    }

    @Override
    public void handleDeleteRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        String response = "";
        LoginableUser user = RequestUtils.getCurrentUser(req);
        
        if (req.getRequestURI().matches("/models/locations")) {
            try {
                Document doc = RequestUtils.getXML(req);
                Map<String, String> props = XMLHelper.parseProperties(doc.getDocumentElement());
                response = "<delete>" + modelManager.removePersistentLocation(props.get("location"), (SingleUser) user) + "</delete>";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ResponseUtils.respondWithStatus(200, response, resp, false);
    }
}
