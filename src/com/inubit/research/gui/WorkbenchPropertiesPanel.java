/**
 *
 * Process Editor -  Workbench Package
 *
 * (C) 2017 Frank Puhlmann
 * 
 */
package com.inubit.research.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.ProcessObjectListener;
import net.frapu.code.visualization.PropertyConfig;

import net.frapu.code.visualization.editors.PropertyEditor;

/**
 *
 * This class implements a generic properties panel for ProcessEditor objects.
 * 
 * @author  frank
 */
public class WorkbenchPropertiesPanel extends javax.swing.JPanel implements ProcessObjectListener {

    private static final long serialVersionUID = -2953810066205643479L;
    protected ProcessObject po;
    // Map for storing keys and JTextFields
    final Map<String, PropertyEditor> data = new HashMap<String, PropertyEditor>();
    // Flag if fields could be edited
    boolean editable;

    /** Creates new form PropertyPanel */
    public WorkbenchPropertiesPanel(ProcessObject po, boolean editable) {
        this.po = po;
        this.editable = editable;
        initCustomComponents();
    }

    public Map<String, PropertyEditor> getData() {
        return data;
    }

    /**
     * Initializes the properties panel according to the ProcessObjects's
     * properties.
     */
    protected void initCustomComponents() {

        // Create a grid bag layout
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Sort properties
        List<String> sProps = new LinkedList<String>();
        for (String key : po.getPropertyKeys()) {
            // Find position to insert
            int pos = 0;
            for (pos = 0; pos < sProps.size(); pos++) {
                if (key.compareTo(sProps.get(pos)) > 0) {
                    continue;
                }
                break;
            }
            sProps.add(pos, key);
        }

        // Add editor for all properties
        int y = 0;
        for (String key : sProps) {
            // Create grid bag constraints
            c.gridx = 0;
            c.gridy = y++;
            c.anchor = GridBagConstraints.NORTHEAST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(2, 2, 0, 2);

            String value = po.getProperty(key);
            // Add key
            JLabel label = new JLabel( PropertyConfig.getPropertyLabel(po, key, Locale.ENGLISH) + ": ");
            label.setHorizontalTextPosition(JLabel.LEFT);
            label.setBackground(Color.WHITE);
            this.add(label, c);

            // Add PropertyEditor
            PropertyEditor editor = po.getPropertyEditor(key);
            if (/* key.startsWith("#") || */ !editable) {
                label.setEnabled(false);
                editor.setReadOnly(true);
            } else {
                editor.setReadOnly(false);
            }

            c.gridx = 0;
            c.gridy = y++;
            c.anchor = GridBagConstraints.EAST;
            c.fill = GridBagConstraints.HORIZONTAL;
            // Add value
            editor.setValue(value);
            // Set ProcessObject
            editor.setProcessObject(po, key);

            // Add to panel
            this.add(editor.getComponent(), c);

            // Add to data
            data.put(key, editor);
        }
        // Add vertical filler
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        JPanel fillerPanel = new JPanel();
        this.add(fillerPanel, c);

        // Add ProcessObject listener
        po.addListener(this);
    }

    public void dispose() {
        // Remove listener
        po.removeListener(this);
        // Free ressources
        for (PropertyEditor e: getData().values()) {
            e.free();
        }
        System.gc();
    }

    @Override
    public void propertyChanged(ProcessObject o, String key, String oldValue, String newValue) {
        // Update all editors
        for (PropertyEditor e: getData().values()) {
            e.update();
        }
    }
    // End of variables declaration                   
}
