/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.gui;

import com.inubit.research.server.merger.animator.ProcessMergeAnimator;
import com.inubit.research.server.merger.ProcessModelMerger;
import com.inubit.research.server.merger.ProcessObjectMerger;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.helper.NodeOnSelectMenuBasis;

/**
 *
 * @author uha
 */
public class ConflictResolverEditor extends ProcessEditor {

    private static final long serialVersionUID = -5094311668980691902L;
    private ProcessMergeAnimator mergeAnimator;
    private VersionTreeManager manager = null;

    public ConflictResolverEditor(VersionTreeManager manager) {
        super();
        initalizeProperties();
        mergeAnimator = new ProcessMergeAnimator(this);
        this.manager = manager;
    }

    public ProcessModelMerger getMerger() {
        return this.getMergeAnimator().getCurrentMerger();
    }

    public void setMerger(ProcessModelMerger merger) {
        this.getMergeAnimator().setCurrentMerger(merger);
    }

    public synchronized ProcessMergeAnimator getMergeAnimator() {
        return mergeAnimator;
    }

    public synchronized void setMergeAnimator(ProcessMergeAnimator mergeAnimator) {
        this.mergeAnimator = mergeAnimator;
    }

    public void checkConflictsSolved() {
        if (manager!=null) {
            manager.checkConflictsSolved();
        }
    }

    public VersionTreeManager getManager() {
        return manager;
    }

    public void setManager(VersionTreeManager manager) {
        this.manager = manager;
    }

    


    private void initalizeProperties() {

        // Clear model
        setModel(new BPMNModel("Mein Model"));

        // Enable animations
        setAnimationEnabled(true);


    }

    @Override
	public synchronized NodeOnSelectMenuBasis getOnSelectMenu(ProcessNode po) {
        if(po != null){
        	try {
	            ProcessObjectMerger oMerger = getMerger().getObjectMerger(po);
	            return new ConflictResolverOnSelectMenu(oMerger, this);
	        } catch (IllegalArgumentException e) {
	            System.err.println("Warning: clicked object not contained in underlying model!");
	        }
        }
        return super.getOnSelectMenu(po);
       
    }
}



