/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.extjs.JavaScriptFunction;
import com.inubit.research.server.manager.ModelManager;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.Linkable;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.DataObject;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.domainModel.DomainClassReference;
import net.frapu.code.visualization.editors.ReferenceChooserRestriction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author fel
 */
public class ReferenceChooserDialogPlugin extends DialogServerPlugin implements RenderableObjectScope {
	
	public static class LinkValidator {
		private static LinkValidator instance;
		
		protected LinkValidator() { }
		
		public boolean isLinkValid( ProcessModel currentModel, ProcessObject currentObject, ProcessModel potRefModel, ProcessObject potRefObject ) { 
			return true;
		}
		
		public static LinkValidator getInstance() {
			if ( instance == null )
				instance = new LinkValidator();
			
			return instance;
		}
	}
	
    private static ReferenceChooserDialogPlugin instance;

    private static Map<Class<? extends ProcessObject>, LinkValidator> supportedClasses = new HashMap<Class<? extends ProcessObject>, LinkValidator>();
    
    private IconOffsetInfo offsetInfo;
    private IconOffsetInfo renderOffset;
    private JavaScriptFunction checkFunction;
    
    static {
        addSupportedObject(Lane.class);
        addSupportedObject(DataObject.class);
        addSupportedObject(DomainClassReference.class);
    }
    
    public ReferenceChooserDialogPlugin() {
        super();
        this.jsFiles = new String[] {
            "Inubit.WebModeler.plugins.ReferenceChooserDialog",
            "Inubit.WebModeler.model.viewer.AnnotatedProcessModel",
            "Inubit.WebModeler.model.viewer.AnnotatedProcessNode",
            "Inubit.WebModeler.model.viewer.AnnotatedProcessEdge",
            "Inubit.WebModeler.model.viewer.ReferenceNode",
            "Inubit.WebModeler.handler.ProcessViewerMouseListener",
            "Inubit.WebModeler.handler.ReferenceChooserMouseListener",
            "Inubit.WebModeler.handler.ProcessViewerLaneHandler"
        };
        this.mainClassName = "Inubit.WebModeler.plugins.ReferenceChooserDialog";
        this.scope = PluginScope.OBJECT;
        this.offsetInfo = new IconOffsetInfo(-27, Orientation.LEFT, -12, Orientation.BOTTOM);
        this.renderOffset = new IconOffsetInfo(+5, Orientation.LEFT, +5, Orientation.BOTTOM);
        this.initializeJSFunction();
    }


