/**
 *
 * Process Editor - inubit Workbench Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins;

import java.awt.Component;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.editors.PropertyEditor;

/**
 *
 * @author fpu
 */
public class PropertiesTableModel implements 
        TableModel, TableCellRenderer, TableCellEditor {

    private ProcessObject processObject;
    private LinkedList<String> keys = new LinkedList<String>();

    public PropertiesTableModel(ProcessObject obj) {
        this.processObject = obj;

        // Sort properties
        List<String> pProps = new LinkedList<String>();
        List<String> sProps = new LinkedList<String>();
        Set<String> remainingKeys = obj.getPropertyKeys();

//        if (remainingKeys.contains(ProcessNode.PROP_TEXT)) {
//            remainingKeys.remove(ProcessNode.PROP_TEXT);
//            pProps.add(ProcessNode.PROP_TEXT);
//        }
//        if (remainingKeys.contains(ProcessEdge.PROP_LABEL)) {
//            remainingKeys.remove(ProcessEdge.PROP_LABEL);
//            pProps.add(ProcessEdge.PROP_LABEL);
//        }
//        if (remainingKeys.contains(ProcessNode.PROP_STEREOTYPE)) {
//            remainingKeys.remove(ProcessNode.PROP_STEREOTYPE);
//            pProps.add(ProcessNode.PROP_STEREOTYPE);
//        }
//        if (remainingKeys.contains(ProcessNode.PROP_XPOS)) {
//            remainingKeys.remove(ProcessNode.PROP_XPOS);
//            pProps.add(ProcessNode.PROP_XPOS);
//        }
//        if (remainingKeys.contains(ProcessNode.PROP_YPOS)) {
//            remainingKeys.remove(ProcessNode.PROP_YPOS);
//            pProps.add(ProcessNode.PROP_YPOS);
//        }
//        if (remainingKeys.contains(ProcessNode.PROP_WIDTH)) {
//            remainingKeys.remove(ProcessNode.PROP_WIDTH);
//            pProps.add(ProcessNode.PROP_WIDTH);
//        }
//        if (remainingKeys.contains(ProcessNode.PROP_HEIGHT)) {
//            remainingKeys.remove(ProcessNode.PROP_HEIGHT);
//            pProps.add(ProcessNode.PROP_HEIGHT);
//        }

        for (String key: remainingKeys) {
            // Find position to insert
            int pos=0;
            for (pos=0; pos<sProps.size(); pos++) {
                if (key.compareTo(sProps.get(pos))>0) continue;
                break;
            }
            sProps.add(pos, key);
        }

        keys.addAll(pProps);
        keys.addAll(sProps);
    }

    @Override
    public int getRowCount() {
        return keys.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex==0) return "Property";
        if (columnIndex==1) return "Value";
        return "?";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex==0) return JLabel.class;
        if (columnIndex==1) return JComponent.class;
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {

        if (columnIndex==1) {
            if (!keys.get(rowIndex).startsWith("#")) return true;
        }

        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex<0 || rowIndex>=keys.size()) return null;
        if (columnIndex==0) {
            JLabel prop = new JLabel(keys.get(rowIndex));
            if (prop.getText().startsWith("#")) prop.setEnabled(false);
            return prop;
        }
        if (columnIndex==1) {
            PropertyEditor editor = processObject.getPropertyEditor(keys.get(rowIndex));
            editor.setProcessObject(processObject, keys.get(rowIndex));
            editor.setReadOnly(keys.get(rowIndex).startsWith("#"));
            editor.setValue(processObject.getProperty(keys.get(rowIndex)));
            return editor;
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        System.out.println("Row="+rowIndex+", Col="+columnIndex);
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        //
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        //
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
       if (value instanceof PropertyEditor) return ((PropertyEditor)value).getComponent();
       return (Component)value; // Just cast Object to Component
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
       if (value instanceof PropertyEditor) return ((PropertyEditor)value).getComponent();
       return (Component)value; // Just cast Object to Component
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        return true;
    }

    @Override
    public void cancelCellEditing() {
        //
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        //
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        //
    }

}
