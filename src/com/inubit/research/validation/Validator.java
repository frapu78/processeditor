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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessObject;
import org.xml.sax.SAXException;

/**
 *
 * @author tmi
 */
public class Validator {
    private ProcessModel model;
    private List<ValidationMessage> messages = null;
    private static Map<Class<? extends ProcessModel>, ModelValidator> supportedModels = new HashMap<Class<? extends ProcessModel>, ModelValidator>();

    @SuppressWarnings("CallToThreadDumpStack")
    public Validator(ProcessModel model, Map<Class<? extends ProcessModel>, ModelValidator> supportedModels) {
        this.model = model;
        this.supportedModels = supportedModels;
    }

    public List<ValidationMessage> getAllMessages() {
        if(messages == null) performCheck();
        return messages;
    }

    public List<ValidationMessage> getMessages(boolean errors, boolean warnings,
            boolean infos) {
        int typeBits = 0;
        if (errors) typeBits |= ValidationMessage.TYPE_ERROR;
        if (warnings) typeBits |= ValidationMessage.TYPE_WARNING;
        if (infos) typeBits |= ValidationMessage.TYPE_INFO;
        return getMessagesOfTypes(typeBits);
    }

    public List<ValidationMessage> getErrorMessages() {
        return getMessagesOfTypes(ValidationMessage.TYPE_ERROR);
    }
    
    public List<ValidationMessage> getWarningMessages() {
        return getMessagesOfTypes(ValidationMessage.TYPE_WARNING);
    }
    
    public List<ValidationMessage> getInformationMessages() {
        return getMessagesOfTypes(ValidationMessage.TYPE_INFO);
    }

    private List<ValidationMessage> getMessagesOfTypes(int typeBits) {
        if(messages == null) performCheck();
        List<ValidationMessage> selectedMessages =
                        new LinkedList<ValidationMessage>();
        for (ValidationMessage message : messages) {
            if ((message.getType() & typeBits) != 0) {
                selectedMessages.add(message);
            }
        }
        return selectedMessages;
    }

    public void performCheck() {
        messages = new LinkedList<ValidationMessage>();
        if (! isSupportedModel()) {
            
            MessageTexts messageTexts = null;
            try {
                messageTexts = getCommonMessages();
            } catch (IOException ex) {
                Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, null, ex);
            }
            messages.add(new ValidationMessage(ValidationMessage.TYPE_ERROR,
                    messageTexts.getLongText("unsupportedModel"),
                    messageTexts.getShortText("unsupportedModel"),
                    new LinkedList<ProcessObject>()));
            return;
        }
        ModelValidator mvi = supportedModels.get(model.getClass()); 
        messages = mvi.doValidation(model);
    }

    private boolean isSupportedModel() {
        return supportedModels.containsKey(model.getClass());
    }
    
    private MessageTexts getCommonMessages() throws IOException, ParserConfigurationException, SAXException {
        InputStream stream = this.getClass().getResourceAsStream("/validation-messages.xml");
        return new MessageTexts(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream));
    }
}
