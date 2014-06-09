/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.exchange;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.request.ExchangeRequestFacade;
import com.inubit.research.server.request.ExchangeResponseFacade;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

/**
 *
 * @author fel
 */
public class StaticHandler extends AbstractHandler {

    boolean ignoreFirstPathElement = false;

    public StaticHandler(String contextUri) {
        this.contextUri = contextUri;
    }

    public StaticHandler(String contextUri, boolean irgnoreFirst) {
        this.contextUri = contextUri;
        this.ignoreFirstPathElement = irgnoreFirst;
    }

    public void handle(HttpExchange he) throws IOException {

        ResponseFacade resp = new ExchangeResponseFacade(he);
        RequestFacade req = new ExchangeRequestFacade(he);

        String requestUri = req.getRequestURI();
        
        if (req.getQuery() != null && !req.getQuery().isEmpty() )
            requestUri = requestUri.substring(0, requestUri.indexOf("?"));
        // Check if gzip compression is supported
        String encoding = req.getHeader(HttpConstants.HEADER_KEY_ACCEPT_ENCODING);
        boolean compress = false;
        if (encoding != null) {
            if (encoding.contains("gzip")) {
                compress = true;
            }
        }

        if (ignoreFirstPathElement) {
            requestUri = requestUri.substring(requestUri.indexOf("/", 1));
        }

        String contentType = HttpConstants.CONTENT_TYPE_TEXT_HTML;

        if (requestUri.endsWith(".js")) {
            contentType = HttpConstants.CONTENT_TYPE_TEXT_JAVASCRIPT;
        } else if (requestUri.endsWith(".gif")) {
            contentType = HttpConstants.CONTENT_TYPE_IMAGE_GIF;
            compress = false; // Already compressed!
        } else if (requestUri.endsWith(".css")) {
            contentType = HttpConstants.CONTENT_TYPE_TEXT_CSS;
        } else if (requestUri.endsWith(".png")) {
            contentType = HttpConstants.CONTENT_TYPE_IMAGE_PNG;
            compress = false; // Already compressed!
        } else if (requestUri.endsWith(".jpg")) {
            contentType = HttpConstants.CONTENT_TYPE_IMAGE_JPEG;
            compress = false; // Already compressed!
        }

        ResponseUtils.respondWithServerResource(contentType, requestUri, resp, compress);

    }
}
