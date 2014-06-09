/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.frapu.code.visualization.xforms;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;

import com.inubit.research.animation.LayoutingAnimator;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.xForms.XFormsLayouter;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.Dragable;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessEditorListener;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessModelListener;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.uml.UMLClass;

/**
 *
 * @author fpu
 */
public class XFormsWizardTransferHandler extends TransferHandler implements ProcessEditorListener, ProcessModelListener {

    /**
     *
     */
    private static final long serialVersionUID = 4163330633395639576L;
    private ProcessEditor editor;
    private ProcessModel boModel;
    private XFormsLayouter layouter = new XFormsLayouter(Configuration.getProperties());
    private LayoutingAnimator layoutAnimator = new LayoutingAnimator(layouter);
    private boolean autolayout = true;

    public XFormsWizardTransferHandler(ProcessEditor editor) {
        this.editor = editor;
        layoutAnimator.setCustomAnimationTime(500);
        setUpListeners(editor);
    }

    private void setUpListeners(ProcessEditor editor) {
        editor.addListener(this);
        editor.getModel().addListener(this);
    }

    public void setBO(ProcessModel bo) {
        boModel = bo;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    @Override
    public Transferable createTransferable(JComponent c) {
        // Get object at clickPosition
        if (c instanceof JTree) {
            JTree tree = (JTree) c;
            Object o = tree.getSelectionPath().getLastPathComponent();
            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode tn = (DefaultMutableTreeNode) o;
                if (tn.getUserObject() instanceof ProcessNode) {
                    return new StringSelection("#" + (((ProcessNode) (tn.getUserObject())).getId()));
                }
                if (tn.getUserObject() instanceof Tuple) {
                    if (((Tuple) (tn.getUserObject())).getKey().equals(XFormsWizard.ACTION_ENTRY)) {
                        return new StringSelection("$" + (((Tuple) (tn.getUserObject())).getValue()));
                    }
                }
                return new StringSelection(o.toString());
            }
        }
        return null;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        // we will only support drops (not clipboard paste)
        if (!support.isDrop()) {
            return false;
        }

        // Accept everything for now...
        return true;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        // if we cannot handle the import, say so
        if (!canImport(support)) {
            return false;
        }

        // fetch the drop location
        Point dropPoint = support.getDropLocation().getDropPoint();
        String content = null;
        try {
            content = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
        } catch (Exception ex) {
            return false;
        }

        if (content == null) {
            return false;
        }

        if (content.startsWith("#")) {
            content = content.substring(1);
            // Try to find ProcessNode in BO
            ProcessNode n = boModel.getNodeById(content);

            System.out.println("Node=" + n + ", content=" + content);

            if (n != null) {
                if (n instanceof UMLClass) {
                    // Do something here...
                    System.out.println("UMLCLass");
                }
            }
        }
        if (content.startsWith("$")) {
            // Add button
            Trigger t = new Trigger();
            t.setText(content.substring(1));
            t.setPos(dropPoint);
            editor.getModel().addNode(t);
            checkClustering(t);
        } else {
            // Insert as input
            String label = content;
            String type = "";
            if (content.indexOf(":") >= 0) {
                label = content.substring(0, content.indexOf(":"));
                type = content.substring(content.indexOf(":") + 1).trim();
            }
            BaseElement i = null;
            if (type.endsWith("boolean")) {
                i = new CheckBox();
                i.setProperty(Input.PROP_LABEL, label);
            } else {
                i = new Input();
                i.setProperty(Input.PROP_LABEL, label + ":");
            }
            i.setPos(dropPoint);
            editor.getModel().addNode(i);
            checkClustering(i);
        }
        layoutModel();
        return false;
    }

    public void setAutoLayout(boolean selected) {
        autolayout = selected;
    }

    /**
     * @param t
     */
    private void checkClustering(ProcessNode nodeAdded) {
        System.out.println(">>" + editor.getModel().getClusters().size());
        for (int i = editor.getModel().getClusters().size() - 1; i >= 0; i--) {
            Cluster c = editor.getModel().getClusters().get(i);
            System.out.println(c);
            if (c.getBoundingBox().intersects(nodeAdded.getBoundingBox())) {
                c.addProcessNode(nodeAdded);
                break;
            }
        }
    }

    /**
     *
     */
    private void layoutModel() {
        if (autolayout) {
            try {
                layoutAnimator.layoutModelWithAnimation(editor, null, 0, 0, ProcessLayouter.LAYOUT_HORIZONTAL);
            } catch (Exception e) {
                System.out.println("Error during auto-layout!");
                e.printStackTrace();
            }
        }
    }

    //-------------- PROCESSEDITORLISTENER METHODS
    @Override
    public void modelChanged(ProcessModel m) {
        setUpListeners(editor);
    }

    @Override
    public void processObjectClicked(ProcessObject o) {
    }

    @Override
    public void processObjectDoubleClicked(ProcessObject o) {
    }

    @Override
    public void processNodeEditingFinished(ProcessNode o) {
    }

    @Override
    public void processNodeEditingStarted(ProcessNode o, JTextField textfield) {
    }

    @Override
    public void processObjectDragged(Dragable o, int oldX, int oldY) {
        if (o instanceof ProcessNode) {
            layoutModel();
        }
    }

    //-------------- PROCESSMODELLISTENER METHODS
    @Override
    public void processEdgeAdded(ProcessEdge edge) {
    }

    @Override
    public void processEdgeRemoved(ProcessEdge edge) {
    }

    @Override
    public void processNodeAdded(ProcessNode newNode) {
        //do not call autoLayout here, otherwise the model will get
        //layouted before the clustering check
    }

    @Override
    public void processNodeRemoved(ProcessNode remNode) {
        layoutModel();
    }

    @Override
    public void processObjectPropertyChange(ProcessObject obj, String name, String oldValue, String newValue) {
        // ignore
    }
}
