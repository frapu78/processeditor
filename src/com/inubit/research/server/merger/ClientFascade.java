/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger;

import com.inubit.research.client.ModelDescription;
import com.inubit.research.client.ModelDirectory;
import com.inubit.research.client.ModelServer;
import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.client.UserCredentials;
import com.inubit.research.server.merger.VersionTreeViewer.LocalModelVersionDescription;
import com.inubit.research.server.merger.VersionTreeViewer.MergedModelVersionDescription;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.bpmn.BPMNModel;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.xml.sax.SAXException;

/**
 *
 * @author FSC
 */
public class ClientFascade {

    private AnswerFromPublishModelDialog answer;
    private ProcessModel modelToPublish;
    private ModelDescription modelDescription;
    private MultiKeyMap mergerCache = new MultiKeyMap();
    private MergedModelVersionDescription mergedVersion = null;

    public ProcessModel getModelToPublish() {
        return modelToPublish;
    }

    public MergedModelVersionDescription getMergedVersion() {
        return mergedVersion;
    }

    public void setMergedVersion(MergedModelVersionDescription mergedVersion) {
        this.mergedVersion = mergedVersion;
    }

    public ClientFascade(ProcessModel modelToPublish) {
        this.modelToPublish = modelToPublish;
    }

    /*
    //**workbench specific**
    WorkbenchPublishModelDialog publishModelDialog = new WorkbenchPublishModelDialog(this, true);
    SwingUtils.center(publishModelDialog);
    publishModelDialog.setTitleText(getSelectedModel().getProperty(ProcessModel.PROP_PROCESS_NAME));
    publishModelDialog.setFolder(getSelectedModel().getProperty(ProcessModel.PROP_FOLDERALIAS));
    //**workbench specific**
    pubish1_ispublishAsNewModel(getSelectedModel());
    //call dialog
    try {
    ProcessModel previewModel = publish2_ProcessAnswerOfPublishModelDialog(answer);
    
    
    }
    except
    JOptionPane.showConfirmDialog(this, ex.getMessage(), "Error Publishing Model", JOptionPane.WARNING_MESSAGE);
    if (previewModel!=null) {
    //conflict requires user interaction
    
    }
    
    
    processModelSaved(modelToPublish, null);
     */
//getselectedModel
   /* Phase 1 of Model publishing
     * Assigns the ProcessModel that should be published
     * @return returns true if the Model should be published as a new model
     */
    public boolean shouldBePublishedAsNewModel() {
        return (!modelToPublish.getProcessModelURI().startsWith("http"));
    }

    /*
     * @return returns null if ProcessModel was published successfully.
     * If a conflict occurs, returns a preview Model which should be presented to the user in order to solve
     * the conflict
     */
    public void forceCommit(ProcessModel model) throws Exception {
        ModelDirectory.publishToServer(model, answer.PublishAsNewModel, modelToPublish.getProcessModelURI(), answer.serverURI, answer.comment, answer.folder, answer.title, new LinkedList<String>());
    }

