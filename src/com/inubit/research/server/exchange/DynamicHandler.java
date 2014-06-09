/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.exchange;

import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.errors.AccessViolationException;
import com.inubit.research.server.request.ExchangeRequestFacade;
import com.inubit.research.server.request.ExchangeResponseFacade;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.handler.AbstractRequestHandler;
import com.inubit.research.server.user.LoginableUser;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author fel
 */
public class DynamicHandler extends AbstractHandler {

    protected static Logger logger;

    private boolean authenticationRequired = false;

    private AbstractRequestHandler handler;

    public DynamicHandler(AbstractRequestHandler arh, String contextUri) {
        this.handler = arh;
        this.contextUri = contextUri;
        this.logger = Logger.getLogger(arh.getClass().getName());
    }

    public DynamicHandler(boolean authenticate, AbstractRequestHandler arh, String contextUri) {
        this(arh, contextUri);
        this.authenticationRequired = authenticate;
    }

    public void handle(HttpExchange t) {
        String requestUri = t.getRequestURI().toASCIIString();
        LoginableUser user = null;

        RequestFacade req = new ExchangeRequestFacade(t);
        ResponseFacade resp = new ExchangeResponseFacade(t);
        try {
            if (this.authenticationRequired) {
                if (RequestUtils.checkForRedirect(req, resp)) {
                    return;
                }

//                Map<String, String> cookie = ProcessEditorServerUtils.parseCookie(t);
//
//                Map<String, String> queryParams = ProcessEditorServerUtils.parseQueryParameters(t.getRequestURI().getQuery());
//
//                if (queryParams.get(UserHandler.SESSION_ATTRIBUTE) != null) {
//                    user = UserManager.getInstance().getUserForSession(queryParams.get(UserHandler.SESSION_ATTRIBUTE));
//                } else if (queryParams.get("key") != null) {
//                    user = UserManager.getInstance().getUserForSession(queryParams.get("key"));
//                } else if (cookie != null && cookie.get(UserHandler.SESSION_ATTRIBUTE) != null) {
//                    user = UserManager.getInstance().getUserForSession(ProcessEditorServerUtils.
//                                parseCookie(t).get(UserHandler.SESSION_ATTRIBUTE));
//                } else {
//                    return;
//                }
                user = RequestUtils.getCurrentUser(req);
            }

            String requestMethod = t.getRequestMethod();

            logger.info("Handle " + requestMethod + "-request for " + requestUri);

            if (requestMethod.equals("GET")) {
                handler.handleGetRequest(req, resp);
            } else if (requestMethod.equals("POST")) {
                handler.handlePostRequest(req, resp);
            } else if (requestMethod.equals("PUT")) {
                handler.handlePutRequest(req, resp);
            } else if (requestMethod.equals("DELETE")) {
                handler.handleDeleteRequest(req, resp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.throwing("Exception while handling model request", t.getRequestURI().toASCIIString(), e);
            try {
                ResponseUtils.respondWithStatus(403, e.getMessage(), new ExchangeResponseFacade(t), true);
            } catch (IOException e2) {
                logger.throwing("Exception while handling exception", t.getRequestURI().toASCIIString(), e2);

            }

        } catch (AccessViolationException e) {
            try {
                ResponseUtils.respondWithStatus(403, e.getMessage(), new ExchangeResponseFacade(t), true);
            } catch (IOException ex) {
            }
        }
    }

    protected void handleGetRequest(HttpExchange t,
            String requestUri,
            LoginableUser user)
            throws IOException,
            AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void handlePostRequest(HttpExchange t,
            String requestUri,
            LoginableUser user)
            throws IOException,
            AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void handlePutRequest(HttpExchange t,
            String requestUri,
            LoginableUser user)
            throws IOException,
            AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void handleDeleteRequest(HttpExchange t,
            String requestUri,
            LoginableUser user)
            throws IOException,
            AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
