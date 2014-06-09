/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessObject;
import org.xml.sax.SAXException;

/**
 *
 * @author cab
 */
public abstract class ModelValidator {
    private ProcessModel model;
    private MessageTexts messageTexts;
    private List<ValidationMessage> messages;
    private boolean hasFatalErrors;

    public ModelValidator() {
        this.hasFatalErrors = false;
    }
    
    public void prepareValidation(ProcessModel model){
        this.model = model;
        this.messageTexts = getMessageTexts();
        clearMessages();
    }
    
    public abstract List<ValidationMessage> doValidation(ProcessModel model);
    
    protected abstract MessageTexts getMessageTexts();
    
    public List<ValidationMessage> getMessages() {
        return messages;
    }

    public boolean hasFatalErrors() {
        return hasFatalErrors;
    }
    
    protected void clearMessages(){
        if(messages == null)
            this.messages = new ArrayList<ValidationMessage>();
        else
            this.messages.clear();
        this.hasFatalErrors = false;
    }
    
    protected void addMessage(String id, ProcessObject object, String... args) {
        addMessage(id, new LinkedList<ProcessObject>(), object, args);
    }
    
    protected void addMessage(ValidationMessage message) {
        messages.add(message);
    }

    protected void addMessage(String id, List<ProcessObject> objects, ProcessObject object, String... args) {
        String fullDescription = messageTexts.getLongText(id),
                shortDescription = messageTexts.getShortText(id);
        int level = messageTexts.getLevel(id);

        int count = 1;
        for (String s : args) {
            fullDescription = fullDescription.replaceAll("\\$" + count, s);
            shortDescription = shortDescription.replaceAll("\\$" + count, s);
            count++;
        }

        messages.add(new ValidationMessage(level, fullDescription,
                shortDescription, objects, object));
        if (messageTexts.isFatal(id)) {
            setHasFatalErrors();
        }
    }

    protected void setHasFatalErrors() {
        hasFatalErrors = true;
    }
    
    protected ProcessModel getModel(){
        return this.model;
    }
    
}
