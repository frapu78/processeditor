/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.extjs;

import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author fel
 */
public class ExtJSFormFactory {

    public static ExtJSForm createEmptyForm() {
        try {
            return new ExtJSForm();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtJSForm createForm( JSONArray items ) {
        try {
            return new ExtJSForm(items);
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtCheckBox createCheckBox () {
        try {
            return new ExtCheckBox();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtCheckBoxGroup createCheckBoxGroup(  ) {
        try {
            return new ExtCheckBoxGroup();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtCheckBoxGroup createCheckBoxGroup( JSONArray items ) {
        try {
            return new ExtCheckBoxGroup(items);
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static ExtRadioButton createRadioButton() {
        try {
            return new ExtRadioButton();
        }  catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static ExtRadioButtonGroup createRadioButtonGroup(  ) {
        try {
            return new ExtRadioButtonGroup();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static ExtCompositeField createCompositeField() {
        try {
            return new ExtCompositeField();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static ExtFieldSet createEmptyFieldSet(  ) {
        try {
            return new ExtFieldSet();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        } 
    }

    public static ExtTextField createTextField() {
        try {
            return new ExtTextField();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static ExtTextField createHiddenTextField() {
        try {
            ExtTextField tf = new ExtTextField();
            tf.setProperty(ExtJSProperty.HIDDEN, true);
            return tf;
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtGridPanel createGridPanel( ExtArrayStore store, ExtColumnModel model ) {
        try {
            return new ExtGridPanel( store, model );
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtArrayStore createArrayStore( ) {
        try {
            return new ExtArrayStore();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtArrayStore createArrayStore( JSONArray fields, JSONArray data ) {
        try {
            ExtArrayStore s = new ExtArrayStore();
            s.setFields(fields);
            s.setDataEntries(data);
            return s;
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtColumnModel createColumnModel() {
        return new ExtColumnModel();
    }

    public static ExtRowSelectionModel createRowSelectionModel() {
        try {
            return new ExtRowSelectionModel();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return  null;
        }
    }

    public static ExtLabel createLabel() {
        try {
            return new ExtLabel();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtTextArea createTextArea() {
        try {
            return new ExtTextArea();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtDisplayField createDisplayField() {
        try {
            return new ExtDisplayField();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtPanel createPanel() {
        try {
            return new ExtPanel();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtButton createButton() {
        try {
            return new ExtButton();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtPasswordField createPasswordField() {
        try {
            return new ExtPasswordField();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtToolbar createToolbar() {
        try {
            return new ExtToolbar();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtToolbarSeparator createToolbarSeparator() {
        try {
            return new ExtToolbarSeparator();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtToolbarSpacer createToolbarSpacer() {
        try {
            return new ExtToolbarSpacer();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtComboBox createComboBox() {
        try {
            return new ExtComboBox();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ExtNumberField createNumberField() {
        try {
            return new ExtNumberField();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static ExtTimeField createTimeField() {
        try {
            return new ExtTimeField();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static ExtDateField createDateField() {
        try {
            return new ExtDateField();
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            return null;
        }
    }
}
