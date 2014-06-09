/*
 * Process Editor
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.helper;

import com.inubit.research.client.UserCredentials;
import java.io.IOException;
import java.net.URI;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fel
 */
public class ReferenceHelper {

    public static ProcessModel getReferencedModel( String uri, ProcessModel pm ) throws IOException, Exception {
        if (uri.contains("#")) {
            uri = uri.substring(0, uri.indexOf("#"));
        }
        // Open model with credentials of the current model!
        UserCredentials credentials = (UserCredentials) pm.getTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS);
        //System.out.println(URI.create(modelUri));
        return ProcessUtils.parseProcessModelSerialization(URI.create(uri), credentials);
    }

    public static ProcessNode getReferencedNode( String uri, ProcessModel pm ) throws IOException, Exception {
        if (uri.contains("#")) {
            uri = uri.substring(0, uri.indexOf("#"));
            String id = uri.substring(uri.indexOf("#") + 1);

            UserCredentials credentials = (UserCredentials) pm.getTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS);
            //System.out.println(URI.create(modelUri));
            ProcessModel m = ProcessUtils.parseProcessModelSerialization(URI.create(uri), credentials);

            if (m.getObjectById(id) != null) {
                    Object o = m.getObjectById(id);
                    if (o instanceof ProcessNode)
                        return ( ProcessNode ) o;
            }
        }

        
        return null;
    }

}
