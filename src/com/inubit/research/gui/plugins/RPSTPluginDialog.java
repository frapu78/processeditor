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
import com.inubit.research.rpst.exceptions.SinkNodeException;
import com.inubit.research.rpst.exceptions.SourceNodeException;
import com.inubit.research.rpst.mapping.BPMNMapping;
import com.inubit.research.rpst.mapping.MappedRPST;
import com.inubit.research.rpst.mapping.MappedTriconnectedComponent;
import com.inubit.research.rpst.mapping.Mapping;
import com.inubit.research.rpst.mapping.RPSTRegionMarker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 *
 * @author fel
 */
public class RPSTPluginDialog extends javax.swing.JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = -24237544377585560L;
	private Workbench workbench;
    private ProcessEditor currentEditor;
    private MappedRPST rpst;
    private DefaultMutableTreeNode lastSelected;
    private Map<MappedTriconnectedComponent, RPSTRegionMarker> markers = new HashMap<MappedTriconnectedComponent, RPSTRegionMarker>();

    /** Creates new form RPSTPluginDialog */
    public RPSTPluginDialog(Workbench workbench, boolean modal) {
        super(workbench, modal);
        this.workbench = workbench;
        this.currentEditor = this.workbench.getSelectedProcessEditor();
        initComponents();

        DefaultTreeCellRenderer tcr = new DefaultTreeCellRenderer();
        tcr.setLeafIcon( new ImageIcon( this.getClass().getResource("/menu/mini_arrow_top_right.gif") ));

        tree.setCellRenderer(tcr);

        tree.addTreeSelectionListener( new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if ( selectedNode != null && !selectedNode.isLeaf() ) { // only highlight compounds, no single edges
                    if ( lastSelected != null ) {
                        if ( !visualizeRegions.isSelected() )
                            dropRegionMarkers( currentEditor ); //deselect everything
                        else {
                        	//retrieve process node
                            RPSTRegionMarker rm = markers.get( (MappedTriconnectedComponent) lastSelected.getUserObject() );
                            //highlight it!
                            if ( rm != null )
                                rm.setHighlighted(false);
                            initiateRepaint();
                        }
                    }

                    lastSelected = selectedNode;

                    if ( !visualizeRegions.isSelected() )
                        visualizeRegion( (MappedTriconnectedComponent) selectedNode.getUserObject() );

                    RPSTRegionMarker rm = markers.get( (MappedTriconnectedComponent) selectedNode.getUserObject() );
                    if ( rm != null ) {
                        rm.setHighlighted(true);
                    }
                }

                initiateRepaint();
            }
        });


        this.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                dropRegionMarkers(currentEditor);
            }
        });

    }

    public void editorChanged( ProcessEditor editor ) {
        try {
            this.dropRegionMarkers(currentEditor);
            this.currentEditor = editor;
            this.refresh();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public void refresh() throws Exception {
        ProcessModel pm = this.workbench.getSelectedModel();

        nameLabel.setText( pm.getProcessName() );

        Mapping map = null;
        if ( pm instanceof BPMNModel )
            map = new BPMNMapping( (BPMNModel) pm );

        try {
            if ( map != null )
                rpst = new MappedRPST(map);
            else
                rpst = null;

            DefaultMutableTreeNode root = null;

            if ( rpst == null ) {
                root = this.createNoDecompositionNode();
            } else {
                root = new DefaultMutableTreeNode( rpst.getRoot() );
                this.addChildren( root, rpst.getRoot() );
            }

            TreeModel tm  = new DefaultTreeModel(root);
            tree.setModel(tm);

            tree.expandRow(0);
            this.repaint();
        } catch ( SinkNodeException e ) {
            JOptionPane.showMessageDialog( this, "Please add at least one end event and connect it to the graph!");
            TreeModel tm  = new DefaultTreeModel( createNoDecompositionNode() );
            tree.setModel(tm);
            throw e;
        } catch ( SourceNodeException e ) {
            JOptionPane.showMessageDialog( this, "Please add at least one start event and connect it to the graph!");
            TreeModel tm  = new DefaultTreeModel( createNoDecompositionNode() );
            tree.setModel(tm);
            throw e;
        }
    }

    private DefaultMutableTreeNode createNoDecompositionNode() {
        DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode("No decomposition available");

        return newRoot;
    }

    private void addChildren( DefaultMutableTreeNode tn , MappedTriconnectedComponent tc ) {
        for ( MappedTriconnectedComponent childCmp : tc.getChildren() ) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode( childCmp );
            this.addChildren( childNode , childCmp );

            tn.add(childNode);
        }

        List<ProcessEdge> edges = tc.getEdges();

        for ( ProcessEdge edge : edges ) {
            DefaultMutableTreeNode edgeChild = new DefaultMutableTreeNode( edge );
            tn.add( edgeChild );
        }

        if ( visualizeRegions.isSelected() ) {
            this.visualizeRegion( tc );
        }
    }

    private void visualizeRegion( MappedTriconnectedComponent mtc ) {
       Rectangle out = mtc.getOutline();

       RPSTRegionMarker rm = new RPSTRegionMarker( new Point( out.x, out.y ), new Point( out.x + out.width, out.y + out.height ), mtc.toString() );
       this.markers.put( mtc, rm );

       this.workbench.getSelectedProcessEditor().addProcessHelper(rm);
       this.workbench.getSelectedProcessEditor().repaint();
    }

    private void highlightRegions( MappedTriconnectedComponent mtc ) {
        this.visualizeRegion( mtc );
        for ( MappedTriconnectedComponent child : mtc.getChildren() )
            this.highlightRegions( child );
    }

    private void initiateRepaint() {
        workbench.getSelectedProcessEditor().repaint();
    }

    private void dropRegionMarkers( ProcessEditor editor ) {
        for ( RPSTRegionMarker marker : this.markers.values() )
            editor.removeProcessHelper(marker);

        this.markers.clear();
        editor.repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jButton1 = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        visualizeRegions = new javax.swing.JCheckBox();

        jTree1.setAutoscrolls(true);
        jScrollPane1.setViewportView(jTree1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("RPST Decomposition");

        jButton1.setText("Refresh");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        nameLabel.setText("ProcessModelName");

        jCheckBox1.setText("Consider message  flows");
        jCheckBox1.setEnabled(false);
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jScrollPane2.setViewportView(tree);

        visualizeRegions.setSelected(true);
        visualizeRegions.setText("Visualize regions");
        visualizeRegions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                visualizeRegionsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(visualizeRegions)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                    .addComponent(nameLabel)
                    .addComponent(jButton1)
                    .addComponent(jCheckBox1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addGap(7, 7, 7)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(visualizeRegions)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
	 * @param evt  
	 */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            dropRegionMarkers( currentEditor );
            refresh();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
	 * @param evt  
	 */
    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    /**
	 * @param evt  
	 */
    private void visualizeRegionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_visualizeRegionsActionPerformed
        if ( visualizeRegions.isSelected() ) {
            if ( rpst != null )
                highlightRegions( rpst.getRoot() );
        } else {
            dropRegionMarkers( currentEditor );
        }
    }//GEN-LAST:event_visualizeRegionsActionPerformed

//    /**
//    * @param args the command line arguments
//    */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                RPSTPluginDialog dialog = new RPSTPluginDialog(new javax.swing.JFrame(), true);
//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        System.exit(0);
//                    }
//                });
//                dialog.setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTree jTree1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTree tree;
    private javax.swing.JCheckBox visualizeRegions;
    // End of variables declaration//GEN-END:variables

}
