/**
 *
 * Process Editor - Petri net Simulation Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.simulation.petrinets;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.petrinets.Edge;
import net.frapu.code.visualization.petrinets.PetriNetModel;
import net.frapu.code.visualization.petrinets.Place;
import net.frapu.code.visualization.petrinets.Transition;

/**
 *
 * Provides static analysis for the hottest path of a ProcessModel.
 *
 * @author fpu
 */
public class HotPathAnalysis {

    private List<ProcessNode> visitedNodes = new LinkedList<ProcessNode>();

    public void highlightHottestPath(PetriNetModel model) {
        
        // Start Walk-Through for all inital places of the net
        for (Place p: getInitialPlaces(model)) {
            followPath(p, model);
        }
    }

    private boolean followPath(ProcessNode currentNode, PetriNetModel model) {
        if (visitedNodes.contains(currentNode)) return false;

        System.out.println("analyzing "+currentNode+" ("+currentNode.getText()+")");

        // Get successors
        Set<ProcessNode> succs = getPostSet(currentNode, model);
        // If multiple transitions follow, select the ones with the highest probability
        Set<ProcessNode> list = new HashSet<ProcessNode>();

        final int FLAG_ADD = 1;
        final int FLAG_NEW = 2;
        int mode = 0;
        for (ProcessNode n: succs) {

            System.out.println("   "+n+" ("+n.getText()+")");

            if (n instanceof Transition) {
                Transition t = (Transition)n;
                for (ProcessNode cn: list) {
                    if (cn instanceof Transition) {
                        Transition ct = (Transition)cn;
                        // Check if higher than probabilities in choice list
                        if (t.getProbability()>ct.getProbability()) mode = mode | FLAG_NEW;
                        // Check if equal
                        if (t.getProbability()==ct.getProbability()) mode = mode | FLAG_ADD;
                    }
                }
                if (list.size()==0) mode = FLAG_NEW;

                if ((mode & FLAG_NEW) == FLAG_NEW) {
                    list.clear();
                    list.add(t);
                }
                if ((mode & FLAG_ADD) == FLAG_ADD) {
                    list.add(t);
                }
            } else {
                list.add(n);
            }
        }

        visitedNodes.add(currentNode);
        currentNode.setHighlighted(true);
        // Process list
        for (ProcessNode n: list) {                  
            followPath(n, model);
        }

        return true;
    }

    public static Set<ProcessNode> getPostSet(ProcessNode currentNode, PetriNetModel model) {
        Set<ProcessNode> result = new HashSet<ProcessNode>();
        
        for (ProcessEdge e: model.getEdges()) {
            if (e.getSource() == currentNode) {
                // Consider only edges
                if (e instanceof Edge) {
                    result.add(e.getTarget());
                }
            }
        }

        return result;
    }

    public static Set<Place> getInitialPlaces(PetriNetModel model) {
        Set<Place> result = new HashSet<Place>();

        for (ProcessNode n: model.getNodes()) {
            if (n instanceof Place) {
                boolean skip = false;
                // Search if any edge has this place as target
                for (ProcessEdge e: model.getEdges()) {
                    if (e.getTarget() == n) {
                        if (e instanceof Edge)
                            skip = true;
                            break;
                    }                 
                }
                if (!skip) result.add((Place)n);
            }
        }

        return result;
    }

}
