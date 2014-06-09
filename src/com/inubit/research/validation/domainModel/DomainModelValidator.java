/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.domainModel;

import com.inubit.research.validation.MessageTexts;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.frapu.code.converter.XSDCreator;
import net.frapu.code.converter.XSDCreator.Hierarchy;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.domainModel.Attribute;
import net.frapu.code.visualization.domainModel.DomainClass;
import net.frapu.code.visualization.domainModel.DomainModel;

import com.inubit.research.client.UserCredentials;
import com.inubit.research.validation.ModelValidator;
import com.inubit.research.validation.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author fpu
 */

public class DomainModelValidator extends ModelValidator {

    private static DomainModelValidator instance = null;
    
    public DomainModelValidator() {
        super();
    }

    @Override
    public List<ValidationMessage> doValidation(ProcessModel model) {
        super.prepareValidation(model);
        // Check root_instance
        checkForRootInstance();
        // Check for changes
        checkForChanges();
        // Check for problematic Dependencies
        checkAggregationDependencies();

        return getMessages();
    }

    private void checkForRootInstance() {
        // A root instance has to be contained if no includes are contained
        // Otherwise it is an information message
        boolean refsIncluded = false;
        for (ProcessNode dc : getModel().getNodes()) {
            if (dc instanceof DomainClass) {
                if (dc.getText().contains(":")) {
                    refsIncluded = true;
                }
            }
        }
        // Check if root_instance exists
        for (ProcessNode dc : getModel().getNodes()) {
            if (dc instanceof DomainClass) {
                if (dc.getStereotype().equalsIgnoreCase(DomainClass.STEREOTPYE_ROOT_INSTANCE)) {
                    return;
                }
            }
        }
        // Otherwise add message
        if (!refsIncluded) {
            addMessage("rootInstanceRequired", null);
        } else {
            addMessage("rootInstanceRecommended", null);
        }
    }

    private void checkForChanges() {
        // Check if model has credentials
        UserCredentials credentials = (UserCredentials) getModel().getTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS);
        // Check if model has URI
        if (credentials == null | getModel().getProcessModelURI() == null) {
            return;
        }

        //
        // The following code is a bit of a hack...
        // @todo: Refactor code for fetching org model in domain validation
        //

        String uri = getModel().getProcessModelURI();
        // Check if starts with server address
        if (!uri.startsWith("http")) uri = credentials.getServer()+uri;
        // Check if URI is temporary
        if (uri.contains("/tmp")) {
            uri = uri.replaceAll("/tmp", "");
            uri = uri.replaceAll("_\\d*", "");
        }

        uri = uri.replaceAll("/versions/\\d+", "");

        // Check if Head model is available
        try {
            ProcessModel m = ProcessUtils.parseProcessModelSerialization(URI.create(uri), credentials);
            if ((m instanceof DomainModel)) {
                listIncompatibleChanges((DomainModel)getModel(), (DomainModel) m);
            } else {
                // Don't care right now - no message
            }
        } catch (Exception ex) {
            addMessage("serverOrModelNotFound", null);
        }

    }

    private void listIncompatibleChanges(DomainModel newModel, DomainModel orgModel) {
        listIncompatibleChangesForClasses(newModel, orgModel);
        listIncompatibleChangesForEdges(newModel, orgModel);
    }

    private void listIncompatibleChangesForClasses(DomainModel newModel, DomainModel orgModel) {
        // Check if all classes of the orgModel are contained in the newModel
        for (ProcessNode orgNode : orgModel.getNodes()) {
            if (orgNode instanceof DomainClass) {
                ProcessNode newNode = newModel.getNodeById(orgNode.getId());
                if (newNode != null) {
                    // Check if all attributes are contained in the newModel
                    DomainClass orgClass = (DomainClass) orgNode;
                    DomainClass newClass = (DomainClass) newNode;
                    // Check if the name is the same
                    if (!orgClass.getText().equals(newClass.getText())) {
                        addMessage("classRenamed", newClass, newClass.getText(), orgClass.getText());
                        return;
                    }

                    for (Attribute orgAtt : orgClass.getAttributesByIDs().values()) {
                        // Check if found
                        boolean found = false;
                        Attribute newAtt = null;
                        for (Attribute nAtt : newClass.getAttributesByIDs().values()) {
                            if (nAtt.getName().equals(orgAtt.getName())) {
                                found = true;
                                newAtt = nAtt;
                            }
                        }
                        if (!found) {
                            // Hmm, attribute has been deleted/renamed!
                            addMessage("attributeDeletedRenamed", newClass, orgAtt.getName());
                        } else {
                            // Check if type is the same
                            if (newAtt != null) {
                                if (!newAtt.getType().equals(orgAtt.getType())) {
                                    // Uups, type changed!
                                    addMessage("attributeTypeChanged", newClass, orgAtt.getName(), orgAtt.getType());
                                }
                            }
                        }
                    }

                } else {
                    // Uups, class not found
                    addMessage("classNotFound", null, orgNode.getText());
                }
            }
        }
    }

    private void listIncompatibleChangesForEdges(DomainModel newModel, DomainModel orgModel) {
        // Check if all edges of the original model are contained in the new model
        for (ProcessEdge orgEdge : orgModel.getEdges()) {
            // Check if found in newModel
            ProcessEdge newEdge = null;
            for (ProcessEdge nEdge : newModel.getEdges()) {
                if (nEdge.getId().equals(orgEdge.getId())) {
                    newEdge = nEdge;
                    break;
                }
            }
            if (newEdge == null) {
                ProcessNode newSource = newModel.getNodeById(orgEdge.getSource().getId());
                ProcessNode newTarget = newModel.getNodeById(orgEdge.getTarget().getId());
                // Care only if nodes still exist, otherwise nodes have been removed!
                if (newSource != null && newTarget != null) {
                    List<ProcessObject> nodes = new LinkedList<ProcessObject>();
                    nodes.add(newSource);
                    nodes.add(newTarget);
                    addMessage("edgeDeleted", nodes, newEdge, orgEdge.getSource().getText(), orgEdge.getTarget().getText());
                    return;
                }
            }
        }

    }

    private void checkAggregationDependencies() {
        Set<Hierarchy> aggregationTrees = new XSDCreator().determineAggregationHierarchies(getModel());

        for ( Hierarchy h : aggregationTrees ) {
            List<List<ProcessObject>> circles = h.detectCircles();
            Set<ProcessNode> circlingNodes = new HashSet<ProcessNode>();

            for ( List<ProcessObject> circle : circles ) {
                circlingNodes.add( (ProcessNode) circle.get(0) );
                addMessage("aggregationCircle", circle, circle.get(0), circle.get(0).getName());
            }

            Map<ProcessNode, List<ProcessObject>> multi = h.detectMultiAggregation();

            for( Map.Entry<ProcessNode, List<ProcessObject>> e : multi.entrySet() ) {
                if ( !circlingNodes.contains(e.getKey()) ) {
                    addMessage("multiAggregation", e.getValue(), e.getKey(), e.getKey().getName() );
                }
            }
        }
    }
    
    @Override
    protected ProcessModel getModel(){
        return (DomainModel) super.getModel();
    }

    public static DomainModelValidator getInstance() {
        if(instance == null)
            instance = new DomainModelValidator();
        return instance;
    }

    @Override
    protected MessageTexts getMessageTexts() {
        InputStream stream = this.getClass().getResourceAsStream("/validation-messages.xml");
        try {
            return new MessageTexts(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream));
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ModelValidator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ModelValidator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ModelValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
