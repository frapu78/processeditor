/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors.rpst;

import com.inubit.research.rpst.exceptions.SinkNodeException;
import com.inubit.research.rpst.exceptions.SourceNodeException;
import com.inubit.research.rpst.tree.RPST;
import com.inubit.research.rpst.tree.TriconnectedComponent;

/**
 * mostly copied from com.inubit.research.rpst.mapping.MappedRPST
 * @author tmi
 */
public class AdaptorMappedRPST {

    private Mapping mapping;
    private RPST rpst;

    public AdaptorMappedRPST(Mapping map)
            throws SinkNodeException, SourceNodeException {
        this.rpst = new RPST(map.getGraph());

        //System.out.println(rpst);

        this.mapping = map;
    }

    public AdaptorMappedTriconnectedComponent getRoot() {
        return new AdaptorMappedTriconnectedComponent(rpst.getRoot(), mapping);
    }

    public TriconnectedComponent getRawRoot() {
        return rpst.getRoot();
    }
}
