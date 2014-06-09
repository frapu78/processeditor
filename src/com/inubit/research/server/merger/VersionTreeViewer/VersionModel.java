/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server.merger.VersionTreeViewer;

import java.util.List;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 *
 * @author Uwe
 */
public class VersionModel extends BPMNModel {

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = super.getSupportedNodeClasses();
        result.add(VersionNode.class);
        return result;
    }

}
