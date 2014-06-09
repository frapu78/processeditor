/**
 *
 * Process Editor - inubit Client Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.client;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.request.handler.UserRequestHandler;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * Provides a Proxy for the directory of ProcessModels on a server.
 *
 * @author fpu
 */
public class ModelDirectory implements ModelDirectoryEntry {

    private List<ModelDirectoryEntry> directory = new LinkedList<ModelDirectoryEntry>();
    private String name;

    public ModelDirectory(String name) {
        this.name = name;
    }

    public List<ModelDirectoryEntry> getEntries() {
        List<String> keys = new LinkedList<String>();
        for (ModelDirectoryEntry e : directory) {
            String descr = e.getDescription();
            if (e instanceof ModelDirectory) {
                descr = "#__" + descr;
            }
            keys.add(descr);
        }
        Collections.sort(keys);
        List<ModelDirectoryEntry> result = new LinkedList<ModelDirectoryEntry>();
        for (String key : keys) {
            if (key.startsWith("#__")) {
                key = key.substring(3);
            }
            for (ModelDirectoryEntry e : directory) {
                if (e.getDescription().equals(key)) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    public void addEntry(ModelDirectoryEntry entry) {
        directory.add(entry);
    }

    @Override
    public String toString() {
        return "ModelDirectory (" + name + ")";
    }

    @Override
    public String getDescription() {
        return name;
    }

    /**
     *
     * @param toPublish
     * @param isNewModel
     * @param oldModelURI
     * @param server
     * @param comment
     * @param folder
     * @param title
     * @param predecessors
     * @throws Exception
     */
    public static URI publishToServer(ProcessModel toPublish, boolean isNewModel, String oldModelURI, String server, String comment, String folder, String title, List<String> predecessors) throws Exception {
        // Set new folder
        toPublish.setProperty(ProcessModel.PROP_FOLDERALIAS, folder);
        // Check if new model
        XmlHttpRequest req;
        if (isNewModel) {
            // POST new model to server
            req = new XmlHttpRequest(URI.create(server + "/models"));
            // The model should better have credentials attached to it
            UserCredentials credentials = (UserCredentials)toPublish.getTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS);
            if (credentials!=null) {
                req.setRequestProperty(HttpConstants.HEADER_KEY_COOKIE, UserRequestHandler.SESSION_ATTRIBUTE+"="+credentials.getSessionId());
            }

        } else {
            String versionLessUri;
            int indexOfVersionSubstr = oldModelURI.lastIndexOf("/version");
            if (indexOfVersionSubstr<0)
                versionLessUri = oldModelURI;
            else versionLessUri = oldModelURI.substring(0, indexOfVersionSubstr);
            req = new XmlHttpRequest(URI.create(versionLessUri));
        }
        req.setRequestProperty("Comment", ProcessEditorServerUtils.escapeString(comment));
        req.setRequestProperty("Folder", ProcessEditorServerUtils.escapeString(folder));
        req.setRequestProperty("Commit-Name", ProcessEditorServerUtils.escapeString(title));
        req.setRequestProperty("predecessors", ModelDescription.encodePredecessors(predecessors));
        // Check if model has credentials
        UserCredentials credentials = (UserCredentials)toPublish.getTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS);
        if (credentials!=null) {
            req.setRequestProperty(HttpConstants.HEADER_KEY_COOKIE, UserRequestHandler.SESSION_ATTRIBUTE+"="+credentials.getSessionId());
        }
        toPublish.setProcessName(title);
        // Process response here
        Document response = req.executePostRequest(toPublish.getSerialization());
        Element elem = (Element) response.getChildNodes().item(0);
        String errDescr = "";
        if (elem.getNodeName().equals("error")) {
            errDescr = elem.getTextContent();
        }
        if (req.getLastStatus() < 200 | req.getLastStatus() > 201) {
            throw new Exception("Upload failed: " + errDescr + "\nServer Response Code: " + req.getLastStatus());
        }
        if (elem.getNodeName().equals("url")) {
            String uri = elem.getTextContent();
            toPublish.setProcessModelURI(uri);
        }
        return new URI(toPublish.getProcessModelURI());
    }



}
