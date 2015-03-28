/**
 *
 * Process Editor - Domain Model Package
 *
 * (C) 2011 inubit AG
 * (C) 2014 the authors
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.domainModel;

import net.frapu.code.visualization.editors.*;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 *
 * @author fpu
 */
public class AttributePropertyEditor extends PropertyEditor {

    private JPanel defaultEditor;
    private JTextField attributeTextField;
    private JButton attributeEditorButton;

    public AttributePropertyEditor() {
        super();
    }

    private void init() {
        attributeTextField = new JTextField();
        attributeTextField.setEditable(false);
        attributeEditorButton = new JButton("...");
        attributeEditorButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Show reference picker here
//                ReferenceChooser ch = new ReferenceChooser(null, true, restrictions);
//                ch.setLinkToHeadVersion(!attributeTextField.getText().contains("/version"));
//                ch.setUri(attributeTextField.getText());
//                SwingUtils.center(ch);
//                ch.setVisible(true);
//                if (ch.getResult()!=null) {
//                    attributeTextField.setText(ch.getResult());
//                }
            }
        });

        defaultEditor = new JPanel();
        defaultEditor.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 95;
        gbc.weighty = 100;
        gbc.gridx = 0;
        gbc.gridy = 0;
        defaultEditor.add(attributeTextField, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        defaultEditor.add(attributeEditorButton, gbc);
    }

    @Override
    public Component getComponent() {
        if (defaultEditor == null) {
            init();
        }
        return defaultEditor;
    }

    @Override
    public void setValue(String value) {
        if (defaultEditor == null) {
            init();
        }
        // Set color in TextField
        attributeTextField.setText(value);
    }

    @Override
    public String getValue() {
        if (defaultEditor == null) {
            init();
        }
        return attributeTextField.getText();
    }

    @Override
    public PropertyEditorType getType() {
        return PropertyEditorType.XSDELEMENT;
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
        attributeEditorButton.setEnabled(!b);
        attributeTextField.setEnabled(!b);
    }

    @Override
    public void free() {
        defaultEditor = null;
    }
}