    @Override
    protected JSONObject getData(ModelInformation mi, RequestFacade req, LoginableUser u) throws JSONException {
        JSONObject data = new JSONObject();
        JSONObject tmp = new JSONObject();
        JSONArray treeData = new JSONArray();
        try {
        Map<ProcessModel, Set<JSONObject>> entryToModelMap = new HashMap<ProcessModel, Set<JSONObject>>();
        ProcessNode selectedNode = mi.getProcessModel().getNodeById(mi.getSelNodeIDs().iterator().next());
        Map<String, Set<ProcessNode>> linkableNodes = getLinkableNodeList(selectedNode, u, mi.getProcessModel() );
        for ( Map.Entry<String, Set<ProcessNode>> e : linkableNodes.entrySet() ) {
            String namespacePrefix = "models@@@" + e.getKey();
            
            if (!e.getKey().equals(mi.getProcessModel().getId())) {
                JSONObject jo = new JSONObject();
                jo.put("id", namespacePrefix);
                jo.put("text", ModelManager.getInstance().getRecentMetaData(e.getKey()).getProcessName());
                JSONArray children = new JSONArray();
                jo.put("children", children);

                treeData.put(jo);
                for( ProcessNode pn : e.getValue() ) {
                    JSONObject leaf = new JSONObject();
                    leaf.put("id", "nodes@@@" + pn.getId());
                    leaf.put("text", pn.getName());
                    leaf.put("leaf", true);
                    children.put(leaf);
                }

            }
        }
        
        
        tmp.put("treedata", treeData);
        tmp.put("currentRef", selectedNode.getProperty(ProcessNode.PROP_REF) );
        data.put("data", tmp);
        
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        return data;
    }

    @Override
    protected JSONObject saveData(JSONArray data, ModelInformation mi) throws JSONException {
        JSONObject response = new JSONObject();
        String refValue = "",
                textValue = "";
        if ( data.length() > 0 ) {
            JSONObject ref = data.getJSONObject(0);
            refValue = (String) ref.get("ref");
            textValue = (String) ref.get("text");
        }
        
        ProcessNode selectedNode = mi.getProcessModel().getNodeById(mi.getSelNodeIDs().iterator().next());
        if (selectedNode instanceof Linkable) {
        	
            selectedNode.setProperty(ProcessNode.PROP_REF, refValue);
            
            if(selectedNode.getPropertyKeys().contains(ProcessNode.PROP_LABEL))
                selectedNode.setProperty(ProcessNode.PROP_LABEL, textValue);
            else if(selectedNode.getPropertyKeys().contains(ProcessNode.PROP_TEXT))
                selectedNode.setProperty(ProcessNode.PROP_TEXT, textValue);
        }

        response.put("action", PluginResponseType.UPDATE);
        response.put("success", true);
        return response;
    }

    @Override
    protected String getItemText() {
        return "Link element to...";
    }

    @Override
    protected String getItemIconPath() {
        return "/pics/menu/icon_16x16_links.gif";
    }

    public IconOffsetInfo getIconOffsetInfo() {
        return this.offsetInfo;
    }
// 
//    changed to add addtional classes from external projects
//    
//    public Set<Class<? extends ProcessObject>> getSupportedObjects() {
//        Set<Class<? extends ProcessObject>> classes = new HashSet<Class<? extends ProcessObject>>();
////        classes.add(Activity.class);
//        classes.add(Lane.class);
//        classes.add(DataObject.class);
//        classes.add(DomainClassReference.class);
//        return classes;
//    }
    
    @Override
    public Set<Class<? extends ProcessObject>> getSupportedObjects() {
        return supportedClasses.keySet();
    }
    
    public static void addSupportedObject(Class<? extends ProcessObject> cls) {
        addSupportedObject(cls, LinkValidator.getInstance());
    }
    
    public static void addSupportedObject(Class<? extends ProcessObject> cls, LinkValidator val ) {
//    	if(!supportedClasses.containsKey(cls))
            supportedClasses.put(cls, val );
    }

    private Map<String, Set<ProcessNode>> getLinkableNodeList( ProcessNode node, LoginableUser user, ProcessModel currentModel ) {
        Map<String, Set<ProcessNode>> linkableNodes = new HashMap<String, Set<ProcessNode>>();
        if (node == null || !(node instanceof Linkable) || !(user instanceof SingleUser) || supportedClasses.get( node.getClass() ) == null )
            return linkableNodes;

        ReferenceChooserRestriction restriction = ((Linkable) node).getReferenceRestrictions();
        Map<String, AccessType> models = ModelManager.getInstance().getRecentVersions((SingleUser) user);
        LinkValidator lv = supportedClasses.get( node.getClass() );
        for( String id : models.keySet() ) {
            Set<ProcessNode> links = new HashSet<ProcessNode>();
            ServerModel sm = ModelManager.getInstance().getRecentVersion(id);
            if ( sm == null )
                continue;
            
            ProcessModel pm = ModelManager.getInstance().getRecentVersion(id).getModel();
                for ( ProcessNode n : pm.getNodes() ) 
                    if ( restriction.isRestricted(n) && lv.isLinkValid(currentModel,node, pm, n) )
                        links.add(n);
            if ( links.size() > 0 )
                linkableNodes.put(id, links);
        }

        return linkableNodes;
    }
    
    public IconOffsetInfo getRenderingIconOffset() {
        return this.renderOffset;
    }

    public String getRenderingIconPath() {
        return this.getItemIconPath();
    }

    public JavaScriptFunction getCheckFunction() {
        return this.checkFunction;
    }

    private void initializeJSFunction() {
        StringBuffer sb = new StringBuffer(100);
        sb.append(  "function(object){");
        sb.append(      "if(object.getProperty('");
        sb.append( ProcessNode.PROP_REF);
        sb.append(      "')!= null && object.getProperty('");
        sb.append( ProcessNode.PROP_REF);
        sb.append(      "')!= '') return true;");
        sb.append(      " else return false;");
        sb.append("}");

        this.checkFunction = new JavaScriptFunction(sb.toString());
    }
    
    public static ReferenceChooserDialogPlugin getInstance() {
        if (instance == null)
            instance = new ReferenceChooserDialogPlugin();
        return instance;
    }
}
