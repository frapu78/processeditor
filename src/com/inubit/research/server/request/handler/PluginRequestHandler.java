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
import com.inubit.research.server.manager.PluginManager;
import com.inubit.research.server.plugins.ObjectScope;
import com.inubit.research.server.plugins.ObjectScope.IconOffsetInfo;
import com.inubit.research.server.plugins.RenderableObjectScope;
import com.inubit.research.server.plugins.RootPageLinkPlugin;
import com.inubit.research.server.plugins.ServerPlugin;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.user.LoginableUser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.Lane;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Initial handler class for all requests that will be processed by a plug-in.
 * @author fel
 */
public class PluginRequestHandler extends AbstractRequestHandler {
    private static final String CONTEXT_URI = "/plugins";
    private static final String MODEL_QUERY_PARAM = "modeltype";

    @Override
    public void handleDeleteRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        this.handleRequest(req, resp);
    }

    @Override
    public void handleGetRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        this.handleRequest(req, resp);
    }

    @Override
    public void handlePostRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        this.handleRequest(req, resp);
    }

    @Override
    public void handlePutRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        this.handleRequest(req, resp);
    }


    private void handleRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        String requestUri = req.getRequestURI();

        LoginableUser user = RequestUtils.getCurrentUser(req);
        ServerPlugin plugin = this.getRequestedPlugin(requestUri);
        
        if (requestUri.matches(CONTEXT_URI + "(\\?" + MODEL_QUERY_PARAM + "=.+)?")) {
            try {
                ResponseUtils.respondWithJSON(resp, this.createPluginList( req ), 200);
            } catch ( JSONException ex ) {
                throw new IOException( ex );
            }
        } else if ( requestUri.matches(CONTEXT_URI + "/rootpage(.+)?" ) ) {
        	JSONObject jo = new JSONObject();
        	try {
        		jo.put("success", true);
        		JSONArray ja = new JSONArray();
        		List<RootPageLinkPlugin> plugs = PluginManager.getRootPlugins();
        		for ( RootPageLinkPlugin p : plugs )
        			ja.put( p.getJSONConfig(req) );
        		jo.put("plugins", ja );
        	} catch ( JSONException ex ) {
        		throw new IOException(ex);
        	}
        	ResponseUtils.respondWithJSON(resp, jo, 200);
    	} else if (plugin != null) {
            if (requestUri.matches(CONTEXT_URI + "/\\d+\\?menu(.+?)")) {
                JSONObject jo = new JSONObject();
                try {
                    jo.put("success", true);
                    jo.put("menuItem", plugin.getMenuItemConfig( req ));
                } catch ( JSONException ex ) {
                    throw new IOException(ex);
                }
                ResponseUtils.respondWithJSON(resp, jo, 200);
            } else {
                plugin.processRequest(requestUri, req, resp, user);
            }
        } else 
            ResponseUtils.respondWithStatus(404, "No plugin associated with this URL", resp, true);
    }

    private ServerPlugin getRequestedPlugin(String uri) {
        Pattern p = Pattern.compile(CONTEXT_URI + "/(\\d+)");

        Matcher m = p.matcher(uri);

        if (m.find())
            return PluginManager.getPlugin(m.group(1));

        return null;
    }

    private JSONObject createPluginList(RequestFacade req) throws JSONException {
        Map<String, String> query = RequestUtils.getQueryParameters(req);
        Set<String> ids = null;
        Class<? extends ProcessModel> modelClass = null;

        if ( query.get(MODEL_QUERY_PARAM) != null ) {
            try {
                modelClass = (Class<? extends ProcessModel>) Class.forName(query.get(MODEL_QUERY_PARAM));
                ids = PluginManager.getPluginIDs( modelClass );
            } catch ( Exception ex ) { 
                ex.printStackTrace();
            }
        } 

        if ( ids == null ) ids = PluginManager.getPluginIDs( null );

        JSONObject jo = new JSONObject();
        jo.put("success", true);
        JSONArray modelPlugs = new JSONArray();
        for (String id : ids) 
            modelPlugs.put(serializePlugin(id, req));

        jo.put("model", modelPlugs);

        if ( modelClass != null ) {
            try {
                ProcessModel pm = modelClass.newInstance();
                List<Class<? extends ProcessNode>> nodeClasses = pm.getSupportedNodeClasses();

                if (pm instanceof BPMNModel )
                    nodeClasses.add(Lane.class);

                List<Class<? extends ProcessEdge>> edgeClasses = pm.getSupportedEdgeClasses();

                JSONArray objectPlugs = new JSONArray();
                for ( Class<? extends ProcessNode> nodeClass : nodeClasses )
                    serializeNodeClassPlugins(nodeClass, objectPlugs, req, new ArrayList<Class<? extends ProcessNode>>());

                for ( Class<? extends ProcessEdge> edgeClass : edgeClasses ) {
                    ids = PluginManager.getObjectPluginIDs(edgeClass);

                    if ( ids.size() > 0 ) {
                        JSONObject edgePlug = new JSONObject();
                        edgePlug.put("classname", edgeClass.getName());
                        JSONArray plugins = new JSONArray();
                        for ( String id : ids )
                            plugins.put(this.serializePlugin(id, req));

                        edgePlug.put("plugins", plugins);
                        objectPlugs.put(edgePlug);
                    }
                }
                jo.put("object", objectPlugs);
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
        }
        return jo;
    }

    private void serializeNodeClassPlugins( Class<? extends ProcessNode> nodeClass, JSONArray ja, RequestFacade req, List<Class<? extends ProcessNode>> visited ) throws Exception {
        if ( visited.contains(nodeClass) ) return;
        
        visited.add( nodeClass );
        Set<String> ids = PluginManager.getObjectPluginIDs( nodeClass );

        if ( ids.size() > 0 ) {
            JSONObject nodePlug = new JSONObject();
            nodePlug.put("classname", nodeClass.getName());
            JSONArray plugs = new JSONArray();
            for ( String id : ids )
                plugs.put(this.serializePlugin(id, req));
            nodePlug.put("plugins", plugs);
            ja.put(nodePlug);
        }
        
        List<Class<? extends ProcessNode>> variants = nodeClass.newInstance().getVariants();
        for ( Class<? extends ProcessNode> variant : variants )
            serializeNodeClassPlugins(variant, ja, req, visited);
    }

    private JSONObject serializePlugin( String id , RequestFacade req ) throws JSONException {
        JSONObject plugJSON = new JSONObject();
        ServerPlugin plugin = PluginManager.getPlugin(id);
        
        plugJSON.put("toolbar", plugin.showInToolbar());
        plugJSON.put("id", id);

        plugJSON.put("type", plugin.getPluginType().toString());
        plugJSON.put("uri", req.getContext() + CONTEXT_URI + "/" + id);

        if ( plugin instanceof ObjectScope ) {
            IconOffsetInfo ioi = (( ObjectScope ) plugin).getIconOffsetInfo();
            plugJSON.put("iconOffset", ioi.toJSON());

            if ( plugin instanceof RenderableObjectScope ) {
                RenderableObjectScope rPlugin = (RenderableObjectScope) plugin;
                JSONObject renderInfo = new JSONObject();
                renderInfo.put("icon", rPlugin.getRenderingIconPath());
                renderInfo.put("renderOffset", rPlugin.getRenderingIconOffset().toJSON());
                renderInfo.put("check", rPlugin.getCheckFunction());
                plugJSON.put("renderInfo", renderInfo);
            }
        }

        return plugJSON;
    }
}