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
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author fpu
 */
public class ColorPropertyEditor extends PropertyEditor {
    private JPanel defaultEditor;
    private JTextField colorTextField;
    private JButton colorPickerButton;
    private Color currentColor = null;

    public ColorPropertyEditor() {
        super();
    }

    private void init() {
        colorTextField = new JTextField();
        colorPickerButton = new JButton("...");
        colorPickerButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Show color picker here
                Color c = JColorChooser.showDialog(null, "Pick color", currentColor);
                if (c != null) {
                    setValue("" + c.getRGB());
                    ProcessObject po = getProcessObject();
                    if (po != null) {
                        po.setProperty(getPropertyKey(), getValue());
                    }
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
        defaultEditor.add(colorTextField,gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        defaultEditor.add(colorPickerButton,gbc);
        colorTextField.addActionListener(new ActionListener() {

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
        int colorValue = 0;
        try {
            colorValue = Integer.parseInt(value);
        } catch (Exception e) {
        }
        currentColor = new Color(colorValue);

        // Set color in TextField
        colorTextField.setText(currentColor.getRed() + "," +
                currentColor.getGreen() + "," +
                currentColor.getBlue());
    }

    @Override
    public String getValue() {
        if (defaultEditor == null) {
            init();
        }
        // Return value from color text field
        int r = 0, g = 0, b = 0;
        try {
            StringTokenizer st = new StringTokenizer(colorTextField.getText(), ",");
            r = Integer.parseInt(st.nextToken());
            g = Integer.parseInt(st.nextToken());
            b = Integer.parseInt(st.nextToken());
        } catch (Exception e) {
        }

        return "" + (new Color(r, g, b).getRGB());
    }

    @Override
    public PropertyEditorType getType() {
        return PropertyEditorType.COLOR;
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
        colorPickerButton.setEnabled(!b);
        colorTextField.setEnabled(!b);
    }

    @Override
    public void free() {
        defaultEditor = null;
    }
}
