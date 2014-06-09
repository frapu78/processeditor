package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.Task;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.SubProcess;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tmi
 */
public class NodeAdaptorTest {

    @Test
    public void testGetContainingProcess() {
        BPMNModel model = new BPMNModel("model");
        ModelAdaptor modelAdaptor = new ModelAdaptor(model);
        SubProcess outer = new SubProcess(),
                   inner = new SubProcess();
        NodeAdaptor outerAdaptor = NodeAdaptor.adapt(outer, modelAdaptor),
                    innerAdaptor = NodeAdaptor.adapt(inner, modelAdaptor);
        outer.setText("outer");
        inner.setText("inner");
        model.addNode(outer);
        model.addNode(inner);
        outer.addProcessNode(inner);
        assertEquals(outerAdaptor, innerAdaptor.getContainingProcess());
        Task task = new Task();
        task.setText("task");
        model.addNode(task);
        inner.addProcessNode(task);
        NodeAdaptor taskAdaptor = NodeAdaptor.adapt(task, modelAdaptor);
        assertEquals(innerAdaptor, taskAdaptor.getContainingProcess());
    }
}
