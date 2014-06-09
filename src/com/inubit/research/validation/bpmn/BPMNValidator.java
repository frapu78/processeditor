/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;

import org.w3c.dom.Document;

import com.inubit.research.validation.MessageTexts;
import com.inubit.research.validation.ModelValidator;
import com.inubit.research.validation.ValidationMessage;
import com.inubit.research.validation.bpmn.adaptors.ActivityAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ClusterAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import com.inubit.research.validation.bpmn.adaptors.LaneableClusterAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ProcessObjectAdaptor;
import com.inubit.research.validation.bpmn.choreography.ChoreographyValidator;
import com.inubit.research.validation.bpmn.soundness.SoundnessChecker;

/**
 * A BPMNValidator checks the syntactic and semantic correctness of a BPMN BPD.
 * @author tmi
 */
public class BPMNValidator extends ModelValidator {
	private static Document messageTextsDoc;
	
	static {
		try {
			InputStream temp = BPMNValidator.class.getResourceAsStream("/validation-messages.xml");
	      	messageTextsDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(temp);
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
	}
	
    private ModelAdaptor model;
    private MessageTexts messageTexts;
    
    private static BPMNValidator instance;

    public BPMNValidator() {
        super();
    }

    /**
     * Check the model and return the list of information, warning and error
     * messages, that were created during the check.
     */
    @Override
    public List<ValidationMessage> doValidation(ProcessModel model) {
        super.prepareValidation(model);
        this.model = new ModelAdaptor((BPMNModel)model);
        this.messageTexts = getMessageTexts();
        try {
            validateClusterContainmentAndAttachement();
        } catch (RecursiveClusterContainmentException ex) {
            return getMessages(); //if there are self-containing clusters, the validation
            //could result in an infinite loop, so stop here
        }
        validateRootNodeType();
        checkForForbiddenNodes();
        checkForInvalidEdges();
        checkForUnlabeledNodes();
        doSpecificNodeChecks();
        validateInstantiationAndCompletion();
        validateLinkCorrelation();
        validateChoreography();
        if (!hasFatalErrors()) {
            checkSoundness();
        } else {
            addMessage("fatalErrorsNoSoundnessCheck", (NodeAdaptor)null);
        }
        return getMessages();
    }

    private void validateRootNodeType() {
        if (model.hasChoreography() && model.hasConversation()) {
            addMessage("mixedUpChoreographyAndConversation",
                    model.getRootNodesExceptPools());
        } else if (model.hasChoreography() && model.hasGlobalPool()) {
            addMessage("mixedUpGlobalPoolAndChoreography",
                    model.getRootNodesExceptPools());
        } else if (model.hasConversation() && model.hasGlobalPool()) {
            addMessage("mixedUpGlobalPoolAndConversation",
                    model.getRootNodesExceptPools());
        }
    }

    private void checkForForbiddenNodes() {
        for (NodeAdaptor node : model.getNodes()) {
            if (!node.isAllowedInBPD()) {
                addMessage("forbiddenNode", node);
            }
        }
    }

    private void checkForInvalidEdges() {
        for (EdgeAdaptor edge : model.getEdges()) {
            try {
                EdgeValidator.validatorFor(edge, model, this).doValidation();
            } catch(UnsupportedEdgeException ex) {
                addMessage("forbiddenEdge", edge);
            }
        }
    }

    private void checkForUnlabeledNodes() {
        new UnlabeledNodesChecker(model, this).doValidation();
    }

    private void doSpecificNodeChecks() {
        for (NodeAdaptor node : model.getNodes()) {
            if (node.isGateway()) {
                GatewayAdaptor gateway = (GatewayAdaptor) node;
                if (((GatewayAdaptor)node).isEventBasedGateway()) {
                    new EventBasedGatewayValidator(gateway, model, this).
                            doValidation();
                }
                new GatewayValidator(gateway, model, this).doValidation();
            } else {
                new NonGatewayNodeValidator(node, this).doValidation();
            }
            if (node.isPool()) {
                new PoolValidator((LaneableClusterAdaptor)node, model, this).
                        doValidation();
            }
            if (node.isTask()) {
                new TaskValidator((ActivityAdaptor)node, model, this).
                        doValidation();
            }
            if (node.isSubProcess()) {
                new SubProcessValidator((ClusterAdaptor)node, model, this).
                        doValidation();
            }
            if (node.isEvent()) {
                new EventValidator((EventAdaptor)node, model, this).
                        doValidation();
            }
            if (node.isConversation()) {
                new ConversationValidator(node, model, this).doValidation();
            }
            if (node.isChoreographyActivity()) {
                validateChoreographyActivity(node);
            }
        }
    }

    public void addMessage(String id,
            Collection<? extends ProcessObjectAdaptor> relatedObjects) {
        addMessage(id, null, relatedObjects);
    }

    public void addMessage(String id, ProcessObjectAdaptor primaryObject,
            Collection<? extends ProcessObjectAdaptor> relatedObjects) {
        List<ProcessObject> related = new LinkedList<ProcessObject>();
        for (ProcessObjectAdaptor object : relatedObjects) {
            related.add(object.getAdaptee());
        }
        String fullDescription  = messageTexts.getLongText(id),
               shortDescription = messageTexts.getShortText(id);
        int level = messageTexts.getLevel(id);
        addMessage(new ValidationMessage(level, fullDescription,
                shortDescription, related,
                primaryObject == null? null : primaryObject.getAdaptee()));
        if (messageTexts.isFatal(id)) {
            setHasFatalErrors();
        }
    }

    public void addMessage(String id, ProcessObjectAdaptor relatedObject) {
        addMessage(id, relatedObject, new LinkedList<ProcessObjectAdaptor>());
    }

    private void validateInstantiationAndCompletion() {
        new InstantiationAndCompletionValidator(model, this).doValidation();
    }

    private void validateClusterContainmentAndAttachement()
            throws RecursiveClusterContainmentException {
        new ClusterContainmentAndAttachementValidator(model, this).doValidation();
    }

    private void validateLinkCorrelation() {
        for (LaneableClusterAdaptor pool : model.getPools()) {
            new LinkCorrelationValidator(pool, model, this).doValidation();
        }
    }

    private void validateChoreographyActivity(NodeAdaptor choreographyActivity) {
        if (!model.getPoolForNode(choreographyActivity).isNull()) {
            addMessage("choreographyActivityInPool", choreographyActivity);
        }
    }

    private void validateChoreography() {
        if (model.hasChoreography()) {
            new ChoreographyValidator(model, this).checkModel();
        }
    }

    private void checkSoundness() {
        for (ModelAdaptor subModel : model.getSubmodelsForNonAdHocProcesses()) {
            new SoundnessChecker(subModel, this).perform();
            subModel.removeAll();
        }
    }
    
    public static BPMNValidator getInstance(){
        if(instance == null)
            instance = new BPMNValidator();
        return instance;
    }

    @Override
    protected MessageTexts getMessageTexts() {
        return new MessageTexts(messageTextsDoc);
    }
}
