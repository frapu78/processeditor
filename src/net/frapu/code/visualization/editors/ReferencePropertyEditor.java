/**
 *
 * Process Editor - Editor Package
 *
 * (C) 2008-2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.editors;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.SwingUtils;

/**
 *
 * @author fpu
 */
public class ReferencePropertyEditor extends PropertyEditor {

    private JPanel defaultEditor;
    private JTextField referenceTextField;
    private JButton referencePickButton;
    private ReferenceChooserRestriction restrictions;

    public ReferencePropertyEditor(ReferenceChooserRestriction restrictions) {
        super();
        this.restrictions = restrictions;
    }

    private void init() {
        referenceTextField = new JTextField();
        referencePickButton = new JButton("...");
        referencePickButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Show reference picker here
                ReferenceChooser ch = new ReferenceChooser(null, true, restrictions);
                ch.setLinkToHeadVersion(!referenceTextField.getText().contains("/version"));
                ch.setUri(referenceTextField.getText());
                SwingUtils.center(ch);
                ch.setVisible(true);
                if (ch.getResult()!=null) {
                    referenceTextField.setText(ch.getResult());
                }
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
        defaultEditor.add(referenceTextField,gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        defaultEditor.add(referencePickButton,gbc);
        referenceTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ProcessObject po = getProcessObject();
                if (po != null) {
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
    public void setValue(String value) {
        if (defaultEditor == null) {
            init();
        }
        // Set color in TextField
        referenceTextField.setText(value);
    }

    @Override
    public String getValue() {
        if (defaultEditor == null) {
            init();
        }
        return referenceTextField.getText();
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
        referencePickButton.setEnabled(!b);
        referenceTextField.setEnabled(!b);
    }

    @Override
    public void free() {
        defaultEditor = null;
    }
}
