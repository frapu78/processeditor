/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.validation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.ChoreographyTask;
import net.frapu.code.visualization.bpmn.Conversation;
import net.frapu.code.visualization.bpmn.ConversationLink;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class RootNodeTypeValidationTest extends BPMNValidationTestCommons {

    private List<ProcessObject> makeChoreography() {
        ChoreographyTask choreoTask = new ChoreographyTask();
        choreoTask.setText("Choreography Task");
        model.addNode(choreoTask);
        StartEvent start = createNoneStartEvent();
        EndEvent end = createEndEvent();
        addSequenceFlow(start, choreoTask);
        addSequenceFlow(choreoTask, end);
        makeSendTask(getTask1InA());
        makeReceiveTask(getTask1InB());
        MessageFlow messageFlow = addMessageFlow(getTask1InA(), getTask1InB());
        EdgeDocker docker = new EdgeDocker(messageFlow);
        model.addNode(docker);
        Association association = new Association(docker, choreoTask);
        association.setProperty(Association.PROP_DIRECTION,
                Association.DIRECTION_NONE);
        model.addEdge(association);

        List<ProcessObject> objects = new LinkedList<ProcessObject>();
        objects.add(choreoTask);
        objects.add(docker);
        objects.add(start);
        objects.add(end);
        return objects;
    }

    private List<ProcessObject> makeConversation() {
        makeSendTask(getTask1InA());
        makeReceiveTask(getTask1InB());
        Conversation conversation = new Conversation();
        model.addNode(conversation);
        ConversationLink link1 = new ConversationLink(conversation, getTask1InA()),
                         link2 = new ConversationLink(conversation, getTask1InB());
        model.addEdge(link1);
        model.addEdge(link2);

        List<ProcessObject> objects = new LinkedList<ProcessObject>();
        objects.add(conversation);
        return objects;
    }

    private List<ProcessObject> makeGlobalProcess() {
        getNonPoolTask1();
        List<ProcessObject> objects = new LinkedList<ProcessObject>();
        objects.add(getGlobalStart());
        objects.add(getNonPoolTask1());
        objects.add(getGlobalEnd());
        return objects;
    }

    @Test
    public void testGlobalPoolAndConversationInOneModel() {
        List<ProcessObject> related = makeGlobalProcess();
        related.addAll(makeConversation());
        assertOneError(texts.getLongText("mixedUpGlobalPoolAndConversation"),
                related, true);
    }

    @Test
    public void testConversationAndChoreographyInOneModel() {
        List<ProcessObject> related = makeConversation();
        related.addAll(makeChoreography());
        assertOneError(texts.getLongText("mixedUpChoreographyAndConversation"),
                related, true);
    }

    @Test
    public void testChoreographyAndGlobalPoolInOneModel() {
        Set<ProcessObject> related = new HashSet(makeGlobalProcess());
        related.addAll(makeChoreography());
        assertOneError(texts.getLongText("mixedUpGlobalPoolAndChoreography"),
                new LinkedList<ProcessObject>(related), true);
    }
}
