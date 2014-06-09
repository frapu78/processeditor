/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.request.handler.PersistentModelLocationsRequestHandler;
import com.inubit.research.server.user.SingleUser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fel
 */
public class PersistentModelLocationManager {
    private Location homeManager = null;

    private Map<Location, Set<String>> index = new HashMap<Location, Set<String>> ();
    private Map<String, Location> paths = new HashMap<String, Location>();

    public PersistentModelLocationManager( ) {
        try {
            Class<? extends Location> c = ProcessEditorServerHelper.getDefaultLocationClass();
            System.out.println("Instantiating " + c + " as home manager!");
            this.homeManager = c.newInstance();
        } catch ( Exception ex ) {
            System.err.println("No home manager instantiated");
            ex.printStackTrace();
        }

        Set<String> usedIds = new HashSet<String>();
        this.addLocation(this.homeManager, usedIds);

        Set<ISLocation> locs = ProcessEditorServerHelper.getUserManager().getAllISConnections();

        for ( ISLocation l : locs ) {
            if ( l.checkConnection() )
                this.addLocation(l, usedIds);
        }
    }

    /**
     * Add location to this manager's index
     * @param l the location
     * @param usedIds the IDs that shall not be assigned a second time
     * @return <ul>
     *  <li>true, if location has been added successfully </li>
     *  <li>false, otherwise   </li>
     * </ul>
     */
    public boolean addLocation( Location l , Set<String> usedIds ) {
        if(!this.index.containsKey(l)) {
	        Map<String, List<ServerModel>> lIndex = l.getIndex(usedIds,true);
                Set<String> p = l.listPaths();

	        Set<String> ids = new HashSet<String>(lIndex.keySet());
	        this.index.put(l, ids);
                for (String path : p) {
                    this.paths.put(path, l);
                }
                usedIds.addAll(ids);
	        return true;
    	}
    	return false;
    }

    public boolean addISConnection( ISLocation ism, SingleUser user ) {
        return ProcessEditorServerHelper.getUserManager().addISConnection( ism , user );
    }

    public boolean moveLocation( String sourcePath, String targetPath, SingleUser user ) {
        Location sourceLocation = this.getLocationForPath(sourcePath);
        Location targetLocation = this.getLocationForPath(targetPath);

        if (targetLocation == null)
            return false;

        if (sourceLocation != null && sourceLocation == targetLocation &&
                sourceLocation.getType().equals(Location.LocationType.FILE)) {

            FileSystemLocation dm = (FileSystemLocation) sourceLocation;

            boolean moved = dm.moveFolder(sourcePath, targetPath, user);
            if (moved) {
                //reload paths
                //@todo this could be done more efficiently
                this.reloadPaths();

                return true;
            }
        }
        return false;
    }

    public String removeLocation( Location toRemove , String path, SingleUser user ) {

        if (toRemove != null && toRemove.getType().equals(Location.LocationType.FILE)) {
            FileSystemLocation dm = (FileSystemLocation) toRemove;
            String home = this.getHomePath(user);
            
            if (path.startsWith(home + MetaCache.ATTIC_FOLDER_NAME)) {
                dm.removeFolder(path, user);
                this.reloadPaths();
                return "deleted";
            } else if (path.startsWith(home + "/")) {
                String pathRest = path.replaceFirst(home, "");
                this.paths.put(home + MetaCache.ATTIC_FOLDER_NAME + pathRest, dm);
                this.moveLocation(path, home + MetaCache.ATTIC_FOLDER_NAME, user);
                return "moved";
            }
        } else if (toRemove != null && toRemove.getType().equals(Location.LocationType.IS)) {
            this.index.remove(toRemove);

            ISLocation ism = (ISLocation) toRemove;
            ProcessEditorServerHelper.getUserManager().removeISConnection( ism, user );

            this.reloadPaths();
            return "deleted";
        }

        return null;
    }

    public Set<Location> getLocations() {
        return this.index.keySet();
    }

    public Map<String, AccessType> getModelsForUser(SingleUser user) {
        Map<String, AccessType> models = new HashMap<String, AccessType>();

        if (user.isAdmin()) {
            Set<Location> locations = this.index.keySet();

            for (Location l : locations) {
                Set<String> ids = this.index.get(l);
                for (String id : ids)
                    models.put(id, l.getMetaDataHandler().getAccessability(id, -1, user));
            }
        } else {
            for (Location l : this.index.keySet()) {
                models.putAll(l.getModelsForUser( user ));
            }
        }

        return models;
    }

