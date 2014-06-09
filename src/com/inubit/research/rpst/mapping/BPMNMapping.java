/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.mapping;

import com.inubit.research.rpst.graph.Edge;
import com.inubit.research.rpst.graph.Node;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Activity;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.ChoreographyActivity;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.SubProcess;

/**
 *
 * @author fel
 */
public class BPMNMapping extends Mapping {
    public BPMNMapping( BPMNModel model ) {
        super( model );
    }

    public BPMNMapping( List<ProcessNode> nodes, List<ProcessEdge> edges, BPMNModel model ) {
        super(nodes, edges, model);
    }

    @Override
    protected void createMapping() {
        Map<ProcessNode, Node> nodeMap = this.createNodeMapping();

        this.mapEdges( nodeMap );
    }
    private Map<ProcessNode, Node> createNodeMapping() {
        Map<ProcessNode, Node> nodeMap = new HashMap<ProcessNode, Node>();

        for ( ProcessNode n : this.nodes ) {
            Node graphNode = null;

            if ( nodeMap.containsKey( n ))
                continue;

            if (    n instanceof Activity ||
                    n instanceof StartEvent ||
                    n instanceof EndEvent ||
                    n instanceof Gateway ||
                    n instanceof ChoreographyActivity ||
                    n instanceof SubProcess ||
                    n instanceof ChoreographySubProcess ) {

                graphNode = mapProcessNode(n);
            } else if ( n instanceof IntermediateEvent ) {
                ProcessNode parentNode = ((IntermediateEvent) n).getParentNode( this.model );

                //map attached event to parent if a parent exists
                if ( parentNode != null ) {
//                    System.out.println("found attached");
                    if ( !nodeMap.containsKey( parentNode )) {
                        graphNode = this.mapProcessNode( parentNode );
                        if ( graphNode != null ) {
                            nodeMap.put( parentNode, graphNode );
                        }
                    } else
                        graphNode = nodeMap.get( parentNode );
                } else
                    graphNode = mapProcessNode(n);
            }

            //ignore all other elements

            if ( graphNode != null ) {
                nodeMap.put(n, graphNode);

                if ( n instanceof StartEvent )
                    this.graph.addSource( graphNode );

                if ( n instanceof EndEvent )
                    this.graph.addSink( graphNode );
            }
        }

        return nodeMap;
    }

    private Node mapProcessNode( ProcessNode n ) {
        if ( !isContainedInSubProcess(n) ) 
            return this.graph.createNode( n.getName() );

        return null;
    }

    private boolean isContainedInSubProcess( ProcessNode n ) {
        Set<Cluster> clusters = n.getParentClusters();
        for ( Cluster c : clusters )
            if ( c instanceof SubProcess || c instanceof ChoreographySubProcess )
                return true;

        return false;
    }

    private void mapEdges( Map<ProcessNode, Node> nodeMap ) {
        for ( ProcessEdge e : this.edges ) {
            if ( e instanceof SequenceFlow ) {
                Node source = nodeMap.get( e.getSource() );
                Node target = nodeMap.get( e.getTarget() );

                if ( source != null && target != null ) {
                    Edge graphEdge  = this.graph.createEdge(source, target);
                    this.edgeMap.put(graphEdge, e);
                }
            }
        }
    }
}
