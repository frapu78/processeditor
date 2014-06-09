/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.testUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author uha
 */
public class ModelGenerator {

    public int width = 1000;
    public int height = 1000;



    public ModelGenerator() {

    }






    public ProcessModel generate(Seed originalSeed, Class ModelType, int avgNodeNumber, int avgEdgeNumber) {
        Seed seed = new Seed(originalSeed);
        ProcessModel model = null;
        try {
            int dice = 0;
            model = (ProcessModel) ModelType.newInstance();
            do {
                dice = seed.decide(avgNodeNumber);
                model.addNode(getNode(ModelType, seed));
            } while (dice != 0);
            ProcessNode[] nodes;
            ProcessEdge edge;
            if (model.getNodes().size() >= 2) {
                do {
                    dice = seed.decide(avgEdgeNumber);
                    nodes = getTwoNodes(model, seed);
                    edge = getEdge(ModelType, seed);
                    edge.setSource(nodes[0]);
                    edge.setTarget(nodes[1]);
                    model.addEdge(edge);
                } while (dice != 0);
            }
        } catch (InstantiationException ex) {
            Logger.getLogger(ModelGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ModelGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return model;
    }

    private ProcessNode getNode(Class ModelType, Seed seed) throws InstantiationException, IllegalAccessException {
        ProcessModel m = null;
        m = (ProcessModel) ModelType.newInstance();
        List createableNodes = m.getCreateableNodeClasses();
        int size = createableNodes.size();
        int dice = seed.decide(size - 1);
        Class nodeClass = (Class) createableNodes.get(dice);
        ProcessNode node = (ProcessNode) nodeClass.newInstance();
        int xPos = seed.decide(width);
        int yPos = seed.decide(height);
        node.setPos(xPos,yPos);
        return node;
    }

    private ProcessEdge getEdge(Class ModelType, Seed seed) throws InstantiationException, IllegalAccessException {
        ProcessModel m = null;
        m = (ProcessModel) ModelType.newInstance();
        List createableEdges = m.getSupportedEdgeClasses();
        int size = createableEdges.size();
        int dice = seed.decide(size - 1);
        Class edgeClass = (Class) createableEdges.get(dice);
        ProcessEdge node = (ProcessEdge) edgeClass.newInstance();
        return node;
    }

    private ProcessNode[] getTwoNodes(ProcessModel model, Seed seed) {
        ProcessNode n1;
        ProcessNode n2;
        List<ProcessNode> nodes = model.getNodes();
        int i = seed.decide(nodes.size() - 1);
        n1 = nodes.get(i);
        i = seed.decide(nodes.size() - 1);
        n2 = nodes.get(i);
        ProcessNode[] result = {n1, n2};
        return result;
    }
}
