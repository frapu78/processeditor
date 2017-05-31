/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008-2017 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.editors;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.frapu.code.visualization.ProcessObject;

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
        defaultEditor.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Update every stroke
                ProcessObject po = getProcessObject();
                if (po != null) {
                    int caretPos = defaultEditor.getCaretPosition();
                    String newValue = getValue().substring(0, caretPos) + 
                            e.getKeyChar() +
                            getValue().substring(caretPos);                   
                    po.setProperty(getPropertyKey(), newValue);
                }
                // Consume event here, since the property is already updated!
                //e.consume();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Ignore
            }

            @Override
            public void keyReleased(KeyEvent e) {
                 // Ignore
            }
        });
        /*
        defaultEditor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ProcessObject po = getProcessObject();
                if (po != null) {
                    po.setProperty(getPropertyKey(), getValue());
                }
            }
        });        */
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

    @Override
    public void update() {
        super.update(); //To change body of generated methods, choose Tools | Templates.
        
        if (!defaultEditor.hasFocus()) {
            // Get updated values from Process Object
            String updatedValue = getProcessObject().getProperty(getPropertyKey());
            setValue(updatedValue);
            }
    }
    
    
    
}
