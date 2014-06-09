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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author frank
 */
public class ListSelectionPropertyEditor extends PropertyEditor {
    protected JComboBox defaultEditor;
    protected ComboBoxModel model;
    protected String[] values;

    public ListSelectionPropertyEditor(String[] values) {
        super();
        this.values=values;
    }

    private void init() {
        defaultEditor = new JComboBox();
        defaultEditor.setSize(200,defaultEditor.getSize().height);
        model = new DefaultComboBoxModel(values);
        defaultEditor.setModel(model);
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
        if (defaultEditor==null) init();
        return defaultEditor;
    }

    @Override
    public String getValue() {
        if (defaultEditor==null) init();
        return (String)defaultEditor.getSelectedItem();
    }

    public String[] getValues() {
        return values;
    }

    @Override
    public void setValue(String value) {
        if (defaultEditor==null) init();
        model.setSelectedItem(value);
    }

    @Override
    public PropertyEditorType getType() {
        return PropertyEditorType.LIST;
    }

    @Override
    public boolean isReadOnly() {
        if (defaultEditor==null) init();
        return defaultEditor.isEnabled();
    }

    @Override
    public void setReadOnly(boolean b) {
        if (defaultEditor==null) init();
        defaultEditor.setEnabled(!b);
    }

    @Override
    public void free() {
        defaultEditor = null;
    }

}
