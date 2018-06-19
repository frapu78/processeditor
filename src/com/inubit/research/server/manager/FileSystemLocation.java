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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.frapu.code.converter.ProcessEditorExporter;
import net.frapu.code.visualization.ProcessModel;


import com.inubit.research.server.config.StructuralConfig;
import com.inubit.research.server.meta.DirectoryMetaDataHandler;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.model.FileServerModel;
import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.user.SingleUser;
import java.io.FileInputStream;
import net.frapu.code.visualization.ProcessUtils;

/**
 * A file system location
 * @author fel
 */
public class FileSystemLocation implements Location, UserHomeable {
    private static final String MODEL_DIR = "/models";
    private static final String META_SUFFIX = ".meta";
    private static final String XML_SUFFIX = ".xml";

    private File baseDir;
    private File modelDir;

    private DirectoryMetaDataHandler metadataHandler;

    FileSystemLocation() {
        this( ProcessEditorServerHelper.SERVER_HOME_DIR );
    }

    /**
     * Create new FileSystemLocation for the given directory
     * @param baseDir the path of the directory
     */
    public FileSystemLocation(String baseDir) {
        this( new File(baseDir ) );
    }

    public FileSystemLocation( File baseDir ) {
        this.baseDir = baseDir;
        if ( !this.baseDir.exists() )
            this.baseDir.mkdirs();

        this.modelDir = new File( this.baseDir, MODEL_DIR );
        if ( !this.modelDir.exists() )
            this.modelDir.mkdir();
    }

