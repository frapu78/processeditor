/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.persistence;

import com.inubit.research.server.ProcessEditorServerHelper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Set;
import net.frapu.code.converter.ProcessEditorExporter;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;

/**
 * Abstract superclass for persistent location connectors like database connectors or file system connectors.
 *
 * Classes that implement this interface shall be used to store any information persistently that
 * is required for the server.
 * @author fel
 */
public abstract class PersistenceConnector {

    public enum ImageType {
        PNG,
        JPG
    }

    private static final String MODELCACHE_DIR = ProcessEditorServerHelper.SERVER_HOME_DIR + "/modelcache";
    private File modelCacheDir;

    static {
        File dir = new File( MODELCACHE_DIR );

        if (!dir.exists())
            dir.mkdirs();
    }

    public PersistenceConnector() {
         this.modelCacheDir = new File(MODELCACHE_DIR);
    }

    public abstract Map<String, String> getIDMapping( String uri );

    public abstract void storeIDMapping( String uri, Map<String, String> mapping );

    public abstract void addToIDMapping( String uri, Map<String, String> mapping );

    public abstract Set<String> getAllMappedIDs();

    /**
     * Get the image of a certain user. 
     * 
     * @param id the id of the user's image
     * @return <ul>
     *  <li> if exists, return the user's image </li>
     *  <li> else, return null</li>     *
     * </ul>
     */
    public abstract BufferedImage loadUserImage( String id );

    /**
     * Save a user's image
     * @param id the id of the user's image
     * @param imageType the type of the image. 
     * @param pic the image as byte array
     *
     * @return the picture's new id
     */
    public abstract String saveUserImage( String id, ImageType imageType, byte[] pic );

    /**
     * Adds a ProcessModel to a persistant cache.
     * @param key (Needs to be a valid filename!)
     * @param model
     */
    public boolean addToModelCache(String key, ProcessModel model) {
        try {
            ProcessEditorExporter exporter = new ProcessEditorExporter();
            File exportFile = new File(modelCacheDir, key);
            exporter.serialize(exportFile, model);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * Retrieves a ProcssModel from a persistant cache. null if not found.
     * @param key
     * @return
     */
    public ProcessModel fetchFromModelCache(String key) {
        try {
            File importFile = new File(modelCacheDir, key);
            ProcessModel result = ProcessUtils.parseProcessModelSerialization(new FileInputStream(importFile));
            return result;
        } catch (Exception ex) {
            return null;
        }
    }

    public void clearModelCache() {
        try {
            for (File f: modelCacheDir.listFiles()) {
                f.delete();
            }
        } catch (Exception ex) {};
    }
    
}