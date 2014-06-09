/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.gui;

import com.inubit.research.client.InvalidUserCredentialsException;
import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.client.XMLHttpRequestException;
import com.inubit.research.server.merger.ClientFascade;
import com.inubit.research.server.merger.ProcessObjectMerger;
import com.inubit.research.server.merger.VersionTreeViewer.LocalModelVersionDescription;
import com.inubit.research.server.merger.VersionTreeViewer.MergedModelVersionDescription;
import com.inubit.research.server.merger.VersionTreeViewer.VersionNode;
import com.inubit.research.server.merger.VersionTreeViewer.VersionTreeViewer;
import com.inubit.research.server.merger.animator.DisplayedVersionChanger;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author Uwe
 */
public class VersionTreeManager {

    VersionTreeViewer versionTreeViewer;
    ConflictResolverEditor processEditor;
    JList changeLog;
    ClientFascade serverConnection;
    ModelVersionDescription lastDisplayedVersionDescription;
    private boolean animate = true;
    private JLabel statusLabel;
    private JButton OKButton;
    private VersionTreeListener versionTreeListener;
    private ConflictResolverListener conflictResolverListener;

    public VersionTreeManager(VersionTreeViewer versionTreeViewer, ConflictResolverEditor processEditor, JList changeLog, ClientFascade serverConnection, JLabel statusLabel, JButton OKButton) throws XMLHttpRequestException, MalformedURLException, InvalidUserCredentialsException {
        this.versionTreeViewer = versionTreeViewer;
        this.processEditor = processEditor;
        this.changeLog = changeLog;
        this.serverConnection = serverConnection;
        this.statusLabel = statusLabel;
        this.OKButton = OKButton;




        //init version browser
        versionTreeListener = new VersionTreeListener(this);        
        versionTreeViewer.addListener(versionTreeListener);
        conflictResolverListener = new ConflictResolverListener(this);
        processEditor.addListener(conflictResolverListener);




        //Changelog
        changeLog.addListSelectionListener(new ChangeLogSelectionListener(processEditor, changeLog));


        //add local version
        ModelVersionDescription localVersion = serverConnection.getLocalVersion();
        VersionNode local = new VersionNode(getEditor(), localVersion, serverConnection.loadModelDescription());
        versionTreeViewer.addVersionNode(local);

        //last displayed
        try {
            lastDisplayedVersionDescription = serverConnection.loadModelDescription().getVersionDescription(localVersion.getPredecessors().get(0));
        } catch (IOException ex) {
            Logger.getLogger(VersionTreeManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(VersionTreeManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(VersionTreeManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        //merged
        try {
            if (!serverConnection.getLocalParent().getVersion().equals(serverConnection.LoadHeadModel().getVersion())) {
                ModelVersionDescription mergedVersion = new MergedModelVersionDescription(localVersion, serverConnection.LoadHeadModel(), serverConnection.getOriginal(localVersion, serverConnection.LoadHeadModel()), serverConnection.loadModelDescription(), Integer.MIN_VALUE, localVersion.getCredentials());
                VersionNode merged = new VersionNode(getEditor(), mergedVersion, serverConnection.loadModelDescription());
                versionTreeViewer.addVersionNode(merged);
                serverConnection.setMergedVersion((MergedModelVersionDescription) mergedVersion);
                getEditor().getMergeAnimator().partialLayout(serverConnection.getMergedVersion().getMerger());
            } else {
                //commiting modified head model
                try {
                    ProcessEditor edi = getEditor();
                    ProcessModel m = lastDisplayedVersionDescription.getProcessModel();
                    edi.setModel(m);
                    //getEditor().setModel(lastDisplayedVersionDescription.getProcessModel());
                } catch (IOException ex) {
                    Logger.getLogger(VersionTreeManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(VersionTreeManager.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(VersionTreeManager.class.getName()).log(Level.SEVERE, null, ex);
        }


        //merger and change version
        getEditor().setMerger(null);
        if (serverConnection.getMergedVersion() == null) {
            changeDisplayedVersion(localVersion);
        } else {
            changeDisplayedVersion(serverConnection.getMergedVersion());
        }
        
        

    }

    public JList getChangeLog() {
        return changeLog;
    }

    public boolean isAnimate() {
        return animate;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    public ConflictResolverEditor getEditor() {
        return processEditor;
    }

    public ModelVersionDescription getLastDisplayedVersionDescription() {
        return lastDisplayedVersionDescription;
    }

    public void setLastDisplayedVersionDescription(ModelVersionDescription lastDisplayedVersionDescription) {
        this.lastDisplayedVersionDescription = lastDisplayedVersionDescription;
    }

    public ClientFascade getServerConnection() {
        return serverConnection;
    }




    private void createChangeLog() {
        Vector<ProcessObjectMerger> mergersToShow = new Vector<ProcessObjectMerger>();
        for (ProcessObjectMerger m : processEditor.getMerger().getMergeRelations()) {
            if (!m.isDestinyEqual() && !m.isDestinyKeep()) {
                mergersToShow.add(m);
            }
        }
        changeLog.setCellRenderer(new MergerListCellRenderer());
        changeLog.setListData(mergersToShow);
    }

    public void checkConflictsSolved() {
        boolean letGO = (serverConnection.getMergedVersion() == null && lastDisplayedVersionDescription instanceof LocalModelVersionDescription);
        if (serverConnection.getMergedVersion() != null) {
            boolean hasConflict = serverConnection.getMergedVersion().getMerger().hasConflict();
            if (hasConflict) {
                hasConflict =false;
                //check whether conflict is relevant ... object could have already been deleted by user
                for (ProcessObjectMerger m : serverConnection.getMergedVersion().getMerger().getConflictingObjects().values()) {
                    if (m.getMergedObject()==null) continue;
                    if (getEditor().getModel().getObjectById(m.getMergedObject().getId())!=null) {
                        hasConflict = true;
                    }
                }
            }
            letGO = letGO || (!hasConflict && lastDisplayedVersionDescription instanceof MergedModelVersionDescription);

        }
        this.OKButton.setEnabled(letGO);
    }

    public void changeDisplayedVersion(ModelVersionDescription newVersion) {
        //ProcessModelMerger merger = serverConnection.getMergedVersion() == null ? null : serverConnection.getMergedVersion().getMerger();
        getEditor().getMergeAnimator().getAnimationQueue().queue(new DisplayedVersionChanger(processEditor, newVersion, this));

    }

    public void versionChanged(ModelVersionDescription from, ModelVersionDescription to) {
        if (to instanceof MergedModelVersionDescription) {
            statusLabel.setText("displaying merger between " + serverConnection.getMergedVersion().getMergeHere() + " and " + serverConnection.getMergedVersion().getToApply() + " Original: " + serverConnection.getMergedVersion().getOriginal());
        } else {
            statusLabel.setText("comparing Version " + from.getVersion() + " to " + to.getVersion());
        }
        getEditor().setEditable(to instanceof MergedModelVersionDescription);
        lastDisplayedVersionDescription = to;
        checkConflictsSolved();
        createChangeLog();
    }
}
