/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.server.merger.VersionTreeViewer;

import com.inubit.research.client.ModelDescription;
import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.server.merger.ProcessModelMerger;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.bpmn.Task;

/**
 *
 * @author Uwe
 */
public class VersionNode extends Task {

    private ModelVersionDescription versionDescription;
    private int absoluteDistance;
    public static final int DEFAULT_WIDTH = 40;
    public static final int MIN_WIDTH = DEFAULT_WIDTH/2;
    public static final int DEFAULT_HEIGHT = 20;
    public static final int MIN_HEIGHT = DEFAULT_HEIGHT/2;
    public static final int MAX_HEIGHT = DEFAULT_HEIGHT * 3;

    public VersionNode() {
    }

    

    public VersionNode(ProcessEditor editor, ModelVersionDescription versionDescription, ModelDescription modelDescription) {
        super();
        this.versionDescription = versionDescription;
        this.setText(versionDescription.getVersion());
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setPos(50, 70);
        calcAbsoulteDistance(versionDescription, modelDescription);
        //this.setSize(VersionTreeViewer.BUTTON_HEIGHT, VersionTreeViewer.BUTTON_HEIGHT);

    }

    public ModelVersionDescription getVersionDescription() {
        return versionDescription;
    }

    public void setVersionDescription(ModelVersionDescription versionDescription) {
        this.versionDescription = versionDescription;
    }

    public String getVersion() {
        return versionDescription.getVersion();
    }

    public ProcessModel getProcessModel() throws IOException, Exception, Exception {
        return versionDescription.getProcessModel();
    }

    public List<String> getPredecessors() {
        return versionDescription.getPredecessors();
    }

    public int getAbsoluteDistance() {
        return absoluteDistance;
    }

    private void calcAbsoulteDistance(ModelVersionDescription versionDescription, ModelDescription modelDescription) {
        int result = 0;
        for (String preVersion : versionDescription.getPredecessors()) {
            try {
                ModelVersionDescription pre = modelDescription.getVersionDescription(preVersion);
                ProcessModelMerger merger = new ProcessModelMerger(pre.getProcessModel(), pre.getProcessModel(), versionDescription.getProcessModel());
                result += merger.getDistance();
            } catch (IOException ex) {
                Logger.getLogger(VersionNode.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                System.err.println("Warning: distance not accurate");
            }
        }
        if (versionDescription.getPredecessors().size()!=0)
            this.absoluteDistance = result / versionDescription.getPredecessors().size();
        else this.absoluteDistance = result;
    }
}
