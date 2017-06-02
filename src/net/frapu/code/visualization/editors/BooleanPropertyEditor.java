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
import java.util.Locale;
import javax.swing.JCheckBox;

/**
 *
 * @author frank
 */
public class BooleanPropertyEditor extends PropertyEditor {
    
    protected JCheckBox defaultEditor;

    public BooleanPropertyEditor() {
        super();
    }

    private void init() {
        defaultEditor= new JCheckBox();
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
        defaultEditor.setText(PropertyConfig.getPropertyLabel(getProcessObject(), getPropertyKey(), Locale.ENGLISH));        
        return defaultEditor;
    }

    @Override
    public void setValue(String value) {
        if (defaultEditor==null) init();
        defaultEditor.setSelected(value.equals("1")?true:false);
    }

    @Override
    public String getValue() {
        if (defaultEditor==null) init();
        return defaultEditor.isSelected()?"1":"0";
    }
    
    @Override
    public boolean containsLabel() {
        return true;
    }

    @Override
    public PropertyEditorType getType() {
        return PropertyEditorType.BOOLEAN;
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
