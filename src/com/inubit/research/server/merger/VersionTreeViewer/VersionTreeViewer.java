/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.server.merger.VersionTreeViewer;

import com.inubit.research.client.InvalidUserCredentialsException;
import com.inubit.research.client.ModelDescription;
import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.client.XMLHttpRequestException;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.adapter.BPMNModelAdapter;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 *
 * @author Uwe
 */
public class VersionTreeViewer extends ProcessEditor {

    public static final int BUTTON_HEIGHT = 20;
    public static final boolean variableNodeSize = true;
    private ModelDescription versionRepository;

    public VersionTreeViewer(ModelDescription modelDescription) {
        super();
        try {
            this.versionRepository = modelDescription;
            this.setModel(new VersionModel());
            this.setEditable(false);
            this.setEnabled(true);
            this.setAnimationEnabled(true);
            createVersionTree();
            defaultLayout();
        } catch (Exception ex) {
            Logger.getLogger(VersionTreeViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void defaultLayout() {
        try {
            ProcessLayouter layouter = new GridLayouter(true, 10, true, 10, 10);
            layouter.layoutModel(new BPMNModelAdapter((VersionModel) this.getModel()), 0, 0, ProcessLayouter.LAYOUT_HORIZONTAL);
        } catch (Exception ex) {
            Logger.getLogger(VersionTreeViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void addVersionNode(VersionNode n) {
        this.getModel().addNode(n);
        addEges(n);
        if (variableNodeSize) {
            setRelativeSize(n);
        }
        defaultLayout();
    }

    private void addEges(VersionNode versionNode) {
        for (VersionNode pred : this.getPredecessors(versionNode)) {
            SequenceFlow edge = new SequenceFlow(pred, versionNode);
            this.getModel().addEdge(edge);
        }
    }

    private void createVersionTree() throws ParserConfigurationException, XMLHttpRequestException, MalformedURLException, InvalidUserCredentialsException {
        try {
            for (ModelVersionDescription version : versionRepository.getModelVersionDescriptions()) {
                VersionNode n = new VersionNode(this, version, versionRepository);
                this.getModel().addNode(n);
            }
            for (ProcessNode n : new ArrayList<ProcessNode>(
                    this.getModel().getNodes())) {
                addEges((VersionNode) n);
                if (variableNodeSize) {
                    setRelativeSize((VersionNode) n);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(VersionTreeViewer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(VersionTreeViewer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(VersionTreeViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Set<VersionNode> getPredecessors(VersionNode n) {
        Set<VersionNode> result = new HashSet<VersionNode>();
        for (String predVersion : n.getPredecessors()) {
            if (!predVersion.equals("")) {
                VersionNode pred = getVersionNode(predVersion);
                result.add(pred);
            }

        }
        return result;
    }

    private VersionNode getVersionNode(String version) {
        for (ProcessNode n : this.getModel().getNodes()) {
            VersionNode versionNode = (VersionNode) n;
            if (versionNode.getVersion().equals(version)) {
                return versionNode;
            }
        }
        return null;
    }

    private void setRelativeSize(VersionNode n) {
        // get all absolute distances
        int distanceSum = 0;
        int number = 0;
        for (ProcessNode pn : this.getModel().getNodes()) {
            if (pn instanceof VersionNode) {
                distanceSum += ((VersionNode) pn).getAbsoluteDistance();
                number++;
            }
        }
        double distanceFactor = (double) n.getAbsoluteDistance() / distanceSum;
        int totalWidth = number * VersionNode.DEFAULT_WIDTH;
        int distributableWidth = totalWidth - number * VersionNode.MIN_WIDTH;
        int w = (int) (VersionNode.MIN_WIDTH + distanceFactor * distributableWidth);
        int h = (int) (Math.min(Math.max(VersionNode.MIN_HEIGHT, distanceFactor * number * VersionNode.DEFAULT_HEIGHT), VersionNode.MAX_HEIGHT));
        n.setSize(w, h);

    }
}
