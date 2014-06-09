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
import com.inubit.research.server.errors.AccessViolationException;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;

import java.io.IOException;

/**
 * This handler returns the ProcessEditor website.
 *
 * @author fpu
 */
public class EditorRequestHandler extends AbstractRequestHandler{

    @Override
    public void handleGetRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        if (RequestUtils.checkForRedirect( req, resp ))
            return;

        ResponseUtils.respondWithNegotiatedServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/ProcessEditorRaphael.html", resp);
    }

    @Override
    public void handlePostRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handlePutRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleDeleteRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
