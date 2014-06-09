/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler;

import com.inubit.research.server.InvitationMailer;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.errors.AccessViolationException;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.user.LoginableUser;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handler for the configuration of the mail server that should be used
 * @author fel
 */
public class MailRequestHandler extends AbstractRequestHandler {

    @Override
    public void handleGetRequest( RequestFacade req , ResponseFacade resp ) throws IOException, AccessViolationException {
        int statusCode = 404;

        LoginableUser user = RequestUtils.getCurrentUser(req);
        String requestUri = req.getRequestURI();
        String response = "Resource not found";
        boolean error = true;

        if (!user.isAdmin())
            throw new AccessViolationException("Admin rights are required for this operation"); 

        if (requestUri.matches("/mail")) {
            error = false;
            statusCode = 200;

            Document doc = XMLHelper.newDocument();
            Element mailEl = XMLHelper.addDocumentElement(doc, "mail");

            Properties props = new Properties();
            props.setProperty("host", InvitationMailer.getSmtpHost());
            props.setProperty( "from", InvitationMailer.getMailFrom());

            if (InvitationMailer.getSmtpUser() != null && InvitationMailer.getSmtpPwd() != null) {
                props.setProperty("user", InvitationMailer.getSmtpUser());
                props.setProperty("pwd", InvitationMailer.getSmtpPwd());
            }

            XMLHelper.addPropertyList(doc, mailEl, props);
            ResponseUtils.respondWithXML(resp, doc, statusCode);
            return;
        }

        ResponseUtils.respondWithStatus(statusCode, response, resp, error);
    }

    @Override
    public void handlePostRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handlePutRequest( RequestFacade req , ResponseFacade resp ) throws IOException, AccessViolationException {
        int statusCode = 404;
        LoginableUser user = RequestUtils.getCurrentUser(req);
        String requestUri = req.getRequestURI();
        String response = "Resource not found";
        boolean error = true;

        if (!user.isAdmin())
            throw new AccessViolationException("Admin rights are required for this operation");

        if (requestUri.matches("/mail")) {
            error = false;
            statusCode = 200;
            response = "";

            try {
                Document doc = RequestUtils.getXML(req);

                Map<String, String> props = XMLHelper.parseProperties(doc.getDocumentElement());

                InvitationMailer.initializeSmtpConnection(props.get("host"), props.get("user"), props.get("pwd"));
                InvitationMailer.setMailFrom( props.get("from") );
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
            
        }

        ResponseUtils.respondWithStatus(statusCode, response, resp, error);
    }

    @Override
    public void handleDeleteRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
