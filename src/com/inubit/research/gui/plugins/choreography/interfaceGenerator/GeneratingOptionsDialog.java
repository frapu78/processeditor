/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.interfaceGenerator;

import com.inubit.research.gui.plugins.choreography.Utils;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.JCheckBox;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 * a dialog that queries the user for the following options on generating the
 * colaboration diagram:<br />
 * - which participants should be generated in full detail<br />
 * - should MessageFlowWithEnvelope be used<br />
 * - should the diagram be layouted
 * @author tmi
 */
class GeneratingOptionsDialog extends javax.swing.JDialog {

  private final int checkboxWidth = 350, checkboxHeight = 20,
          relativeLeftBorder = 10, relativeTopBorder = 20,
          absoluteLeftBorder, asoluteTopBorder;

  private BPMNModel choreography;
  private Collection<String> selectedParticipants;
  private Map<String, JCheckBox> checkboxes;
  private boolean successfull = false;
  
  
  public GeneratingOptionsDialog(
          java.awt.Frame parent, boolean modal, BPMNModel choreography,
          Point workbenchCenter) {
    super(parent, modal);
    initComponents();
    absoluteLeftBorder = participantSelectionPanel.getX() + relativeLeftBorder;
    asoluteTopBorder = participantSelectionPanel.getY() + relativeTopBorder;
    this.choreography = choreography;
    identifyParticipants();
    setPosition(workbenchCenter);
    generateButton.requestFocusInWindow();
  }

  public boolean aborted() {
    return ! successfull;
  }

  /**
   * did the user click the generate button?
   */
  public boolean successfull() {
    return successfull;
  }

  /**
   * @return the participants, that should be generated in detail (not as
   * blackbox pools)
   */
  public Collection<String> getSelectedParticipants() {
    return selectedParticipants;
  }

  /**
   * @return if the user selected to use MessageFlowWithEnvelope
   */
  public boolean messageFlowWithEnvelopeSelected() {
    return messageFlowWithEnvelopeCheckbox.isSelected();
  }

  /**
   * @return wheter the user likes the diagram layouted
   */
  public boolean wishesLayouting() {
    return layoutCheckbox.isSelected();
  }

  /**
   * @return wheter the user wants implicit splits and joins to be avoided
   */
  public boolean wishesImplicitSplitAndJoinAvoidance() {
    return avoidImplicitCheckbox.isSelected();
  }

  /**
   * creates a checkbox for each participant
   */
  private void identifyParticipants() {
    selectedParticipants = new HashSet<String>();
    checkboxes = new HashMap<String, JCheckBox>();
    int checkboxCount = 0;
    for(ProcessNode node : choreography.getNodes()) {
      for(String participant : Utils.participantsOf(node)) {
        if(!checkboxes.containsKey(participant)) {
          createCheckboxFor(participant, checkboxCount);
          ++checkboxCount;
        }
      }
    }
    resizeToFitParticipants(checkboxCount);
  }

  /**
   * resizes the participantSelectionPanel (Panel, that contains the checkboxes
   * with the names of the participants) and the dialog, so that they optimally
   * fit the number of participant-checkBoxes
   */
  private void resizeToFitParticipants(int participantCount) {
    participantSelectionPanel.setMinimumSize(
            new Dimension(0, participantCount * checkboxHeight));
    participantSelectionPanel.setPreferredSize(
            new Dimension(0, participantCount * checkboxHeight + 2 * relativeTopBorder));
    setMinimumSize(new Dimension(getMinimumSize().width,
            participantSelectionPanel.getMinimumSize().height + 250));
    setPreferredSize(new Dimension(getPreferredSize().width,
            participantSelectionPanel.getPreferredSize().height + 250));
    repaint();
  }

  /**
   * sets the position of the dialog, so that the center of the dialog is at
   * the supplied point.
   */
  private void setPosition(Point center) {
    setBounds(center.x - getWidth() / 2,
            center.y - getHeight() / 2,
            getWidth(),
            getHeight());
  }


  /**
   * creates a checkbox for selecting, wheter participant should be shown in full
   * detail.
   * @param order the number of previous checkboxes
   */
  private void createCheckboxFor(String participant, int order) {
    JCheckBox checkbox = new JCheckBox(participant, true);
    checkbox.setBounds(absoluteLeftBorder, asoluteTopBorder + order * checkboxHeight,
            checkboxWidth, checkboxHeight);
    checkboxes.put(participant, checkbox);
    selectedParticipants.add(participant);
    checkbox.addActionListener(createCheckboxActionListener(participant));
    participantSelectionPanel.add(checkbox);
  }

