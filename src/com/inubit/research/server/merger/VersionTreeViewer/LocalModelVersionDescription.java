/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server.merger.VersionTreeViewer;

import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.client.UserCredentials;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author Uwe
 */
public class LocalModelVersionDescription extends ModelVersionDescription {

    protected ProcessModel newModel;

    public LocalModelVersionDescription(ModelVersionDescription derivedFromVersion, ProcessModel localModel, Integer versionNumber, UserCredentials credentials) {
        super(derivedFromVersion.getParentModelDescription(), derivedFromVersion.getModelUri(), "local",
                derivedFromVersion.getComment(), derivedFromVersion.getPredecessors(), credentials);
        newModel = localModel;
        predecessors = new ArrayList<String>();
        predecessors.add(derivedFromVersion.getVersion());
    }

    @Override
    public ProcessModel getProcessModel() throws IOException, Exception {
        return newModel;
    }

    @Override
    public ImageIcon getPreview() throws MalformedURLException {
        throw new UnsupportedOperationException("not implemented yet");
    }







}
