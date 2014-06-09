/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.server.merger.VersionTreeViewer;

import com.inubit.research.client.ModelDescription;
import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.client.UserCredentials;
import com.inubit.research.server.merger.ProcessModelMerger;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author Uwe
 */
public class MergedModelVersionDescription extends LocalModelVersionDescription {

    ModelVersionDescription mergeHere;
    ModelVersionDescription toApply;
    ModelVersionDescription original;
    ProcessModelMerger merger;

    public MergedModelVersionDescription(ModelVersionDescription derivedFromVersionToContinue, ModelVersionDescription modelToMergeIn, ModelVersionDescription original, ModelDescription modelDescritption, Integer versionNumber, UserCredentials credentials) {
        super(derivedFromVersionToContinue, null, versionNumber, credentials);
        mergeHere = derivedFromVersionToContinue;
        toApply = modelToMergeIn;
        this.original = original;
        predecessors.clear();
        predecessors.add(mergeHere.getVersion());
        predecessors.add(toApply.getVersion());
        this.version = "merged";
        try {
            merger = new ProcessModelMerger(getOriginal().getProcessModel(), getMergeHere().getProcessModel(), getToApply().getProcessModel());
            newModel = merger.getMergedModel();
        } catch (IOException ex) {
            Logger.getLogger(MergedModelVersionDescription.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MergedModelVersionDescription.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ProcessModelMerger getMerger() {
        return merger;
    }

    @Override
    public ProcessModel getProcessModel() throws IOException, Exception {
        return merger.getMergedModel();
    }

    public void setMergedModel(ProcessModel model) {
        merger.setMergedModel(model);
        newModel = model;
    }


    

    public ModelVersionDescription getMergeHere() {
        return mergeHere;
    }

    public void setMergeHere(ModelVersionDescription mergeHere) {
        this.mergeHere = mergeHere;
    }

    public ModelVersionDescription getOriginal() {
        return original;
    }

    public void setOriginal(ModelVersionDescription original) {
        this.original = original;
    }

    public ModelVersionDescription getToApply() {
        return toApply;
    }

    public void setToApply(ModelVersionDescription toApply) {
        this.toApply = toApply;
    }



 }