  private ActionListener createCheckboxActionListener(String participant) {
    return new ActionListener() {

      private String participant;

      public ActionListener setParticipant(String participant) {
        this.participant = participant;
        return this;
      }

      public void actionPerformed(ActionEvent e) {
        if (checkboxes.get(participant).isSelected()) {
          selectedParticipants.add(participant);
        } else {
          selectedParticipants.remove(participant);
        }
      }
    }.setParticipant(participant);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    participantSelectionPanel = new javax.swing.JPanel();
    generateButton = new javax.swing.JButton();
    abortButton = new javax.swing.JButton();
    messageFlowWithEnvelopeCheckbox = new javax.swing.JCheckBox();
    jLabel1 = new javax.swing.JLabel();
    layoutCheckbox = new javax.swing.JCheckBox();
    avoidImplicitCheckbox = new javax.swing.JCheckBox();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Generate collaboration diagram");
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    setFocusCycleRoot(false);
    setMinimumSize(new java.awt.Dimension(375, 0));

    participantSelectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Select the Participants, that should be generated in detail"));
    participantSelectionPanel.setFont(participantSelectionPanel.getFont());
    participantSelectionPanel.setName("participantSelectionPanel"); // NOI18N
    participantSelectionPanel.setOpaque(false);

    javax.swing.GroupLayout participantSelectionPanelLayout = new javax.swing.GroupLayout(participantSelectionPanel);
    participantSelectionPanel.setLayout(participantSelectionPanelLayout);
    participantSelectionPanelLayout.setHorizontalGroup(
      participantSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 343, Short.MAX_VALUE)
    );
    participantSelectionPanelLayout.setVerticalGroup(
      participantSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 100, Short.MAX_VALUE)
    );

    generateButton.setText("Generate now");
    generateButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    generateButton.setFocusCycleRoot(true);
    generateButton.setMaximumSize(new java.awt.Dimension(115, 25));
    generateButton.setMinimumSize(new java.awt.Dimension(115, 25));
    generateButton.setPreferredSize(new java.awt.Dimension(115, 25));
    generateButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        generateButtonActionPerformed(evt);
      }
    });

    abortButton.setText("Abort");
    abortButton.setMaximumSize(new java.awt.Dimension(115, 25));
    abortButton.setMinimumSize(new java.awt.Dimension(115, 25));
    abortButton.setPreferredSize(new java.awt.Dimension(115, 25));
    abortButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        abortButtonActionPerformed(evt);
      }
    });

    messageFlowWithEnvelopeCheckbox.setSelected(true);
    messageFlowWithEnvelopeCheckbox.setText("Generate message flow with message-icon on it");

    jLabel1.setText("(Note: will be saved as usual message flow)");

    layoutCheckbox.setSelected(true);
    layoutCheckbox.setText("Layout model");

    avoidImplicitCheckbox.setSelected(true);
    avoidImplicitCheckbox.setText("Avoid implicit Splits and Joins");
    avoidImplicitCheckbox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        avoidImplicitCheckboxActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(avoidImplicitCheckbox)
          .addComponent(layoutCheckbox)
          .addGroup(layout.createSequentialGroup()
            .addGap(21, 21, 21)
            .addComponent(jLabel1))
          .addComponent(messageFlowWithEnvelopeCheckbox)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(abortButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(generateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(participantSelectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(participantSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(messageFlowWithEnvelopeCheckbox)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(layoutCheckbox)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(avoidImplicitCheckbox)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(generateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(abortButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
    successfull = true;
    setVisible(false);
  }//GEN-LAST:event_generateButtonActionPerformed

  private void abortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortButtonActionPerformed
    successfull = false;
    setVisible(false);
  }//GEN-LAST:event_abortButtonActionPerformed

  private void avoidImplicitCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avoidImplicitCheckboxActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_avoidImplicitCheckboxActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton abortButton;
  private javax.swing.JCheckBox avoidImplicitCheckbox;
  private javax.swing.JButton generateButton;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JCheckBox layoutCheckbox;
  private javax.swing.JCheckBox messageFlowWithEnvelopeCheckbox;
  private javax.swing.JPanel participantSelectionPanel;
  // End of variables declaration//GEN-END:variables
}
