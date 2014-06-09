/**
 *
 * Process Editor - Simulation Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.simulation.petrinets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * This class implements a generic properties panel for ProcessEditor nodes.
 * 
 * @author  frank
 */
public class InspectorPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 2871247435251434437L;

	protected Map<String,String> props;

    // Map for storing keys and JTextFields
    final Map<String,JTextField> data = new HashMap<String,JTextField>();
    
    /** Creates new form PropertyPanel */
    public InspectorPanel(Map<String,String> props) {
        this.props = props;
        initCustomComponents();
    }

    public Map<String, JTextField> getData() {
        return data;
    }

    
    /**
     * Initializes the properties panel according to the ProcessNodes's 
     * properties.
     */
    protected void initCustomComponents() {

        // Create a grid bag layout
        this.setLayout(
                new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        // Add text editor for all properties
        int y = 0;
        for (String key: props.keySet()) {
            // Create grid bag constraints
            c.gridx = 0;
            c.gridy = y;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;

            String value = props.get(key);
            // Add key
            this.add(new JLabel(key),c);
            
            // Add text field
            JTextField valueField = new JTextField();            
            c.gridx = 1;
            c.gridy = y++;
            c.fill = GridBagConstraints.HORIZONTAL;
            // Add value
            valueField.setText(value);
            valueField.setSize(100,valueField.getSize().height);
            // All fields are disabled
            valueField.setEnabled(false);

            // Add to panel
            this.add(valueField,c);

            // Add to data
            data.put(key, valueField);
        }
        // Add vertical filler
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        JPanel fillerPanel = new JPanel();
        this.add(fillerPanel,c);
    }
    
}
