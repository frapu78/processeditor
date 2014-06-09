/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler;

import com.inubit.research.server.errors.AccessViolationException;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.ResponseFacade;
import java.io.IOException;

/**
 *
 * @author fel
 */
public abstract class AbstractRequestHandler {
    public abstract void handleGetRequest(  RequestFacade req,
                                            ResponseFacade resp )
                                        throws  IOException,
                                                AccessViolationException ;

    public abstract void handlePostRequest( RequestFacade req,
                                            ResponseFacade resp )
                                        throws  IOException,
                                                AccessViolationException ;

    public abstract void handlePutRequest(  RequestFacade req,
                                            ResponseFacade resp )
                                        throws  IOException,
                                                AccessViolationException ;

    public abstract void handleDeleteRequest(   RequestFacade req,
                                                ResponseFacade resp )
                                        throws  IOException,
                                                AccessViolationException ;

}
