/**
 *
 * Process Editor - inubit Workbench Natural Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.natural;

import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.Task;

/**
 *
 * Provides a model for natural language processing.
 *
 * @todo: Provide change listener
 *
 * @author fpu
 */
public class NaturalLanguageInputModel {

    protected BPMNModel model;
    protected List<String> participants = new LinkedList<String>();

    public NaturalLanguageInputModel(BPMNModel bpmnModel) {
        this.model = bpmnModel;
    }

    /**
     * Parses the attached ProcessModel for changes.
     */
    public void processModelChange() {
    }

    /**
     * Applies a natural input to the ProcessModel.
     * @param input
     */
    public String processNaturalInput(String input) {
        String response = "OK";
        try {
            Sentence sentence = NaturalLanguageParser.parseNaturalLanguageString(input);

            System.out.println("PROCESSING: "+sentence);

            if (sentence.getPredicate().getVerb().equals("add")) {
                response = processAdd(sentence);
            } else {
                response = "Action (verb) not recognized.";
            }

        } catch (NaturalLanguageParseException e) {
            response = e.getMessage().toUpperCase();
        }

        return response;
    }

    protected String processAdd(Sentence sentence) {
        String response = "Object (type) not recognized.";
        if (sentence.getPredicate().getObject_type().equals("participant")) {
            response = addPool(sentence);
        }
        if (sentence.getPredicate().getObject_type().equals("task")) {
            response = addTask(sentence);
        }
        return response;
    }

    protected String addTask(Sentence sentence) {
        String label = sentence.getPredicate().getObject_label();
        if (label==null) label = "New Task";
        // Create new task
        Task task = new Task();
        task.setText(label);
        // Try to detect Pool
        Pool pool = detectPool(sentence.getSubject().getSubject());
        if (pool==null && sentence.getSubject().getSubject()!=null) {
            return "Participant '"+sentence.getSubject().getSubject()+"' not found!";
        }

        if (pool!=null) pool.addProcessNode(task);

        model.addNode(task);

        return "OK";
    }

    protected String addPool(Sentence sentence) {
        if (sentence.getPredicate().getObject_label() == null) {
            return "No label for the participant given!";
        }
        // Check if Pool is already contained in model
        for (ProcessNode n : model.getNodes()) {
            if (n instanceof Pool) {

                if (n.getText().equalsIgnoreCase(   sentence.getPredicate().getObject_label())) {
                    return "Pool '" + sentence.getPredicate().getObject_label() + "' already contained.";
                }
            }
        }
        // Create new Pool
        Pool pool = new Pool();
        pool.setText(sentence.getPredicate().getObject_label());
        model.addNode(pool);
        return "OK";
    }

    protected Pool detectPool(String label) {
        for (ProcessNode n : model.getNodes()) {
            if (n instanceof Pool) {
                if (n.getText().equalsIgnoreCase(label)) return (Pool)n;
            }
        }
        return null;
    }
}