    public ServerModel saveProcessModel(ProcessModel pm, String id, int version) {
        String fileName = this.modelDir.getPath() + "/" + id + ".v" + version;
        
        Date now = new Date();
        String creationDate = DateFormat.getDateTimeInstance(
            DateFormat.LONG, DateFormat.LONG).format(now);
        
        pm.setProperty(ProcessModel.PROP_LASTCHECKIN, creationDate);
        
        File newVersion = new File(fileName);
        try {
            new ProcessEditorExporter().serialize(newVersion, pm);
            return new FileServerModel(newVersion, this);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean removeModel(String id, SingleUser user) {
        String homePath = this.getUserHome(user);
        String modelPath = this.metadataHandler.getFolderAlias(id);
        
        if ( modelPath.startsWith(homePath + MetaCache.ATTIC_FOLDER_NAME) ) {
            //remove it physically
            this.getMetaDataHandler().remove(id);
            return true;
        }
        //move to attic
        return false;
    }

    public String getAtticPath(SingleUser user) {
        return this.getUserHome(user) + MetaCache.ATTIC_FOLDER_NAME;
    }

    @Override
    public Map<String, List<ServerModel>> getIndex(Set<String> usedIDs, boolean forceRefresh) {
        Map<String, List<ServerModel>> index = new HashMap<String, List<ServerModel>>();

        String[] files = this.modelDir.list();
        //sort files so that version 0 always comes first
        Arrays.sort(files);

        for (String file : files) {
            //ignore meta files by now
            if (file.endsWith(META_SUFFIX) || file.endsWith(XML_SUFFIX)) {
                continue;
            }
            
            try {
                ProcessUtils.parseProcessModelSerialization(new FileInputStream(new File(this.modelDir, file)));
            } catch ( ClassNotFoundException ex ) {
                System.err.println("[FileSystemLocation] Class Not Found: " + ex.getMessage());
                continue;
            } catch ( Exception ex ) {
                System.err.println("[FileSystemLocation] Exception while parsing model serialization: " + ex.getMessage());
                continue;
            }
            
            String id = this.parseIdFromFileName(file);
            if (id == null) {
                continue;
            }

            if (file.endsWith(".v0")) {
                //create new version list
                List<ServerModel> versions = new LinkedList<ServerModel>();
                index.put(id, versions);
            }

            //get version number from file
            int version = -1;
            int vIndex = file.lastIndexOf(".v") + 2;

            try {
                version = Integer.parseInt(file.substring(vIndex));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue;
            }
            
            //add version to corresponding list
            this.addToList(index.get(id), version, file);
        }

        return index;
    }

    @Override
    public DirectoryMetaDataHandler getMetaDataHandler() {
        if (this.metadataHandler == null) {
            this.metadataHandler = new DirectoryMetaDataHandler(this.modelDir);
        }

        return this.metadataHandler;
    }

    @Override
    public LocationType getType() {
        return LocationType.FILE;
    }

    @Override
    public String getName() {
        return this.baseDir.getName();
    }

    @Override
    public Set<String> listPaths() {
        return this.getMetaDataHandler().getConfig().listPaths();
    }

    @Override
    public Set<String> listPaths( SingleUser user ) {
        Set<String> paths = new HashSet<String>();

        StructuralConfig sc = this.getMetaDataHandler().getConfig();

        paths.addAll(sc.listUserHome(user));

        Set<String> sharedPaths = sc.listSharedPaths( user );

        for (String sPath : sharedPaths)
            paths.add(Location.SHARED_PATH_PREFIX + sPath);
        
        return paths;
    }

    @Override
    public Map<String, AccessType> getModelsForUser( SingleUser user ) {
        Map<String, AccessType> models = new HashMap<String, AccessType>();

        StructuralConfig sc = this.getMetaDataHandler().getConfig();

        String path = sc.getUserHome(user);
        Set<String> ids = sc.getModelIDs(path, true);
        for (String id : ids)
            models.put(id, AccessType.OWNER);

        Set<String> sharedIds = sc.getSharedModels(user);
        for (String id : sharedIds) {
            AccessType access = this.getMetaDataHandler().getAccessability(id, -1, user);
            if (!(models.containsKey(id) && models.get(id).compareTo(access) >= 0))
                models.put(id, access);
        }
        return models;
    }

    @Override
    public ServerModel createNewModel( File modelFile, String path, String id, SingleUser user, String comment ) {
        ServerModel newModel = null;
        
        try {
            newModel = this.importFromFile(modelFile, id);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (newModel != null) {
            this.addModelToFolder(id, path, user);
            this.getMetaDataHandler().setVersionUser(id, "0", user.getName());
            this.getMetaDataHandler().setVersionComment(id, "0", comment);
            return newModel;
        }

        return null;
    }

    public void addModelToFolder( String id , String path, SingleUser user) {
        this.getMetaDataHandler().getConfig().addModel(id, path, user);
    }

    public boolean moveFolder( String folder, String target, SingleUser user) {
        return this.getMetaDataHandler().getConfig().moveDirectory( folder, target, user );
    }

    public void removeFolder( String path, SingleUser user) {
        this.getMetaDataHandler().getConfig().removeDirectory( path, user );
    }

    public Set<String> getModelIDs( String path, boolean recursive) {
        return this.getMetaDataHandler().getConfig().getModelIDs(path, recursive);
    }

    public String getUserHome( SingleUser user ) {
        return this.getMetaDataHandler().getConfig().getUserHome( user );
    }

    public void createUserHome( SingleUser user ) {
        this.getMetaDataHandler().getConfig().createUserHome(user);
    }

    @Override
    public boolean checkConnection() {
        // This location should always work!
        return true;
    }

     /**
     * Import a model from the given file.
     * @param modelFile the file containing the model
     * @param id the model's id
     */
    private FileServerModel importFromFile( File modelFile, String id ) throws IOException {
        if (modelFile == null || id == null) {
            return null;
        }

        //create new model file
        File newFile = new File(this.modelDir, id + ".v0");

        BufferedReader br = new BufferedReader(new FileReader(modelFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));

        //copy
        while (br.ready()) {
            bw.write(br.readLine());
            bw.newLine();
        }

        bw.flush();
        bw.close();

        return new FileServerModel(newFile, this);
    }

    /**
     * Parse the model ID from the given version fileName
     * @param fileName the file name
     * @return <ul>
     *  <li> null, if it is no process model version file </li>
     *  <li> the id, otherwise </li>
     * </ul>
     */
    private String parseIdFromFileName(String fileName) {
        //get position of file extension
        int endIndex = fileName.lastIndexOf(".v");
        //if it is no version file retun null
        if (endIndex == -1) {
            return null;
        }

        //get beginning of file name / end of path
        int startIndex = fileName.lastIndexOf("/");

        return fileName.substring(startIndex + 1, endIndex);
    }

    /**
     * Add file to list at the given index
     * @param list the list
     * @param index the index
     * @param fileName the file name
     */
    private void addToList(List<ServerModel> list, int index, String fileName) {
        File f = new File(this.modelDir, fileName);

        //if index is not already present
        if (index > list.size()) {
            //add dummy entries, that will be set later
            for (int i = list.size(); i < index; i++) {
                list.add(i, null);
            }

            list.add(index, new FileServerModel(f, this));
        } else if (index < list.size()) {
            //replace dummy entry
            list.set(index, new FileServerModel(f, this));
        } else {
            //add new hosted model
            list.add(index, new FileServerModel(f, this));
        }
    }
}