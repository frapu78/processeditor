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
 * Handler for login and index pages
 * @author fel
 */
public class RootRequestHandler extends AbstractRequestHandler {

    @Override
    public void handleGetRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        if (RequestUtils.getCurrentUser(req) != null) {
            ResponseUtils.respondWithNegotiatedServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/index.html", resp);
            return;
        }

        ResponseUtils.respondWithServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/login.html", resp, false);
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