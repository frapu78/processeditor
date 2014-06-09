/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.editors;

import net.frapu.code.visualization.*;
import net.frapu.code.visualization.editors.PropertyEditor;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;

/**
 *
 * Returns a default Property Editor.
 *
 * @todo: Needs to support direct connection to a ProcessObject for updating values!
 *
 * @author frank
 */
public class DefaultPropertyEditor extends PropertyEditor {

    protected JTextField defaultEditor = null;

    public DefaultPropertyEditor() {
        super();
    }

    private void init() {
        // Init
        defaultEditor = new JTextField();
        defaultEditor.setColumns(20);
        defaultEditor.setSize(100, defaultEditor.getSize().height);
        defaultEditor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ProcessObject po = getProcessObject();
                if (po!=null) {
                    po.setProperty(getPropertyKey(), getValue());
                }
            }
        });
    }

    @Override
    public Component getComponent() {
        if (defaultEditor == null) {
            init();
        }
        return defaultEditor;
    }

    @Override
    public String getValue() {
        if (defaultEditor == null) {
            init();
        }
        return defaultEditor.getText();
    }

    @Override
    public void setValue(String value) {
        if (defaultEditor == null) {
            init();
        }
        defaultEditor.setText(value);
    }

    @Override
    public boolean isReadOnly() {
        if (defaultEditor == null) {
            init();
        }
        return !defaultEditor.isEnabled();
    }

    @Override
    public void setReadOnly(boolean b) {
        if (defaultEditor == null) {
            init();
        }
        defaultEditor.setEnabled(!b);
    }

    @Override
    public void free() {
        defaultEditor = null;
    }
}
