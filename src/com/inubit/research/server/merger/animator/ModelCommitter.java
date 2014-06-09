/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import com.inubit.research.server.merger.ClientFascade;
import java.awt.Frame;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import net.frapu.code.visualization.ProcessEditor;

/**
 *
 * @author Uwe
 */
public class ModelCommitter extends AnimationSequence {

    private ClientFascade serverConnection;
    private Frame dialogToClose;

    public ModelCommitter(ProcessEditor editor, ClientFascade serverConnection, Frame dialogToClose) {
        super(editor);
        this.serverConnection = serverConnection;
        this.dialogToClose = dialogToClose;
    }





    public void run() {
        try {
            serverConnection.forceCommit(getEditor().getModel());
        } catch (Exception ex) {
            Logger.getLogger(ModelCommitter.class.getName()).log(Level.SEVERE, null, ex);
        }
        dialogToClose.dispose();
    }

}
