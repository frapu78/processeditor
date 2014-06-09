/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import com.inubit.research.server.plugins.ObjectScope.IconOffsetInfo;
import com.inubit.research.server.plugins.ObjectScope.Orientation;
import com.inubit.research.server.plugins.ServerPlugin.PluginScope;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.user.LoginableUser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.uml.UMLAttribute;
import net.frapu.code.visualization.uml.UMLClass;
import net.frapu.code.visualization.uml.UMLUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author fel
 */
public class UMLAttributeDialogPlugin extends DialogServerPlugin implements ObjectScope {
    private IconOffsetInfo offsetInfo;

    public UMLAttributeDialogPlugin() {
        super();
        this.scope = PluginScope.OBJECT;
        this.jsFiles = new String[]{ "Inubit.WebModeler.plugins.UMLAttributeDialog" };
        this.mainClassName = "Inubit.WebModeler.plugins.UMLAttributeDialog";
        this.offsetInfo = new IconOffsetInfo(-4, Orientation.LEFT, -27, Orientation.TOP);
    }

    @Override
    protected String getItemText() {
        return "Edit attributes";
    }

    @Override
    public IconOffsetInfo getIconOffsetInfo() {
        return offsetInfo;
    }

    @Override
    protected JSONObject getData( ModelInformation mi, RequestFacade req, LoginableUser u ) throws JSONException {
        JSONObject data = new JSONObject();
        JSONObject jo = new JSONObject();
        JSONArray a = new JSONArray();

        assert mi.getSelNodeIDs().size() == 1;
        if ( mi.getSelNodeIDs().size() == 1 ) {
            ProcessNode node = mi.getProcessModel().getNodeById( mi.getSelNodeIDs().iterator().next() );
            try {
                UMLClass umlClass = (UMLClass) node;
                List<UMLAttribute> attributes = umlClass.getAttributes();
                for ( UMLAttribute attribute : attributes ) {
                    JSONArray attrJSON = new JSONArray();
                    attrJSON.put(attribute.getName());
                    attrJSON.put(attribute.getType());
                    attrJSON.put(attribute.getMultiplicity());
                    attrJSON.put(attribute.getVisibility());
                    a.put(attrJSON);
                }
            } catch ( ClassCastException ex ) {
                ex.printStackTrace();
            }
        }
        jo.put("attributes", a);
        
        JSONArray types = new JSONArray();
        types.put(UMLUtils.getAttributeTypes());
        jo.put("types", types);
        data.put("data", jo);
        return data;
    }

    @Override
    protected JSONObject saveData(JSONArray data, ModelInformation mi) throws JSONException {
        JSONObject response = new JSONObject();
        assert mi.getSelNodeIDs().size() == 1;
        if ( mi.getSelNodeIDs().size() == 1 ) {
            StringBuffer sb = new StringBuffer();
            ProcessNode node = mi.getProcessModel().getNodeById( mi.getSelNodeIDs().iterator().next() );

            for ( int i = 0; i < data.length(); i++ ) {
                JSONObject o = data.getJSONObject(i);

                UMLAttribute att = new UMLAttribute((String) o.get("name"));
                att.setProperty( UMLAttribute.PROP_TYPE, (String) o.get("type"));
                att.setProperty(UMLAttribute.PROP_MULTIPLICITY, (String) o.get("multiplicity"));
                att.setProperty(UMLAttribute.PROP_VISIBILITY, (String) o.get("visibility"));

                sb.append(att.toString());
                if ( i < data.length() - 1 )
                    sb.append(UMLClass.ELEMENT_DELIMITER);
            }

            try {
                ((UMLClass) node).setProperty(UMLClass.PROP_ATTRIBUTES, sb.toString());
                response.put("success", true);
                response.put("action", PluginResponseType.UPDATE);
            } catch ( ClassCastException ex ) {
                ex.printStackTrace();
                response.put("success", false);
                response.put("action", PluginResponseType.ERROR);
                response.put("errormsg", "Failed to update attributes");
            }
        }

        return response;
    }

    @Override
    public Set<Class<? extends ProcessObject>> getSupportedObjects() {
        Set<Class<? extends ProcessObject>> classes = new HashSet<Class<? extends ProcessObject>>();
        classes.add( UMLClass.class );
        return classes;
    }

    @Override
    protected String getItemIconPath() {
        return "/pics/menu/pencil_small.gif";
    }
}
