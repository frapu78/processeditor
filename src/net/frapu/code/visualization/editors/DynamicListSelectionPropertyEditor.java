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
import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * Provides an editable drop-down box with dynamically retrieved values.
 *
 * @author frank
 */
public class DynamicListSelectionPropertyEditor extends PropertyEditor {

    protected JComboBox defaultEditor;
    protected ComboBoxModel model;
    protected ListDataSource data;
    protected boolean isEditable;

    /**
     * Creates a new dynamic List selection editor that can provide an editor for
     * custom values.
     * 
     * @param data
     * @param isEditable
     */
    public DynamicListSelectionPropertyEditor(ListDataSource data, boolean isEditable) {
        super();
        this.data = data;
        this.isEditable = isEditable;
    }

    private void init() {
        defaultEditor = new JComboBox();
        defaultEditor.setEditable(isEditable);
        defaultEditor.setSize(200, defaultEditor.getSize().height);
        updateData();
        defaultEditor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ProcessObject po = getProcessObject();
                if (po != null) {
                    po.setProperty(getPropertyKey(), getValue());
                }
            }
        });
    }

    public boolean isEditable() {
        return isEditable;
    }

    private void updateData() {
        // Retrieve current selection
        String current = getValue();
        Object item = defaultEditor.getSelectedItem();
        List<String> listData;
        if (data == null) {
            listData = new LinkedList<String>();
            listData.add(current);
        } else {
            listData = data.getListData();
            if (!listData.contains(current))
                if (isEditable) listData.add(0,current);
        }
        String[] values = new String[listData.size()];
        for (int pos = 0; pos < listData.size(); pos++) {
            values[pos] = listData.get(pos);
        }
        model = new DefaultComboBoxModel(values);
        defaultEditor.setModel(model);
        defaultEditor.setSelectedItem(item);
    }

    @Override
    public void update() {
        if (defaultEditor!=null) {
            super.update();
            updateData();
        }
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
        return (String) defaultEditor.getSelectedItem();
    }

    @Override
    public void setValue(String value) {
        if (defaultEditor == null) {
            init();
        }
        model.setSelectedItem(value);
    }

    @Override
    public PropertyEditorType getType() {
        return PropertyEditorType.DYNAMICLIST;
    }

    @Override
    public boolean isReadOnly() {
        if (defaultEditor == null) {
            init();
        }
        return defaultEditor.isEnabled();
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
