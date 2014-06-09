/**
 *
 * Process Editor - inubit Workbench Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins;

import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.WorkbenchEditorListener;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author fel
 */
public class RPSTPlugin extends WorkbenchPlugin implements WorkbenchEditorListener {

    private RPSTPluginDialog dialog;

    public RPSTPlugin( Workbench workbench ) {
        super( workbench );
        workbench.addWorkbenchEditorListener(this);
    }

    public void newEditorCreated(ProcessEditor editor) {
        try {
            if ( this.dialog != null && this.dialog.isVisible() )
                this.dialog.refresh();
        } catch ( Exception ex ) {}
    }

    public void selectedProcessEditorChanged(ProcessEditor editor) {
        if ( this.dialog != null && this.dialog.isVisible() )
                this.dialog.editorChanged(editor);
    }

    @Override
    public Component getMenuEntry() {
        JMenuItem item = new JMenuItem( "RPST" );

        item.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProcessModel pm = workbench.getSelectedModel();

                if ( dialog == null )
                    dialog = new RPSTPluginDialog(workbench, false);

                try {
                    dialog.refresh();
                    dialog.setVisible(true);
                } catch ( Exception ex ) {
                    ex.printStackTrace();
                }
            }
        });

        return item;
    }
}
