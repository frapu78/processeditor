/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.interfaceGenerator;

import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.plugins.choreography.AbstractChoreographyPlugin;
import com.inubit.research.gui.plugins.choreography.enforceabilityChecker.EnforceabilityPlugin;
import com.inubit.research.gui.plugins.choreography.enforceabilityChecker.EnforceabilityPlugin.ErrorLevel;
import com.inubit.research.layouter.ProcessLayouter;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.layouter.LayoutMenuitemActionListener;

/**
 * WorkbenchPlugin for generating colaboration diagrams from choreography diagrams.
 * This plugin includes the EnforceabilityPlugin, so do not add it seperatly.
 * @author tmi
 */
public class BehavioralInterfaceGeneratingPlugin extends AbstractChoreographyPlugin {

  private EnforceabilityPlugin enforceabilityPlugin;
  private static final String EXC_OnlyBPMN = "Only applicable for BPMN-Models",
          EXC_NotEnforceable = "Choreography must be enforceable for generating " +
          "a colaboration diagram.",
          EXC_UserAbort = "The user has aborted this action";

  private BPMNModel choreography;

  public BehavioralInterfaceGeneratingPlugin(Workbench workbench) {
    this(workbench, true);
  }

  /**
   * @param addContextMenu determines, wheter the plugin should add a custom
   * context menu to ProcessEditors showing BPMN models or not.
   */
  public BehavioralInterfaceGeneratingPlugin(Workbench workbench, boolean addContextMenu) {
    super(workbench, addContextMenu);
    enforceabilityPlugin = new EnforceabilityPlugin(workbench, false);
  }

  /**
   * performs the interface-generation, which includes checking enforceability,
   * prompting the user to select options and generating a colaboration diagram
   * from the choreography
   */
  public void generateInterface() throws AbortedException {
    GeneratingOptionsDialog dialog = prepareGenerating();
    BPMNModel behavioralInterface =
            (new BehavioralInterfaceGenerator(
                choreography, dialog.getSelectedParticipants(),
                dialog.messageFlowWithEnvelopeSelected(),
                dialog.wishesImplicitSplitAndJoinAvoidance())).
            getBehavioralInterface();
    ProcessEditor editor = workbench.openNewModel(behavioralInterface);
    ProcessLayouter layouter = behavioralInterface.getUtils().getLayouters().get(0);
    if(dialog.wishesLayouting()) {
      try {
          new LayoutMenuitemActionListener(editor, layouter).
                  actionPerformed(null);
      } catch (Exception ex) {
        showLayoutingException(ex);
      }
    }
  }

  /**
   * checks enforceability and prompts the user to select options
   * @return the dialog where the user selected the options for generating
   * @throws AbortedException if the choreography is not enforceable (i.e. there
   * were EnforceabilityProblems with type Error, or there were Warnings and
   * the user did not agree to generating the diagram nevertheless) or the user
   * selected cancel in the options dialog.
   */
  private GeneratingOptionsDialog prepareGenerating() throws AbortedException {
    if (!(workbench.getSelectedModel() instanceof BPMNModel)) {
      showOnlyBPMNWarning();
      throw new AbortedException(EXC_OnlyBPMN);
    }

    if (!checkEnforceability()) throw new AbortedException(EXC_NotEnforceable);

    choreography = (BPMNModel) workbench.getSelectedModel();
    GeneratingOptionsDialog dialog =
            new GeneratingOptionsDialog(workbench, true, choreography,
            new Point((int)workbench.getBounds().getCenterX(),
                (int)workbench.getBounds().getCenterY()));
    dialog.setVisible(true);

    if(dialog.aborted()) throw new AbortedException(EXC_UserAbort);
    return dialog;
  }

  /**
   * checks enforceability and, if there were warnings, but no errors, asks the
   * user wheter he wants to generate the colaboration nevertheless
   * @return true, if there were no problems, or there were warnings only and
   * the wants the diagram to be generated nevertheless
   */
  private boolean checkEnforceability() {
    ErrorLevel level = enforceabilityPlugin.performCheck(false);
    if(level.equals(ErrorLevel.Error)) {
      showNotEnforceableWarning();
      return false;
    } else if (level.equals(ErrorLevel.Warning)) {
      return querryUserForWarningAcceptance();
    }
    return true;
  }

  private void showOnlyBPMNWarning() {
    JOptionPane.showMessageDialog(workbench, "Only applicable for BPMN models");
  }

  private void showNotEnforceableWarning() {
    JOptionPane.showMessageDialog(workbench,
            "The choreography must be enforcealbe in order to be "
            + "automatically turned into behavioral interfaces");
  }

  private boolean querryUserForWarningAcceptance() {
    int selectedOption = JOptionPane.showConfirmDialog(workbench,
            "In order to enforce this choreography, (an) artificial timeout(s)" +
            "needs to be set up.\n Click OK to generate the colaboration " +
            "diagram, or Cancel to rework the choreography.",
            "Accept warnings?",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if(selectedOption == JOptionPane.OK_OPTION) {
      enforceabilityPlugin.removeMarkers();
    }
    return selectedOption == JOptionPane.OK_OPTION;
  }

  private void showLayoutingException(Exception exception) {
    JOptionPane.showMessageDialog(workbench,
            "Some problem happened while layouting the diagram: "
            + exception.toString());
    exception.printStackTrace();
  }

  @Override
  protected List<Component> getMenuEntries(){
    List<Component> entries = new LinkedList<Component>();
    JMenuItem generate = new JMenuItem("Generate Behavioral Interface");
    generate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
    generate.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        try{
          generateInterface();
        } catch(AbortedException ex) {
          //do nothing
        }
      }
    });
    registerComponent(generate);
    entries.add(generate);
    
    for (Component component : enforceabilityPlugin.getMenuEntries()) {
      entries.add(component);
    }
    return entries;
  }
}
