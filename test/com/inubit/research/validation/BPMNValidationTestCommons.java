package com.inubit.research.validation;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.TimerStartEvent;
import org.junit.Before;
import java.util.List;
import com.inubit.research.validation.bpmn.BPMNValidator;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilderFactory;
import net.frapu.code.visualization.LayoutUtils;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.Pool;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 * Provides common methods and fields for testing the parts of the BPMN-Validator.
 * @author tmi
 */
public abstract class BPMNValidationTestCommons {

    protected BPMNModel model;
    private Pool poolA;
    private Pool poolB;
    private Task task1InA;
    private Task task2InA;
    private Task task1InB;
    private Task task2InB;
    private Task nonPoolTask1;
    private Task nonPoolTask2;
    private StartEvent startInA, startInB, globalStart;
    private EndEvent endInA, endInB, globalEnd;
    protected MessageTexts texts;

    @Before
    @SuppressWarnings("CallToThreadDumpStack")
    public void setUp() {
        model = new BPMNModel();
        InputStream stream = this.getClass().getResourceAsStream("/validation-messages.xml");
        try {
            texts = new MessageTexts(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        }
    }

    protected void removeUnconnectedNodes() {
        for (ProcessNode node : new LinkedList<ProcessNode>(model.getNodes())) {
            if (!(node instanceof Pool || node instanceof Lane)) {
                if (model.getIncomingEdges(ProcessEdge.class, node).isEmpty() &&
                        model.getOutgoingEdges(ProcessEdge.class, node).isEmpty()) {
                    model.removeNode(node);
                }
            }
        }
    }

    protected void assertOneError(String description,
            ProcessObject relatedObject, boolean doLayout) {
        assertOneMessage(ValidationMessage.TYPE_ERROR, description,
                relatedObject, null, doLayout);
    }

    protected void assertOneWarning(String description,
            ProcessObject relatedObject, boolean doLayout) {
        assertOneMessage(ValidationMessage.TYPE_WARNING, description,
                relatedObject, null, doLayout);
    }

    protected void assertOneInfo(String description,
            ProcessObject relatedObject, boolean doLayout) {
        assertOneMessage(ValidationMessage.TYPE_INFO, description,
                relatedObject, null, doLayout);
    }

    protected void assertOneError(String description,
            List<ProcessObject> relatedObjects, boolean doLayout) {
        assertOneMessage(ValidationMessage.TYPE_ERROR, description,
                null, relatedObjects, doLayout);
    }

    protected void assertOneWarning(String description,
            List<ProcessObject> relatedObjects, boolean doLayout) {
        assertOneMessage(ValidationMessage.TYPE_WARNING, description,
                null, relatedObjects, doLayout);
    }

    protected void assertOneInfo(String description,
            List<ProcessObject> relatedObjects, boolean doLayout) {
        assertOneMessage(ValidationMessage.TYPE_INFO, description,
                null, relatedObjects, doLayout);
    }

    protected void assertOneError(String description, ProcessObject primaryObject,
            List<ProcessObject> relatedObjects, boolean doLayout) {
        assertOneMessage(ValidationMessage.TYPE_ERROR, description,
                primaryObject, relatedObjects, doLayout);
    }

    protected void assertOneWarning(String description, ProcessObject primaryObject,
            List<ProcessObject> relatedObjects, boolean doLayout) {
        assertOneMessage(ValidationMessage.TYPE_WARNING, description,
                primaryObject, relatedObjects, doLayout);
    }

    protected void assertOneInfo(String description, ProcessObject primaryObject,
            List<ProcessObject> relatedObjects, boolean doLayout) {
        assertOneMessage(ValidationMessage.TYPE_INFO, description,
                primaryObject, relatedObjects, doLayout);
    }


    private void assertOneMessage(int type, String description,
            ProcessObject primaryObject, List<ProcessObject> relatedObjects,
            boolean doLayout) {
        if (relatedObjects == null) {
            relatedObjects = new LinkedList<ProcessObject>();
        }
        if (doLayout) layoutModel();
        List<ValidationMessage> validationMessages =
                new BPMNValidator().doValidation(model);
        if (validationMessages.size() == 2 &&
                validationMessages.get(1).getShortDescription().
                equals("No soundness-check possible")) {
            validationMessages.remove(1);
        }

        for( Iterator<ValidationMessage> iter = validationMessages.iterator();iter.hasNext();)
            if ( iter.next().getType() != type )
                iter.remove();

        assertEquals(1, validationMessages.size());
        ValidationMessage message = validationMessages.get(0);
        assertEquals(type, message.getType());
        assertEquals(description, message.getDescription());
        assertEquals(primaryObject, message.getPrimaryObject());
        assertEquals(relatedObjects.size(), message.getInvolvedObjects().size());
        assertTrue(relatedObjects.containsAll(message.getInvolvedObjects()));
    }

    /*private void assertOneMessage(int type, String description,
            List<ProcessObject> relatedObjects, boolean doLayout) {
        if (doLayout) layoutModel();
        List<ValidationMessage> validationMessages =
                new BPMNValidator(model, texts).performCheck();
        assertEquals(1, validationMessages.size());
        ValidationMessage message = validationMessages.get(0);
        assertEquals(type, message.getType());
        assertEquals(description, message.getDescription());
        assertTrue("should have no primary object", !message.hasPrimaryObject());
        assertEquals(relatedObjects.size(), message.getInvolvedObjects().size());
        assertTrue(message.getInvolvedObjects().containsAll(relatedObjects));
    }*/

    protected void assertNoMessages(boolean doLayout) {
        if (doLayout) layoutModel();
        List<ValidationMessage> validationMessages =
                new BPMNValidator().doValidation(model);
        assertTrue(validationMessages.isEmpty());
    }

    @SuppressWarnings("CallToThreadDumpStack")
    protected void layoutModel() {
        try {
            model.getUtils().getLayouters().get(0).
                    layoutModel(LayoutUtils.getAdapter(model));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected void makeReceiveTask(Task task) {
        task.setProperty(Task.PROP_STEREOTYPE, Task.TYPE_RECEIVE);
    }

    protected void makeSendTask(Task task) {
        task.setProperty(Task.PROP_STEREOTYPE, Task.TYPE_SEND);
    }

    protected void makeServiceTask(Task task) {
        task.setProperty(Task.PROP_STEREOTYPE, Task.TYPE_SERVICE);
    }

    protected MessageFlow addMessageFlow(ProcessNode from, ProcessNode to) {
        MessageFlow flow = new MessageFlow(from, to);
        flow.setLabel("message");
        model.addEdge(flow);
        return flow;
    }

    protected List<ProcessObject> listOf(ProcessObject ob) {
        List<ProcessObject> list = new LinkedList<ProcessObject>();
        list.add(ob);
        return list;
    }

    protected List<ProcessObject> listOf(ProcessObject ob1, ProcessObject ob2) {
        List<ProcessObject> objects = listOf(ob1);
        objects.add(ob2);
        return objects;
    }

    protected List<ProcessObject> listOf(ProcessObject ob1, ProcessObject ob2,
            ProcessObject ob3) {
        List<ProcessObject> objects = listOf(ob1,ob2);
        objects.add(ob3);
        return objects;
    }

    protected List<ProcessObject> listOf(ProcessObject ob1, ProcessObject ob2,
            ProcessObject ob3, ProcessObject ob4) {
        List<ProcessObject> objects = listOf(ob1,ob2, ob3);
        objects.add(ob4);
        return objects;
    }

    protected EndEvent createEndEvent() {
        EndEvent end = new EndEvent();
        end.setText("end");
        model.addNode(end);
        return end;
    }

    protected SequenceFlow addSequenceFlow(ProcessNode source, ProcessNode target) {
        SequenceFlow flow = new SequenceFlow(source, target);
        model.addEdge(flow);
        flow.setLabel("label");
        return flow;
    }

    protected StartEvent createNoneStartEvent() {
        StartEvent start = new StartEvent();
        start.setText("start");
        model.addNode(start);
        return start;
    }

    protected TimerStartEvent createTimerStartEvent() {
        TimerStartEvent start = new TimerStartEvent();
        start.setText("start");
        model.addNode(start);
        return start;
    }

    protected Pool getPoolA() {
        if (poolA == null) {
            poolA = new Pool();
            poolA.setText("poolA");
            model.addNode(poolA);
            model.moveToBack(poolA);
        }
        return poolA;
    }

    protected Pool getPoolB() {
        if (poolB == null) {
            poolB = new Pool();
            poolB.setText("poolB");
            model.addNode(poolB);
            model.moveToBack(poolB);
        }
        return poolB;
    }

    protected Task getTask1InA() {
        return getTask1InA(true, true);
    }

    protected Task getTask1InA(boolean flowFromStart, boolean flowToEnd) {
        if (task1InA == null) {
            task1InA = new Task();
            task1InA.setText("task1InA");
            model.addNode(task1InA);
            getPoolA().addProcessNode(task1InA);
            if (flowFromStart) addSequenceFlow(getStartInA(), task1InA);
            if (flowToEnd) addSequenceFlow(task1InA, getEndInA());
        }
        return task1InA;
    }

    protected Task getTask2InA() {
        return getTask2InA(true, true);
    }

    protected Task getTask2InA(boolean flowFromStart, boolean flowToEnd) {
        if (task2InA == null) {
            task2InA = new Task();
            task2InA.setText("task2InA");
            model.addNode(task2InA);
            getPoolA().addProcessNode(task2InA);
            if (flowFromStart) addSequenceFlow(getStartInA(), task2InA);
            if (flowToEnd) addSequenceFlow(task2InA, getEndInA());
        }
        return task2InA;
    }

    protected Task getTask1InB() {
        return getTask1InB(true, true);
    }

    protected Task getTask1InB(boolean flowFromStart, boolean flowToEnd) {
        if (task1InB == null) {
            task1InB = new Task();
            task1InB.setText("task1InB");
            model.addNode(task1InB);
            getPoolB().addProcessNode(task1InB);
            if (flowFromStart) addSequenceFlow(getStartInB(), task1InB);
            if (flowToEnd) addSequenceFlow(task1InB, getEndInB());
        }
        return task1InB;
    }

    protected Task getTask2InB() {
        return getTask2InB(true, true);
    }

    protected Task getTask2InB(boolean flowFromStart, boolean flowToEnd) {
        if (task2InB == null) {
            task2InB = new Task();
            task2InB.setText("task2InB");
            model.addNode(task2InB);
            getPoolB().addProcessNode(task2InB);
            if (flowFromStart) addSequenceFlow(getStartInB(), task2InB);
            if (flowToEnd) addSequenceFlow(task2InB, getEndInB());
        }
        return task2InB;
    }

    protected Task getNonPoolTask1() {
        return getNonPoolTask1(true, true);
    }

    protected Task getNonPoolTask1(boolean flowFromStart, boolean flowToEnd) {
        if (nonPoolTask1 == null) {
            nonPoolTask1 = new Task();
            nonPoolTask1.setText("nonPoolTask1");
            model.addNode(nonPoolTask1);
            if (flowFromStart) addSequenceFlow(getGlobalStart(), nonPoolTask1);
            if (flowToEnd) addSequenceFlow(nonPoolTask1, getGlobalEnd());
        }
        return nonPoolTask1;
    }

    protected Task getNonPoolTask2() {
        return getNonPoolTask2(true, true);
    }

    protected Task getNonPoolTask2(boolean flowFromStart, boolean flowToEnd) {
        if (nonPoolTask2 == null) {
            nonPoolTask2 = new Task();
            nonPoolTask2.setText("nonPoolTask2");
            model.addNode(nonPoolTask2);
            if (flowFromStart) addSequenceFlow(getGlobalStart(), nonPoolTask2);
            if (flowToEnd) addSequenceFlow(nonPoolTask2, getGlobalEnd());
        }
        return nonPoolTask2;
    }

    protected StartEvent getStartInA() {
        if (startInA == null) {
            startInA = createTimerStartEvent();
            getPoolA().addProcessNode(startInA);
        }
        return startInA;
    }

    protected StartEvent getStartInB() {
        if (startInB == null) {
            startInB = createTimerStartEvent();
            getPoolB().addProcessNode(startInB);
        }
        return startInB;
    }

    protected StartEvent getGlobalStart() {
        if (globalStart == null) {
            globalStart = createTimerStartEvent();
        }
        return globalStart;
    }

    protected EndEvent getEndInA() {
        if (endInA == null) {
            endInA = createEndEvent();
            poolA.addProcessNode(endInA);
        }
        return endInA;
    }

    protected EndEvent getEndInB() {
        if (endInB == null) {
            endInB = createEndEvent();
            poolB.addProcessNode(endInB);
        }
        return endInB;
    }

    protected EndEvent getGlobalEnd() {
        if (globalEnd == null) {
            globalEnd = createEndEvent();
        }
        return globalEnd;
    }


    protected Gateway gatewayAfter(ProcessNode node) {
        Gateway gateway = new ExclusiveGateway();
        model.addNode(gateway);
        Cluster cluster = model.getClusterForNode(node);
        if (cluster != null) {
            cluster.addProcessNode(gateway);
        }
        for (ProcessEdge edge : model.getOutgoingEdges(SequenceFlow.class, node)) {
            edge.setSource(gateway);
        }
        addSequenceFlow(node, gateway);
        return gateway;
    }

    protected Gateway gatewayBefore(ProcessNode node) {
        Gateway gateway = new ExclusiveGateway();
        model.addNode(gateway);
        Cluster cluster = model.getClusterForNode(node);
        if (cluster != null) {
            cluster.addProcessNode(gateway);
        }
        for (ProcessEdge edge : model.getIncomingEdges(SequenceFlow.class, node)) {
            edge.setTarget(gateway);
        }
        addSequenceFlow(gateway, node);
        return gateway;
    }
}