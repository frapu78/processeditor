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

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * Returns a multi-line default Property Editor.
 *
 * @author frank
 */
public class MultiLinePropertyEditor extends PropertyEditor {

    protected JScrollPane scrollPane = null;
    protected JTextArea defaultEditor = null;

    public MultiLinePropertyEditor() {
        super();
    }

    private void init() {
        // Init
        defaultEditor = new JTextArea();
        defaultEditor.setColumns(20);
        defaultEditor.setRows(3);
        defaultEditor.setFont(new JLabel("bla").getFont());
        defaultEditor.setSize(100, defaultEditor.getSize().height);
        scrollPane = new JScrollPane(defaultEditor);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    @Override
    public Component getComponent() {
        if (defaultEditor == null) {
            init();
        }
        return scrollPane;
    }

    @Override
    public String getValue() {
        if (defaultEditor == null) {
            init();
        }
        return defaultEditor.getText();
    }

    @Override
    public PropertyEditorType getType() {
        return PropertyEditorType.MULTILINE;
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
