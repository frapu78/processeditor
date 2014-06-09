/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.user.LoginableUser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessObject;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Basic abstract superclass for all server plugins. For further documentation see docs/ServerPlugins.pdf
 * @author fel
 */
public abstract class ServerPlugin {

    private static final String DEFAULT_ICON_URI = "/pics/menu/maintenance.gif";
    
    protected final static Set<Class<? extends ProcessModel>> SUPPORT_ALL = new HashSet<Class<? extends ProcessModel>>(new ArrayList() {{add(ProcessModel.class);}});

    protected Set<Class<? extends ProcessModel>> supportedModels;
    protected Set<Class<? extends ProcessObject>> supportedObjects;

    /**
     * Enumeration of plugin types<br><br>
     * <ul>
     *  <li> SIMPLE - Plugin is represented by a simple button. </li>
     *  <li> PRE_ACTION_FORM - Plugin that shows a form before performing its action.
     *       The form data is passed to the plugin. </li>
     *  <li> POST_ACTION_FORM - Plugin shows a form after performing its action.
     *       The form data is passed to the plugin. (CURRENTLY NOT IMPLEMENTED)</li>
     *  <li> STRUCTURED - Plugin that results in a sub menu. It contains menu elements of multiple
     *       SIMPLE and/or FORM plugins. The included plugins work as described above. (CURRENTLY NOT IMPLEMENTED) </li>
     *  <li> INFOFORM - Plugin, that shows a form, which can only be canceled
     *      and does not block the editor, while it is shown.</li>
     * <li> DIALOG - Plugin that has a complex widget and is enabled to load further JS files</li>
     * </ul>
     */
    public enum ProcessEditorServerPluginType {
        SIMPLE,
        STRUCTURED,
        FORM,
        DIALOG,
        INFODIALOG
    }

    /**
     * Enuemeration of plugin response types<br><br>
     * <ul>
     *  <li> OPEN - Response forces browser to open a new page ( e.g. model ). 
     *       An URI has to be included within response data. </li>
     *  <li> UPDATE - Response forces the model to be layouted. Nodes and edges can be changed, removed, and added.
     *       Layout data has to be included within response data.</li>
     *  <li> ERROR - Response signals that an error has occured while performing its action.
     *       This forces the browser to show a message box (error box).
     *       The error message has to be included within response data. </li>
     *  <li> INFO - Response forces the browser to show a message box (info box).
     *       The info message has to be included within response data. </li>
     * </ul>
     */
    public enum PluginResponseType {
        OPEN,
        UPDATE,
        ERROR,
        INFO
    }

    public enum PluginScope {
        MODEL,
        OBJECT
    }

    protected ProcessEditorServerPluginType type;
    protected PluginScope scope = PluginScope.MODEL;

    /**
     * Process the incoming request to this plugin.
     * @param requestUri the requested uri
     * @param t the http exchange object
     * @param u the user that performs this request
     * @throws IOException
     */
    public abstract void processRequest( String requestUri, RequestFacade req, ResponseFacade resp, LoginableUser u ) throws IOException;

    /**
     * Get this plugin's type
     * @return the type
     */
    public ProcessEditorServerPluginType getPluginType () {
        return this.type;
    }

    public PluginScope getPluginScope() {
        return this.scope;
    }

    /**
     * Return true, to display this plugin as simple icon in toolbar
     * @return 
     */
    public boolean showInToolbar() {
        return false;
    }

    protected String getItemIconUri( RequestFacade req ) {
        String iconUri = null;
        if (this.getItemIconPath() != null) {
            iconUri = this.getItemIconPath();
            if ( req != null && !req.getContext().equals("/") )
                iconUri = req.getContext() + iconUri;
        }
        return iconUri;
    }

    protected String getItemIconPath() {
        return null;
    }

    protected abstract String getItemText();
    
    
    public boolean supportsModel( Class<? extends ProcessModel> modelClass ) {
        if ( modelClass == null )
            //in case no class is given, enable the server to return a list of avaiable plug-ins
            return true;

        if ( !this.scope.equals( PluginScope.MODEL) )
            return false;

        if ( supportedModels == null ) {
            supportedModels = ( (ModelScope) this ).getSupportedModels();
        }

        for ( Class<? extends ProcessModel> c : supportedModels )
            if ( c.isAssignableFrom(modelClass) )
                return true;

        return false;
    }

    public boolean supportsObject( Class<? extends ProcessObject> objectClass ) {
        if ( objectClass == null )
             //in case no class is given, enable the server to return a list of avaiable plug-ins
            return true;

        if ( !this.scope.equals( PluginScope.OBJECT ) )
            return false;

        if ( supportedObjects == null ) 
            supportedObjects = ( (ObjectScope) this ).getSupportedObjects();

        for ( Class<? extends ProcessObject> c : supportedObjects )
            if ( c.isAssignableFrom(objectClass) )
                return true;

        return false;
    }

    /**
     * Get JSON-String that configures the menu item of this plugin.
     * @return the JSON config object as String
     */
    public JSONObject getMenuItemConfig( RequestFacade req ) throws JSONException {
        if (this.showInToolbar() || this.getPluginScope().equals(PluginScope.OBJECT))
            return getToolbarConfig( req );
        else
            return getOrdinaryConfig( req );
    }

    private JSONObject getOrdinaryConfig( RequestFacade req ) throws JSONException{
        JSONObject jo = new JSONObject();
        jo.put("xtype", "menuitem");
        jo.put("text", this.getItemText() );

        if ( this.getItemIconUri( req ) != null ) 
            jo.put("icon", this.getItemIconUri(req));
        return jo;
    }

    private JSONObject getToolbarConfig( RequestFacade req ) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put( "xtype", "button" );
        jo.put( "icon", this.getItemIconUri( req ) == null ? DEFAULT_ICON_URI : this.getItemIconUri( req ));

        if (this.getItemText() != null) 
            jo.put("tooltip", this.getItemText());

        return jo;
    }
}
