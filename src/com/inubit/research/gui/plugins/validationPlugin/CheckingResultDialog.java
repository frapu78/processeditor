/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.validationPlugin;

import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.WorkbenchEditorListener;
import com.inubit.research.validation.ValidationMessage;
import com.inubit.research.validation.Validator;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessModelListener;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author tmi
 */
public class CheckingResultDialog extends javax.swing.JDialog {
    
    private Validator validator;
    private Collection<ProcessObject> highlightedObjects =
            new HashSet<ProcessObject>();
    private ProcessObject primaryHighlightedObject;
    private Color originalColorOfPrimaryObject;
    private List<ValidationMessage> shownMessages;

    private ProcessModel currentModel;
    private ProcessModelListener listener;

    private final String[] ignoredPropertyChanges = {ProcessNode.PROP_BACKGROUND,
        ProcessNode.PROP_HEIGHT, ProcessNode.PROP_SHADOW, ProcessNode.PROP_WIDTH,
        ProcessNode.PROP_XPOS, ProcessNode.PROP_YPOS,
        ProcessEdge.PROP_COLOR_ARC, ProcessEdge.PROP_LABELOFFSET,
        ProcessEdge.PROP_POINTS, ProcessEdge.PROP_SOURCE_DOCKPOINT,
        ProcessEdge.PROP_TARGET_DOCKPOINT};

    /** Creates new form CheckingResultDialog */
    public CheckingResultDialog(Workbench parent, Validator validator) {
        super(parent, false);
        initComponents();
        this.validator = validator;
        updateMessages();
        fitPositionToParent();

        selectedModelChanged();
        getWorkbench().addWorkbenchEditorListener(new WorkbenchEditorListener() {

            @Override
            public void newEditorCreated(ProcessEditor editor) {
                selectedModelChanged();
            }

            @Override
            public void selectedProcessEditorChanged(ProcessEditor editor) {
                selectedModelChanged();
            }
        });
    }

    protected void selectedModelChanged() {
        if (listener != null && currentModel != null) {
            currentModel.removeListener(listener);
        }
        currentModel = getWorkbench().getSelectedModel();
        listener =new ProcessModelListener() {
            @Override
            public void processNodeAdded(ProcessNode newNode) {
                doRevalidate();
            }
            @Override
            public void processNodeRemoved(ProcessNode remNode) {
                doRevalidate();
            }
            @Override
            public void processEdgeAdded(ProcessEdge edge) {
                doRevalidate();
            }
            @Override
            public void processEdgeRemoved(ProcessEdge edge) {
                doRevalidate();
            }
            @Override
            public void processObjectPropertyChange(ProcessObject obj,
                    String name, String oldValue, String newValue) {
                if (!Arrays.asList(ignoredPropertyChanges).contains(name)) {
                    doRevalidate();
                }
            }
        };
        //currentModel.addListener(listener);
        validator = new Validator(currentModel, ValidationPlugin.getInstance(getWorkbench()).getSupportedModels());
        doRevalidate();
    }

    private Workbench getWorkbench() {
        return (Workbench) getParent();
    }

    private void updateMessages() {
        fillTable();
        updateSummaryLabel();
    }

    private void fillTable() {
        String[] columns = {"Type", "Description"};
        shownMessages = new LinkedList<ValidationMessage>();
        DefaultTableModel model = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (ValidationMessage message : validator.getMessages(
                viewErrorsCheckbox.isSelected(),
                viewWarningsCheckbox.isSelected(),
                viewInformationCheckbox.isSelected())) {
            String[] row = {messageTypeString(message), message.getShortDescription()};
            model.addRow(row);
            shownMessages.add(message);
        }
        messagesTable.setModel(model);
        messagesTable.getColumnModel().getColumn(0).setMinWidth(75);
        messagesTable.getColumnModel().getColumn(0).setMaxWidth(90);
        messagesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
    }

