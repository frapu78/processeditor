/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * XFormsWizard.java
 *
 * Created on 03.12.2009, 10:04:26
 */

package net.frapu.code.visualization.xforms;

import java.awt.GridLayout;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.uml.UMLClass;

/**
 *
 * @author fpu
 */
public class XFormsWizard extends javax.swing.JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1915176194961795599L;
	public final static String ACTION_ROOT = "action_root";
    public final static String ACTION_ENTRY = "action_entry";

    private ProcessEditor editor;
    private XFormsModel form = new XFormsModel();
    private ProcessModel bo;
    private DefaultTreeModel boModel;
    private Set<ProcessNode> processedNodes = new HashSet<ProcessNode>();;
    private XFormsWizardTransferHandler transferHandler;
    private DefaultTreeModel actionModel;
    private ProcessModel resultModel;

    /** Creates new form XFormsWizard */
    public XFormsWizard(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        init();
    }

    private void init() {
        editor = new ProcessEditor();
        editor.setModel(form);
        editor.setAnimationEnabled(true);
        editorPane.setLayout(new GridLayout(1,1));
        editorPane.add(editor);
        transferHandler = new XFormsWizardTransferHandler(editor);
        editor.setTransferHandler(transferHandler);
        XFormsWizardTreeCellRenderer cr = new XFormsWizardTreeCellRenderer();
        boTree.setCellRenderer(cr);
        boTree.setDragEnabled(true);
        boTree.setTransferHandler(transferHandler);
        DefaultTreeSelectionModel selModel = new DefaultTreeSelectionModel();
        selModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
        boTree.setSelectionModel(selModel);
        actionTree.setCellRenderer(cr);
        actionTree.setDragEnabled(true);
        actionTree.setTransferHandler(transferHandler);
        actionTree.setRootVisible(false);
    }

    public void setData(String title, ProcessModel businessObject, ProcessNode rootProcessNode, List<String> actions) {
        editor.setModel(new XFormsModel());
        bo = businessObject;
        transferHandler.setBO(bo);
        // Build tree model from ClassDiagram
        processedNodes.clear();
        DefaultMutableTreeNode rootNode = createTreeModel(businessObject, rootProcessNode);
        boModel = new DefaultTreeModel(rootNode);
        boTree.setModel(boModel);
        // Set actions
        DefaultMutableTreeNode actionRoot = new DefaultMutableTreeNode(new Tuple(ACTION_ROOT,"Actions"));
        for (String action: actions) {
            DefaultMutableTreeNode actionNode = new DefaultMutableTreeNode(new Tuple(ACTION_ENTRY, action));
            actionRoot.add(actionNode);
        }
        actionModel = new DefaultTreeModel(actionRoot);
        actionTree.setModel(actionModel);
        // Create default Panel if title != null
        if (title != null) {
            Panel p = new Panel();
            p.setText(title);
            p.setSize(300,200);
            p.setPos(p.getSize().width/2+50, p.getSize().height/2+20);
            editor.getModel().addNode(p);
        }
    }

    private DefaultMutableTreeNode createTreeModel(ProcessModel businessObject, ProcessNode rootProcessNode) {
        if (processedNodes.contains(rootProcessNode)) return null;
        if (!(rootProcessNode instanceof UMLClass)) return null;
        processedNodes.add(rootProcessNode);
        // 1st: Create new tree entry
        DefaultMutableTreeNode result = new DefaultMutableTreeNode(rootProcessNode);
        // 2nd: Process all outgoing edges
        for (ProcessNode n: businessObject.getSuccessors(rootProcessNode) ) {
            // Add as siblings
            DefaultMutableTreeNode nextNode = createTreeModel(businessObject, n);
            if (nextNode!=null) result.add(nextNode);
        }
        // 3rd: Add attributes
        StringTokenizer st = new StringTokenizer(rootProcessNode.getProperty(UMLClass.PROP_ATTRIBUTES),UMLClass.ELEMENT_DELIMITER);
        while (st.hasMoreElements()) {
            String nextAttribute = st.nextToken();
            // Create sibling
            DefaultMutableTreeNode nextNode = new DefaultMutableTreeNode(nextAttribute);
            result.add(nextNode);
        }
        return result;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        boTree = new javax.swing.JTree();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        actionTree = new javax.swing.JTree();
        cbAutoLayout = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Form Wizard");

        okButton.setLabel("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setLabel("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jSplitPane1.setDividerLocation(400);
        jSplitPane1.setOneTouchExpandable(true);

        javax.swing.GroupLayout editorPaneLayout = new javax.swing.GroupLayout(editorPane);
        editorPane.setLayout(editorPaneLayout);
        editorPaneLayout.setHorizontalGroup(
            editorPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 397, Short.MAX_VALUE)
        );
        editorPaneLayout.setVerticalGroup(
            editorPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 453, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(editorPane);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jScrollPane2.setViewportView(boTree);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Business Object:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("Actions:");

        jScrollPane3.setViewportView(actionTree);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel1);

        cbAutoLayout.setSelected(true);
        cbAutoLayout.setText("Always Auto-Layout");
        cbAutoLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAutoLayoutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbAutoLayout)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 396, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelButton)
                        .addComponent(okButton))
                    .addComponent(cbAutoLayout))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        resultModel = null;
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        resultModel = editor.getModel();
        editor.dispose();
        this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cbAutoLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAutoLayoutActionPerformed
        transferHandler.setAutoLayout(cbAutoLayout.isSelected());
    }//GEN-LAST:event_cbAutoLayoutActionPerformed

    public ProcessModel getResult() {
        return resultModel;
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) throws Exception {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                                
                try {
                    
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                    XFormsWizard dialog = new XFormsWizard(new javax.swing.JFrame(), true);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                        public void windowClosing(java.awt.event.WindowEvent e) {
                            System.exit(0);
                        }
                    });

                    //model = ProcessUtils.parseProcessModelSerialization(new URI("http://res-proto:1205/models/5244814"));
                    //ProcessNode node = model.getNodeById("17209834");
                    ProcessModel model = ProcessUtils.parseProcessModelSerialization(new URI("http://127.0.0.1:1205/models/28259286"));
                    ProcessNode node = model.getNodeById("24392121");

                    List<String> actions = new LinkedList<String>();
                    actions.add("Ok");
                    actions.add("Cancel");
                    actions.add("More Info");

                    dialog.setData("My form", model, node, actions);
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree actionTree;
    private javax.swing.JTree boTree;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox cbAutoLayout;
    private javax.swing.JPanel editorPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables


}
