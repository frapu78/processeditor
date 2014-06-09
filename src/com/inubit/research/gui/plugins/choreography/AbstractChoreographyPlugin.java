/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography;

import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.WorkbenchEditorListener;
import com.inubit.research.gui.plugins.WorkbenchPlugin;
import java.awt.Component;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 * Abstract superclass for the EnforeabilityPlugin and the
 * BehavioralInterfaceGeneratingPlugin. Provides some common methods for these
 * plugins.
 * @author tmi
 */
public abstract class AbstractChoreographyPlugin extends WorkbenchPlugin {

  /**
   * all components, that have been added to the workbench. They are captured
   * in order to dis- and enable them according to the model type when the
   * selected editor changes.
   */
  private Collection<Component> components = new HashSet<Component>();

  /**
   * states, wheter a context menu should be added to BPMN-Editors or not
   */
  protected boolean addContextMenu;
  private WorkbenchEditorListener workbenchEditorListener =
          new WorkbenchEditorListener() {

            public void newEditorCreated(ProcessEditor editor) {
              processEditorChanged(editor, true);
            }

            public void selectedProcessEditorChanged(ProcessEditor editor) {
              processEditorChanged(editor, false);
            }
          };

  public AbstractChoreographyPlugin(Workbench workbench, boolean addContextMenu) {
    super(workbench);
    workbench.addWorkbenchEditorListener(workbenchEditorListener);
    this.addContextMenu = addContextMenu;
  }

  /**
   * used for retrieving the plugin´s menu entry
   * @return the submenu to be added to the plugin-menu in the workbench
   */
  @Override
  public JMenuItem getMenuEntry() {
    JMenu subMenu = new JMenu("Choreography Tools", false);
    for (Component component : getMenuEntries()) {
      subMenu.add(component);
    }
    registerComponent(subMenu);
    return subMenu;
  }

  protected abstract List<Component> getMenuEntries();

  /**
   * Checks, whether this editor display a BPMN-diagram and accordingly dis- or
   * enables all components, that were added to the workbench and, in case this
   * option is selected and the editor is new and displaying BPMN, adds a context-
   * menu entry to the editor
   */
  private void processEditorChanged(ProcessEditor editor, boolean isNew) {
    boolean isBPMN = editor.getModel() instanceof BPMNModel;
    for (Component component : components) {
      component.setEnabled(isBPMN);
    }
    if (addContextMenu && isBPMN && isNew) {
      editor.addCustomPopUpMenuItem(getMenuEntry());
    }
  }

  protected void registerComponent(Component component) {
    components.add(component);
  }
}
