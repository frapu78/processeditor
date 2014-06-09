/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.frapu.code.visualization.xforms;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import net.frapu.code.converter.XSDImporter;
import net.frapu.code.visualization.uml.UMLClass;

/**
 *
 * @author fpu
 */
public class XFormsWizardTreeCellRenderer extends DefaultTreeCellRenderer {

    public XFormsWizardTreeCellRenderer() {
        super();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (!(value instanceof DefaultMutableTreeNode)) return new JLabel(value.toString());
        Object o = ((DefaultMutableTreeNode)value).getUserObject();
        JLabel result = new JLabel();
        if (o instanceof UMLClass) {
            UMLClass uc = (UMLClass)o;
            result.setText(uc.getText());
            if (uc.getStereotype().equals(XSDImporter.STEREOTYPE_ELEMENT))
                result.setIcon(new javax.swing.ImageIcon(getClass().getResource("/schema/e_small.png")));
            if (uc.getStereotype().equals(XSDImporter.STEREOTYPE_SIMPLETYPE))
                result.setIcon(new javax.swing.ImageIcon(getClass().getResource("/schema/st_small.png")));
            if (uc.getStereotype().equals(XSDImporter.STEREOTYPE_COMPLEXTYPE))
                result.setIcon(new javax.swing.ImageIcon(getClass().getResource("/schema/ct_small.png")));           
        } else
        if (o instanceof Tuple) {
            Tuple t = (Tuple)o;
            if (t.getKey().equals(XFormsWizard.ACTION_ROOT)) {
                result.setText(t.getValue());
            }
            if (t.getKey().equals(XFormsWizard.ACTION_ENTRY)) {
                result.setText(t.getValue());
                result.setIcon(new javax.swing.ImageIcon(getClass().getResource("/schema/action_small.png")));
            }
        } else {
            result.setText(o.toString());
        }
        if (selected) {
            result.setOpaque(true);
            result.setBackground(new Color(193,203,255));
        }
        return result;
    }

}