     /**
     * Get the location belonging to the given model ID
     * @param id the ID
     * @return <ul>
     *  <li> null, if no location was found. </li>
     *  <li> the location, otherwise </li>
     * </ul>
     */
    Location getLocationForId( String id ) {
        for (Map.Entry<Location, Set<String>> e : this.index.entrySet()) {
            if (e.getValue().contains(id))
                return e.getKey();
        }

        return null;
    }

    /**
     * Get location that has controls a path, that matches exactly the given one
     * @param path the path
     * @return the location
     */
    Location getLocationForPath( String path ) {
        Location l = this.paths.get(path);

        if ( l == null) {
            for (String p : this.paths.keySet()) {
                if (p.equals(path)) {
                    l = this.paths.get(p);
                    break;
                }
            }
        }

        return l;
    }

    /**
     * Get location that controls a path, for
     * which the given path will lead to a subdirectory
     *
     * @param path the path
     * @return the location
     */
    Location getSimilarLocationForPath( String path ) {
        if ( path.trim().equals("/") )
            return null;
        
        for (Map.Entry<String, Location> e : this.paths.entrySet()) {
            if (!e.getKey().equals("/") && path.startsWith(e.getKey())) {
                return e.getValue();
            }
        }

        return null;
    }

    Location getSaveableLocation( String path, SingleUser user ) {
        Location target = this.getLocationForPath(path);

        if (target == null)
            target = this.getSimilarLocationForPath(path);

        if (target == null)
            target = this.homeManager;

        return target;
    }

    Map<String, Location.LocationType> listLocations( SingleUser user ) {
        Map<String, Location.LocationType> list = new HashMap<String, Location.LocationType>();

        if (user.isAdmin())   {
            for (Location l : this.index.keySet()) {
                Set<String> p = l.listPaths();
                this.addPathSet(list, p, l);
            }
        } else {
            for (Location l : this.index.keySet()) {
                Set<String> p = l.listPaths( user );

                this.addPathSet(list, p, l);
            }
        }

        return list;
    }

    Set<String> getLocationsForUser( SingleUser user ) {
        Set<String> locs = new HashSet<String>();
        if ( user.isAllowedToSaveToFileSystem() )
            locs.add( "File System" );

        for ( ISLocation is : user.getISConnections() ) {
            locs.add( is.getName() );
        }

        return locs;
    }

    String getHomePath ( SingleUser user ) {
        return ((UserHomeable) this.homeManager).getUserHome( user );
    }

    String getHomeLocation( SingleUser user ) {
        if ( user.getHomeLocation() == null ) {
            if ( user.isAllowedToSaveToFileSystem() )
                return "File System";
            else
                return "";
        }

        return user.getHomeLocation().getName();
    }

    void createUserHome ( SingleUser user ) {
        ((UserHomeable) this.homeManager).createUserHome( user ) ;
    }

    void setLocationAtPath( String path, Location l ) {
        this.paths.put(path, l);
    }

    void addIdToLocation( Location l, String id ) {
        if(this.index.containsKey(l)) {
            this.index.get(l).add(id);
        }
    }

    String getLocationType( String id ) {
        Location l = this.getLocationForId(id);

        if (l != null) {
            return l.getType().toString();
        }

        return null;
    }

    void removeFromIndex( Location l ) {
        this.index.remove(l);

        Iterator<Map.Entry<String, Location>> it = this.paths.entrySet().iterator();
        while ( it.hasNext() )
            if ( it.next().getValue().equals(l) )
                it.remove();
    }

    private void addPathSet( Map<String, Location.LocationType> allPaths, Set<String> toAdd, Location l ) {
        if (l.getType().equals(Location.LocationType.FILE) || l.getType().equals(Location.LocationType.DB)) {
           for (String path : toAdd) {
                allPaths.put(path, l.getType());
           }
        } else if (l.getType().equals(Location.LocationType.IS)) {
            //only base path and attic is associated with type, others are null
            String name = l.getName();
            allPaths.put(name, Location.LocationType.IS);

            for (String path : toAdd) {
                if(path.equals(name) || name.equals("/is" + path) ) continue;

                Location.LocationType type = null;

                if(path.endsWith( MetaCache.ATTIC_FOLDER_NAME) ) {
                    type = Location.LocationType.IS;
                }
                
                if (!path.startsWith("/is/"))
                        allPaths.put("/is" + path, type);
                else
                    allPaths.put(path, type);

           }
        } 
    }

    private void reloadPaths() {
        this.paths = new HashMap<String, Location>();
        for (Location l : this.index.keySet()) {
            Set<String> p = l.listPaths();
            for (String path : p) {
                this.paths.put(path, l);
            }
        }
    }
}