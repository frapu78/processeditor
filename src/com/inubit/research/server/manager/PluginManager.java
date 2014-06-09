/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessObject;

import com.inubit.research.server.plugins.BehavioralInterfacePlugin;
import com.inubit.research.server.plugins.DomainAttributeDialogPlugin;
import com.inubit.research.server.plugins.LayoutPlugin;
import com.inubit.research.server.plugins.ReferenceChooserDialogPlugin;
import com.inubit.research.server.plugins.RootPageLinkPlugin;
import com.inubit.research.server.plugins.ServerPlugin;
import com.inubit.research.server.plugins.UMLAttributeDialogPlugin;
import com.inubit.research.server.plugins.ValidationPlugin;
import com.inubit.research.server.plugins.VerticalAlignmentPlugin;

/**
 * Registry for plugins.
 * @author fel
 */
public class PluginManager {
    private static Map<String, ServerPlugin> plugins = new HashMap<String, ServerPlugin>();
    private static List<RootPageLinkPlugin> rootPagePlugins = new LinkedList<RootPageLinkPlugin>();

    static {
        addPlugin(new LayoutPlugin());
        addPlugin(new VerticalAlignmentPlugin());
        addPlugin(new BehavioralInterfacePlugin());
        addPlugin(ValidationPlugin.getInstance());
        addPlugin(new DomainAttributeDialogPlugin());
        addPlugin(new UMLAttributeDialogPlugin());
        addPlugin(ReferenceChooserDialogPlugin.getInstance());
    }

    public static ServerPlugin getPlugin( String id ) {
        return plugins.get(id);
    }

    public static String addPlugin( ServerPlugin plugin ) {
        String id = generateID();
        plugins.put(id, plugin);
        return id;
    }
    
    public static void addRootPagePlugin( RootPageLinkPlugin plugin ) {
    	rootPagePlugins.add(plugin);
    }
    
    public static List<RootPageLinkPlugin> getRootPlugins() {
    	return rootPagePlugins;
    }

    public static Set<String> getPluginIDs( Class<? extends ProcessModel> modelClass) {
        Set<String> ids = new HashSet<String>();

        for( Map.Entry<String, ServerPlugin> e : plugins.entrySet() ) 
            if ( e.getValue().supportsModel(modelClass) )
                ids.add( e.getKey() );

        return ids;
    }

    public static Set<String> getObjectPluginIDs( Class<? extends ProcessObject> objectClass) {
        Set<String> ids = new HashSet<String>();

        for( Map.Entry<String, ServerPlugin> e : plugins.entrySet() )
            if ( e.getValue().supportsObject(objectClass) )
                ids.add( e.getKey() );

        return ids;
    }

    private static String generateID() {
        Random r = new Random(System.currentTimeMillis());
        String id = null;

        while (id == null || plugins.keySet().contains(id))
            id = String.valueOf(Math.abs(r.nextInt()));

        return id;
    }
}