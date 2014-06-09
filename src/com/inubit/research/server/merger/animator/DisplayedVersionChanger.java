/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.server.merger.ProcessModelMerger;
import com.inubit.research.server.merger.VersionTreeViewer.MergedModelVersionDescription;
import com.inubit.research.server.merger.gui.VersionTreeManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.ProcessEditor;

/**
 *
 * @author Uwe
 */
public class DisplayedVersionChanger extends AnimationSequence {

    private ModelVersionDescription lastDisplayedVersionDescription;
    private ModelVersionDescription newVersion;

    private VersionTreeManager versionTreeManager;


    public DisplayedVersionChanger(ProcessEditor editor, ModelVersionDescription newVersion, VersionTreeManager versionTreeManager) {
        super(editor);
        this.newVersion = newVersion;
        this.versionTreeManager = versionTreeManager;
    }



    public void changeDisplayedVersion() {
        lastDisplayedVersionDescription = versionTreeManager.getLastDisplayedVersionDescription();
        AnimationSequenceQueue animQueue = getEditor().getMergeAnimator().getAnimationQueue();
        if (lastDisplayedVersionDescription instanceof MergedModelVersionDescription) {
            ((MergedModelVersionDescription) lastDisplayedVersionDescription).setMergedModel(getEditor().getModel().clone());
        }
        if (lastDisplayedVersionDescription == newVersion) {
            try {
                getEditor().getMergeAnimator().animateModelTransition(newVersion.getProcessModel());
            } catch (IOException ex) {
                Logger.getLogger(DisplayedVersionChanger.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(DisplayedVersionChanger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
        if (newVersion instanceof MergedModelVersionDescription) {
            if (versionTreeManager.isAnimate()) {
                try {
                    getEditor().getMergeAnimator().animateModelTransition(newVersion.getProcessModel());
                } catch (IOException ex) {
                    Logger.getLogger(DisplayedVersionChanger.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(DisplayedVersionChanger.class.getName()).log(Level.SEVERE, null, ex);
                } 
            } else {
                try {
                    animQueue.queue(new ModelSetter(getEditor(), newVersion.getProcessModel()));
                } catch (IOException ex) {
                    Logger.getLogger(DisplayedVersionChanger.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(DisplayedVersionChanger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            animQueue.queue(new VersionChangeFinished(getEditor(), ((MergedModelVersionDescription)newVersion).getMerger(), versionTreeManager, lastDisplayedVersionDescription, newVersion));
        } else {
            try {

                ProcessModelMerger newMerger;
                newMerger = versionTreeManager.getServerConnection().getMerger(lastDisplayedVersionDescription, newVersion, false);
                if (versionTreeManager.isAnimate()) {
                    try {
                        // einfacher Versionsvergleich
                        if (getEditor().getMergeAnimator().getCurrentMerger() != null) {
                            if(!(lastDisplayedVersionDescription instanceof MergedModelVersionDescription))
                                animQueue.queue(new MarkingRemover(getEditor()));
                        }
                            //getEditor().getMergeAnimator().animateModelTransition(lastDisplayedVersionDescription.getProcessModel());
                            //backup changes in merged Model
                            //animQueue.queue(new MergedModelBackup(getEditor(), lastDisplayedVersionDescription));
                        //}
                        getEditor().getMergeAnimator().animateModelTransition(newMerger, false);
                    } catch (Exception ex) {
                        Logger.getLogger(DisplayedVersionChanger.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        animQueue.queue(new ModelSetter(getEditor(), newVersion.getProcessModel()));                       
                    } catch (IOException ex) {
                        Logger.getLogger(DisplayedVersionChanger.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(DisplayedVersionChanger.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    getEditor().getMergeAnimator().setCurrentMerger(newMerger);
                }
                animQueue.queue(new VersionChangeFinished(getEditor(), newMerger, versionTreeManager, lastDisplayedVersionDescription, newVersion));
            } catch (Exception ex) {
                Logger.getLogger(DisplayedVersionChanger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void run() {
        changeDisplayedVersion();
    }
}
