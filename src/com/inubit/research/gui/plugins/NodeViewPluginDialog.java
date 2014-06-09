/**
 *
 * Process Editor - inubit Workbench Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins;

import com.inubit.research.gui.Workbench;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author frank
 */
public class NodeViewPluginDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 8463217948776515402L;
    private Map<DefaultMutableTreeNode, ProcessObject> nodeMap = new HashMap<DefaultMutableTreeNode, ProcessObject>();
    private ProcessEditor currentEditor;
    //private ProcessModel currentModel;
    private Workbench workbench;
    private String filter = "";

    /** Creates new form NodeViewPluginDialog */
    public NodeViewPluginDialog(Workbench parent, boolean modal) {
        super(parent, modal);
        workbench = parent;
        initComponents();
    }

    public void setEditor(ProcessEditor editor) {
        currentEditor = editor;
        currentEditor.setInvisibleAlpha(0.2f);
        ProcessModel model = editor.getModel();
        //currentModel = model;

        String uri = model.getProcessModelURI();
        if (uri != null) {
            modelName.setText(uri.substring(uri.lastIndexOf(File.separator) + 1));
        }

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        DefaultMutableTreeNode nodesNode = addNodes(model);
        DefaultMutableTreeNode edgesNodes = addEdges(model);
        rootNode.add(nodesNode);
        rootNode.add(edgesNodes);
        
        TreeModel treeModel = new DefaultTreeModel(rootNode);
        tree.setModel(treeModel);
        for (int i = tree.getRowCount() - 1; i >= 0; i--) {
            tree.expandRow(i);
        }
        
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                if (node == null) {
                    return;
                }

                ProcessObject hNode = nodeMap.get(node);
                if(hNode != null) {
	                // Highlight in editor
	                currentEditor.getSelectionHandler().clearSelection();
	                currentEditor.getSelectionHandler().addSelectedObject(hNode);
	                if(hNode instanceof ProcessNode)
	                	workbench.setViewportToPoint(((ProcessNode)hNode).getPos());
	                else
	                	workbench.setViewportToPoint(((ProcessEdge)hNode).getRoutingPoints().get(0));
	                //currentEditor.repaint();
                }

            }
        });
        this.repaint();

    }

	/**
	 * @param model
	 */
	private DefaultMutableTreeNode addEdges(ProcessModel model) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Edges");

        // Collect contained types
        Map<String, List<ProcessEdge>> edgeType = new HashMap<String, List<ProcessEdge>>();
        for (ProcessEdge edge : model.getEdges()) {
            String key = edge.getClass().getSimpleName();
            if (!edgeType.containsKey(key)) {
                // Create new entry
            	edgeType.put(key, new LinkedList<ProcessEdge>());
            }
            List<ProcessEdge> nodeList = edgeType.get(key);
            nodeList.add(edge);
        }

        // Insert contained types
        for (String parentNode : edgeType.keySet()) {
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(parentNode + " (" + edgeType.get(parentNode).size() + ")");
            for (ProcessEdge edge : edgeType.get(parentNode)) {
                String label = edge.toString() + "(" + edge.getProperty(ProcessObject.PROP_ID) + ")";
                DefaultMutableTreeNode entry = new DefaultMutableTreeNode(label);
                nodeMap.put(entry, edge);
                // Add only if matching to filter
                if (filter.length() == 0 | label.toString().toLowerCase().contains(filter)) {
                    classNode.add(entry);
                }
            }
            if (classNode.getChildCount() > 0) {
                rootNode.add(classNode);
            }
        }

        return rootNode;       
	}

	private DefaultMutableTreeNode addNodes(ProcessModel model) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Nodes");

        // Set all ProcessNodes to visible/invisible
        for (ProcessNode node: model.getNodes()) {
            node.setVisible(!applyFilterToModel.isSelected());
        }

        // Collect contained types
        Map<String, List<ProcessNode>> nodeType = new HashMap<String, List<ProcessNode>>();
        for (ProcessNode node : model.getNodes()) {
            String key = node.getClass().getSimpleName();
            if (sortByStereotypes.isSelected()) {
                key = node.getStereotype();
            }

            if (!nodeType.containsKey(key)) {
                // Create new entry
                nodeType.put(key, new LinkedList<ProcessNode>());
            }
            List<ProcessNode> nodeList = nodeType.get(key);
            nodeList.add(node);
        }

        // Insert contained types
        for (String parentNode : nodeType.keySet()) {
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(parentNode + " (" + nodeType.get(parentNode).size() + ")");
            for (ProcessNode node : nodeType.get(parentNode)) {
                String label = node.getText() + "(" + node.getProperty(ProcessObject.PROP_ID) + ")";
                DefaultMutableTreeNode entry = new DefaultMutableTreeNode(label);
                nodeMap.put(entry, node);
                // Add only if matching to filter
                if (filter.length() == 0 | label.toString().toLowerCase().contains(filter)) {
                    classNode.add(entry);
                    if (applyFilterToModel.isSelected()) {
                        node.setVisible(true);
                    }
                }
            }
            if (classNode.getChildCount() > 0) {
                rootNode.add(classNode);
            }
        }
        
        return rootNode;
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        modelName = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        filterTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        sortByStereotypes = new javax.swing.JCheckBox();
        applyFilterToModel = new javax.swing.JCheckBox();

        setTitle("ProcessModel Node View");

        jScrollPane1.setViewportView(tree);

        modelName.setText("ProcessModelName");

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        filterTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterTextFieldActionPerformed(evt);
            }
        });
        filterTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                filterTextFieldKeyTyped(evt);
            }
        });

        jLabel1.setText("Search:");

        sortByStereotypes.setText("Sort by stereotypes");
        sortByStereotypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByStereotypesActionPerformed(evt);
            }
        });

        applyFilterToModel.setText("Apply filter to model");
        applyFilterToModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyFilterToModelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sortByStereotypes)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(modelName, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                        .addComponent(refreshButton))
                    .addComponent(applyFilterToModel)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modelName)
                    .addComponent(refreshButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sortByStereotypes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(applyFilterToModel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
	 * @param evt  
	 */
    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        setEditor(currentEditor);
        currentEditor.repaint();
    }//GEN-LAST:event_refreshButtonActionPerformed

    /**
	 * @param evt  
	 */
    private void filterTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterTextFieldActionPerformed
    }//GEN-LAST:event_filterTextFieldActionPerformed

    /**
	 * @param evt  
	 */
    private void filterTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterTextFieldKeyTyped
        filter = filterTextField.getText().toLowerCase();
        setEditor(currentEditor);
        currentEditor.repaint();
    }//GEN-LAST:event_filterTextFieldKeyTyped

    /**
	 * @param evt  
	 */
    private void sortByStereotypesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByStereotypesActionPerformed
        // TODO add your handling code here:
        setEditor(currentEditor);
    }//GEN-LAST:event_sortByStereotypesActionPerformed

    /**
	 * @param evt  
	 */
    private void applyFilterToModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyFilterToModelActionPerformed
        setEditor(currentEditor);
        currentEditor.repaint();
    }//GEN-LAST:event_applyFilterToModelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox applyFilterToModel;
    private javax.swing.JTextField filterTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel modelName;
    private javax.swing.JButton refreshButton;
    private javax.swing.JCheckBox sortByStereotypes;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables
}