    private void updateSummaryLabel() {
        summaryLabel.setText(validator.getAllMessages().size() + " messages: "
                + validator.getErrorMessages().size() + " errors, "
                + validator.getWarningMessages().size() + " warnings, "
                + validator.getInformationMessages().size() + " information-messages");
    }

    private String messageTypeString(ValidationMessage message) {
        if(message.getType() == ValidationMessage.TYPE_INFO) {
            return "Information";
        } else if (message.getType() == ValidationMessage.TYPE_WARNING) {
            return "Warning";
        } else {
            return "Error";
        }
    }

    private void fitPositionToParent() {
        Rectangle screenBounds = getParent().getGraphicsConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
                getParent().getGraphicsConfiguration());
        screenBounds = new Rectangle((int)screenBounds.getX() - insets.left,
                (int)screenBounds.getY() - insets.top,
               (int) screenBounds.getWidth() - insets.left - insets.right,
               (int) screenBounds.getHeight() - insets.top - insets.bottom);
        Rectangle parentBounds = getParent().getBounds();
        if(parentBounds.getMaxX() + getWidth() <= screenBounds.getMaxX()) {
            setLocation((int) parentBounds.getMaxX(), (int) parentBounds.getY());
        } else if (parentBounds.getX() - getWidth() >= screenBounds.getX()) {
            setLocation((int)parentBounds.getX() - getWidth(),
                                                (int) parentBounds.getY());
        } else if (parentBounds.getMaxY() + getHeight() <= screenBounds.getMaxY()) {
            setLocation((int)parentBounds.getX(), (int) parentBounds.getMaxY());
        } else if (parentBounds.getY() - getHeight() >= screenBounds.getY()) {
            setLocation((int)parentBounds.getX(),
                                (int) parentBounds.getY() - getHeight());
        } else {
            setLocation((int) screenBounds.getMaxX() - getWidth(),
                    (int) screenBounds.getMaxY() - getHeight());
        }
    }

    private void selectionChanged() {
        if (messagesTable.getSelectedRow() == -1) {
            removeHighlighting();
            descriptionPane.setText("Select an element to view its full "
                    + "description and highlight all related process objects");
        } else {
            ValidationMessage selectedMessage = shownMessages.get(
                    messagesTable.getSelectedRow());
            descriptionPane.setText(selectedMessage.getDescription());
            removeHighlighting();
            highlightedObjects = new HashSet<ProcessObject>(
                                    selectedMessage.getInvolvedObjects());
            if (selectedMessage.hasPrimaryObject()) {
                highlightPrimaryObject(selectedMessage.getPrimaryObject());
            }
            for (ProcessObject object : highlightedObjects) {
                object.setHighlighted(true);
            }
            getWorkbench().repaint();
        }
    }

    private void removeHighlighting() {
        for(ProcessObject object : highlightedObjects) {
            object.setHighlighted(false);
        }
        if (primaryHighlightedObject != null) {
            if (primaryHighlightedObject instanceof ProcessNode) {
                ((ProcessNode)primaryHighlightedObject).
                        setBackground(originalColorOfPrimaryObject);
                primaryHighlightedObject = null;
            } else if (primaryHighlightedObject instanceof ProcessEdge) {
                ((ProcessEdge)primaryHighlightedObject).setColor(
                        originalColorOfPrimaryObject);
                primaryHighlightedObject = null;
            }
        }
        getWorkbench().repaint();
    }

    private void highlightPrimaryObject(ProcessObject primaryObject) {
        if (primaryObject instanceof ProcessNode) {
            primaryHighlightedObject = primaryObject;
            originalColorOfPrimaryObject =
                    ((ProcessNode)primaryObject).getBackground();
            ((ProcessNode)primaryObject).setBackground(new Color(255, 208, 0));
        } else if (primaryObject instanceof ProcessEdge) {
            primaryHighlightedObject = primaryObject;
            originalColorOfPrimaryObject =
                    ((ProcessEdge)primaryObject).getColor();
            ((ProcessEdge)primaryObject).setColor(new Color(255, 208, 0));
        }
    }

    private void doRevalidate() {
        validator.performCheck();
        updateMessages();
        selectionChanged();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        messagesTable = new javax.swing.JTable();
        closeButton = new javax.swing.JButton();
        revalidateButton = new javax.swing.JButton();
        summaryLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descriptionPane = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        viewErrorsCheckbox = new javax.swing.JCheckBox();
        viewWarningsCheckbox = new javax.swing.JCheckBox();
        viewInformationCheckbox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Validation Result");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        messagesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Type", "Short description"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        messagesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        messagesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                messagesTableMousePressed(evt);
            }
        });
        messagesTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                messagesTableKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(messagesTable);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        revalidateButton.setText("Revalidate");
        revalidateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revalidateButtonActionPerformed(evt);
            }
        });

        summaryLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        summaryLabel.setText("0 messages: 0 errors, 0 warnings, 0 information-messages");

        jScrollPane2.setBorder(null);
        jScrollPane2.setOpaque(false);

        descriptionPane.setBorder(null);
        descriptionPane.setEditable(false);
        descriptionPane.setText("Select an element to view its full description and highlight all related process objects");
        descriptionPane.setOpaque(false);
        jScrollPane2.setViewportView(descriptionPane);

        jLabel1.setText("View:");

        viewErrorsCheckbox.setSelected(true);
        viewErrorsCheckbox.setText("Errors");
        viewErrorsCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewErrorsCheckboxActionPerformed(evt);
            }
        });

        viewWarningsCheckbox.setSelected(true);
        viewWarningsCheckbox.setText("Warnings");
        viewWarningsCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewWarningsCheckboxActionPerformed(evt);
            }
        });

        viewInformationCheckbox.setSelected(true);
        viewInformationCheckbox.setText("Information messages");
        viewInformationCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewInformationCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(viewErrorsCheckbox)
                        .addGap(18, 18, 18)
                        .addComponent(viewWarningsCheckbox)
                        .addGap(18, 18, 18)
                        .addComponent(viewInformationCheckbox))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(summaryLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(revalidateButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 213, Short.MAX_VALUE)
                        .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(summaryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(viewErrorsCheckbox)
                    .addComponent(viewWarningsCheckbox)
                    .addComponent(viewInformationCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(revalidateButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void messagesTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_messagesTableMousePressed
        selectionChanged();
    }//GEN-LAST:event_messagesTableMousePressed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        removeHighlighting();
        currentModel.removeListener(listener);
    }//GEN-LAST:event_formWindowClosed

    private void messagesTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_messagesTableKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_UP ||
                evt.getKeyCode() == KeyEvent.VK_DOWN) {
            selectionChanged();
        }
    }//GEN-LAST:event_messagesTableKeyReleased

    private void revalidateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revalidateButtonActionPerformed
        doRevalidate();
    }//GEN-LAST:event_revalidateButtonActionPerformed

    private void viewErrorsCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewErrorsCheckboxActionPerformed
        fillTable();
        selectionChanged();
    }//GEN-LAST:event_viewErrorsCheckboxActionPerformed

    private void viewWarningsCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewWarningsCheckboxActionPerformed
        fillTable();
        selectionChanged();
    }//GEN-LAST:event_viewWarningsCheckboxActionPerformed

    private void viewInformationCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewInformationCheckboxActionPerformed
        fillTable();
        selectionChanged();
    }//GEN-LAST:event_viewInformationCheckboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JTextPane descriptionPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable messagesTable;
    private javax.swing.JButton revalidateButton;
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JCheckBox viewErrorsCheckbox;
    private javax.swing.JCheckBox viewInformationCheckbox;
    private javax.swing.JCheckBox viewWarningsCheckbox;
    // End of variables declaration//GEN-END:variables

}