    public boolean requiresMerger() {
        try {
            return getLocalParent().getVersion().equals(modelDescription.getHeadVersion());
        } catch (Exception ex) {
            Logger.getLogger(ClientFascade.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new IllegalArgumentException();
    }

    public boolean modelToCommitIsBasedOnLatestVersion(){
        try {
            return getLocalParent().getVersion().equals(LoadHeadModel().getVersion());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean commitIfPossible(AnswerFromPublishModelDialog answer) throws Exception {
        this.answer = answer;
        if (answer.publish) {
            if (answer.ForceCommit || modelToCommitIsBasedOnLatestVersion()) {
                forceCommit(modelToPublish);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /*
     * @return returns null if ProcessModel was published successfully.
     * If a conflict occurs, returns a preview Model which should be presented to the user in order to solve
     * the conflict
     */
    public ModelVersionDescription getLocalParent() throws Exception {
        String originalUri = modelToPublish.getProcessModelURI();
        return loadModelDescription().getVersionDescription(new URI(originalUri));
    }

    public int getVersionModelToPublishOrigin() {
        String result = modelToPublish.getProcessModelURI();
        result = result.substring(result.lastIndexOf("/") + 1);
        return Integer.parseInt(result);
    }

    public ProcessModel LoadVersionFromRepository(int version) throws Exception {
        if (version < 0) {
            return new BPMNModel();
        }

        if (version >= loadModelDescription().getModelVersionDescriptions().size()) {
            return modelToPublish;
        }
        for (ModelVersionDescription modelVersionDescription : loadModelDescription().getModelVersionDescriptions()) {
            if (Integer.parseInt(modelVersionDescription.getVersion()) == version) {
                return modelVersionDescription.getProcessModel();
            }
        }
        throw new Exception("model version not found");
    }

    public ModelVersionDescription LoadHeadModel() throws Exception {
        return loadModelDescription().getVersionDescription(loadModelDescription().getHeadVersion());
    }

    public ModelDescription loadModelDescription() {
        try {
            ModelServer server = new ModelServer(new URI(modelToPublish.getProcessModelURI()), "Test",
                    (UserCredentials) modelToPublish.getTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS));
            ModelDescription modelDescription =
                    server.findModel(modelToPublish.getProcessModelURI());
            return modelDescription;
        } catch (Exception ex) {
            Logger.getLogger(ClientFascade.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new Error("Server Error");
    }

    private ProcessModelMerger getCachedMerger(String compareVersion, String to) {
        return (ProcessModelMerger) this.mergerCache.get(compareVersion, to);
    }

    private void cacheMerger(ProcessModelMerger merger, String compareVersion, String to) {
        this.mergerCache.put(compareVersion, to, merger);
    }

    public ProcessModelMerger getMerger(ModelVersionDescription compareFrom, ModelVersionDescription to, boolean actuallyMerging) throws Exception {
        //ProcessModelMerger merger = getCachedMerger(compareFrom.getVersion(), to.getVersion());
        ProcessModelMerger merger = null;
        if (merger == null) {
            System.out.println("Cache miss");
            ProcessModel mFrom;
            ProcessModel mTo;
            ProcessModel mOriginal;
            mFrom = compareFrom.getProcessModel();
            mTo = to.getProcessModel();
            //mOriginal = getOrginal_Old(Integer.parseInt(compareFrom.getVersion()), Integer.parseInt(to.getVersion()));
            ModelVersionDescription original = getOriginal(compareFrom, to);
            mOriginal = original.getProcessModel();
//            if (Integer.parseInt(original.getVersion())>Integer.parseInt(compareFrom.getVersion())
//                    || Integer.parseInt(original.getVersion())>Integer.parseInt(to.getVersion())) {
//                throw new IllegalStateException("versions?");
//          }
            if (!actuallyMerging && !original.getVersion().equals(compareFrom.getVersion()) && !original.getVersion().equals(to.getVersion())) {
                System.err.println("original version might be wrong");
            }
            System.out.println("Original: " + original.getVersion() + " From: " + compareFrom.getVersion() + " To: " + to.getVersion());
            merger = new ProcessModelMerger(mOriginal, mFrom, mTo);
            cacheMerger(merger, compareFrom.getVersion(), to.getVersion());
        } else {
            System.out.println("Cache hit");
        }
        return merger;
    }

    private List<ModelVersionDescription> getAllVersions(Collection<String> versions) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, Exception {
        List<ModelVersionDescription> result = new LinkedList<ModelVersionDescription>();
        for (String s : versions) {
            result.add(loadModelDescription().getVersionDescription(s));
        }
        return result;
    }

    private void replaceByPredecessors(LinkedList<ModelVersionDescription> replace) {
        LinkedList<ModelVersionDescription> copy = new LinkedList<ModelVersionDescription>(replace);
        replace.clear();
        for (ModelVersionDescription v : copy) {
            try {
                if (v != null) {
                    replace.addAll(getAllVersions(v.getPredecessors()));
                }
            } catch (IOException ex) {
                Logger.getLogger(ClientFascade.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(ClientFascade.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(ClientFascade.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(ClientFascade.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(ClientFascade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private boolean precedes(ModelVersionDescription precededBy, ModelVersionDescription version) {
        LinkedList<ModelVersionDescription> preds = new LinkedList<ModelVersionDescription>();
        preds.add(version);
        do {
            replaceByPredecessors(preds);
            if (preds.contains(precededBy)) {
                return true;
            }
        } while (!preds.isEmpty());
        return false;
    }

    public ModelVersionDescription getOriginal(ModelVersionDescription compareFrom, ModelVersionDescription to) {
        LinkedList<ModelVersionDescription> visitedFromCompareFrom = new LinkedList<ModelVersionDescription>();
        LinkedList<ModelVersionDescription> visitedFromCompareTo = new LinkedList<ModelVersionDescription>();
        LinkedList<ModelVersionDescription> currentCompareFrom = new LinkedList<ModelVersionDescription>();
        LinkedList<ModelVersionDescription> currentCompareTo = new LinkedList<ModelVersionDescription>();

        currentCompareFrom.add(compareFrom);
        currentCompareTo.add(to);

        if (precedes(to, compareFrom)) {
            return compareFrom;
        }

        while (!currentCompareFrom.isEmpty() || !currentCompareTo.isEmpty()) {
            visitedFromCompareFrom.addAll(currentCompareFrom);
            visitedFromCompareTo.addAll(currentCompareTo);
            if (currentCompareFrom.size() == 1 && visitedFromCompareTo.containsAll(currentCompareFrom)) {
                return currentCompareFrom.getFirst();
            }
            if (currentCompareTo.size() == 1 && visitedFromCompareFrom.containsAll(currentCompareTo)) {
                return currentCompareTo.getFirst();
            }
            replaceByPredecessors(currentCompareTo);
            replaceByPredecessors(currentCompareFrom);
        }
        throw new IllegalStateException("should not be reached");
    }

//    @Deprecated
//    private ProcessModel getOrginal_Old(int compareFrom, int to) {
//        try {
//            int maxVersion = this.getModelDescription().getModelVersionDescriptions().size() - 1;
//            if (to > maxVersion) {
//                if (getVersionModelToPublishOrigin() > compareFrom) {
//                    return LoadVersionFromRepository(compareFrom);
//                } else {
//                    return getLocalParent();
//                }
//            }
//            if (compareFrom > maxVersion) {
//                if (getVersionModelToPublishOrigin() > to) {
//                    return LoadVersionFromRepository(to);
//                } else {
//                    return getLocalParent();
//                }
//            }
//            return LoadVersionFromRepository(compareFrom);
//
//
//        } catch (Exception ex) {
//            Logger.getLogger(ClientFascade.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        throw new IllegalStateException();
//    }
    public LocalModelVersionDescription getLocalVersion() {
        try {
            return new LocalModelVersionDescription(getLocalParent(),
                    getModelToPublish(), loadModelDescription().getModelVersionDescriptions().size(),
                    (UserCredentials) modelToPublish.getTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS));
        } catch (Exception ex) {
            Logger.getLogger(ClientFascade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
