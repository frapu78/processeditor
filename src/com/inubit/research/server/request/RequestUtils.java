/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.manager.TemporaryKeyManager;
import com.inubit.research.server.multipart.SimpleMultipartParser;
import com.inubit.research.server.request.handler.UserRequestHandler;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.TemporaryUser;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;

/**
 *
 * @author fel
 */
public class RequestUtils {
    public static boolean checkForRedirect( RequestFacade req , ResponseFacade resp ) throws IOException {
        //for workbench
        if (req.getRequestURI().matches("/models/\\d+/versions/\\d+\\?key=.+?")) {
            Pattern p = Pattern.compile("/models/(\\d+)/versions/(\\d+)\\?key=(.+)");
            Matcher m = p.matcher(req.getRequestURI());

            if (m.find()) {
                String id = m.group(1);
                String version = m.group(2);
                String key = m.group(3);

                TemporaryUser tu = TemporaryKeyManager.checkKey(key, id, version);
                if (tu != null) {
                    String sessionID = ProcessEditorServerHelper.getUserManager().login(key, tu );
                    resp.addCookie(UserRequestHandler.SESSION_ATTRIBUTE, sessionID, "/" );

                    return false;
                }
            }
        } else if ( getCurrentUser( req ) == null ) {
            resp.setHeader( HttpConstants.HEADER_KEY_LOCATION, req.getContext()+ "/?redirect=" + req.getContext() + req.getRequestURI() );
            ResponseUtils.respondWithStatus(307, "", resp, false);
            return true;
        } else {
            return false;
        }

        return true;
    }

    public static Document getXML( RequestFacade req ) throws Exception {
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        Document doc = builder.parse( req.getInputStream() );

        return doc;
    }

    public static JSONObject getJSON( RequestFacade req ) throws IOException, JSONException {
        JSONTokener tokener = new JSONTokener( new InputStreamReader( req.getInputStream()) );
        return new JSONObject( tokener );
    }

    public static Map<String, String> getQueryParameters( RequestFacade req ) {
        String query = req.getQuery();
        return getQueryParameters(query);
    }
    
    public static Map<String, String> getQueryParameters(String reqPath ) {
        String query = reqPath;
        Map<String, String> params = new HashMap<String, String>();
        if(query != null) {
	        String[] parts = query.split("\\&");

	        for (String part : parts) {
	        	if(part.contains("=")) {
	        		String[] _values = part.split("=");
	        		if(_values.length >= 2) {
	        			params.put(_values[0], _values[1]);
	        		}
	        	}
	        }
        }
        return params;
    }

    public static LoginableUser getCurrentUser( RequestFacade req ) {
        return ProcessEditorServerHelper.getUserManager().getUserForRequest(req);
    }
    
    public static String getContent( RequestFacade req ) throws IOException {
        InputStream in = req.getInputStream();
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
    
    public static File parseMultiPartItemIntoTmpFile( RequestFacade req, String itemName ) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(req.getInputStream());

        SimpleMultipartParser smp = new SimpleMultipartParser();

        File upDir = new File(ProcessEditorServerHelper.TMP_DIR);

        if (!upDir.exists()) {
            upDir.mkdir();
        }

        File newFile = new File(ProcessEditorServerHelper.TMP_DIR + "/model" + upDir.list().length);

        OutputStream os = new FileOutputStream( newFile );

        byte[] content = smp.parseItemContentAsByteArray(bis, itemName);
        InputStream is = new ByteArrayInputStream( content );

        byte[] buf = new byte[2048];
        int length;
        while((length = is.read(buf)) > -1)
            os.write(buf, 0, length);


        os.flush();
        os.close();
        
        return newFile;
    }
}