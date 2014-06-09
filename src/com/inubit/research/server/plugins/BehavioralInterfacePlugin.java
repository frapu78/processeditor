/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import com.inubit.research.gui.plugins.choreography.interfaceGenerator.BehavioralInterfaceGenerator;
import com.inubit.research.server.extjs.ExtCheckBox;
import com.inubit.research.server.extjs.ExtCheckBoxGroup;
import com.inubit.research.server.extjs.ExtFieldSet;
import com.inubit.research.server.extjs.ExtJSForm;
import com.inubit.research.server.extjs.ExtJSFormFactory;
import com.inubit.research.server.extjs.ExtJSProperty;
import com.inubit.research.server.manager.ModelManager;
import com.inubit.research.server.multipart.MultiPartObject;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.handler.util.ProcessModelUtils;
import com.inubit.research.server.user.LoginableUser;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.bpmn.BPMNModel;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author fel
 */
public class BehavioralInterfacePlugin extends FormServerPlugin implements ModelScope{

    private static final String ENVELOPE_BOX_NAME = "envelope";
    private static final String IMPLICIT_BOX_NAME = "avoidimplicit";

    public BehavioralInterfacePlugin() {
        super();
        this.scope = PluginScope.MODEL;
    }
    
    @Override
    void performMultipartFormAction(RequestFacade req, ResponseFacade resp, MultiPartObject mpo, ModelInformation modelInfo, LoginableUser u) throws IOException, JSONException {
        Set<String> keys = mpo.keySet();
        Map<String, String> params = new HashMap<String, String>();
        for ( String key : keys ) 
            params.put(key, "on");
        
        this.performURLEncodedFormAction(req, resp, params, modelInfo, u);
    }
    
    @Override
    void performURLEncodedFormAction(RequestFacade req, ResponseFacade resp, Map<String, String> params, ModelInformation modelInfo, LoginableUser u) throws IOException, JSONException {
        boolean envelopeOnFlow = true;
        boolean avoidImplicitSplitJoin = true;
        
        if (modelInfo.getProcessModel() instanceof BPMNModel )  {
            Set<String> participants =
                com.inubit.research.gui.plugins.choreography.Utils.participantsOf(modelInfo.getProcessModel().getNodes());

            Set<String> selectedParticipants = new HashSet<String>();

            if (params.containsKey(ENVELOPE_BOX_NAME))
                envelopeOnFlow = false;

            if (params.containsKey(IMPLICIT_BOX_NAME))
                avoidImplicitSplitJoin = false;

            for (String particpant : participants ) {
                if (params.containsKey( particpant.toLowerCase() ))
                    selectedParticipants.add(particpant);
            }
            BehavioralInterfaceGenerator big = new BehavioralInterfaceGenerator(
                                                    (BPMNModel) modelInfo.getProcessModel(),
                                                    selectedParticipants,
                                                    envelopeOnFlow,
                                                   avoidImplicitSplitJoin);

            ProcessModel pm = null;
            try {
                pm = big.getBehavioralInterface();
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }

            ProcessModelUtils utils = new ProcessModelUtils(pm);
            utils.layout();

            String uri  = ModelManager.getInstance().addTemporaryModel(pm);

            JSONObject jo = new JSONObject();
            jo.put("success", true);
            jo.put("action", PluginResponseType.OPEN.toString());
            jo.put("uri", uri);
            //Send HTML/Text as content type to prevent ,e.g., Firefox from downloading the response
            ResponseUtils.respondWithJSONAsText( resp, jo, 200 );
        } else {
            JSONObject jo = new JSONObject();
            jo.put("success", false);
            jo.put("action", PluginResponseType.ERROR.toString());
            jo.put("errormsg", "This is no BPMN model. Interface generation is only available for BPMN models!");
            //Send HTML/Text as content type to prevent ,e.g., Firefox from downloading the response
            ResponseUtils.respondWithJSONAsText(resp, jo, 400);
        }

    }

    @Override
    protected JSONObject getFormConfig(ModelInformation mi, RequestFacade req, LoginableUser u) throws FormGenerationException {
        if (!(mi.getProcessModel() instanceof BPMNModel))
            throw new FormGenerationException("This is no BPMN model. Interface generation is only available for BPMN models!");

        ExtJSForm form = ExtJSFormFactory.createEmptyForm();
        form.setProperty(ExtJSProperty.TITLE, "Generate Collaboration Diagram");
        form.setProperty(ExtJSProperty.HIDE_LABELS, Boolean.TRUE);
        form.setProperty(ExtJSProperty.FILE_UPLOAD, Boolean.FALSE);

        ExtCheckBoxGroup group = ExtJSFormFactory.createCheckBoxGroup();
        group.setProperty(ExtJSProperty.COLUMNS, "1");
        group.setProperty(ExtJSProperty.MIN_HEIGHT, "150");
        group.setProperty(ExtJSProperty.STYLE, "{ paddingTop: '10px' }");

        Set<String> participants = 
            com.inubit.research.gui.plugins.choreography.Utils.participantsOf(mi.getProcessModel().getNodes());

        if (participants.size() == 0) 
            throw new FormGenerationException("This model has no participants. Maybe this is no choreography model.");

        ExtFieldSet fSet = ExtJSFormFactory.createEmptyFieldSet();
        fSet.setProperty(ExtJSProperty.LAYOUT, "fit");
        fSet.setProperty(ExtJSProperty.FRAME, Boolean.TRUE);
        fSet.setProperty(ExtJSProperty.TITLE, "Select the participants that should be generated in detail");
        fSet.setProperty(ExtJSProperty.COLLAPSIBLE, Boolean.TRUE);
        fSet.addItem(group);

        for ( String participant : participants ) {
            ExtCheckBox cb = ExtJSFormFactory.createCheckBox();
            group.addItem( cb );
            cb.setProperty(ExtJSProperty.NAME, participant.toLowerCase());
            cb.setProperty(ExtJSProperty.CHECKED, Boolean.TRUE);
            cb.setProperty(ExtJSProperty.STYLE, "{ marginLeft: '10px' }");
            cb.setProperty(ExtJSProperty.BOX_LABEL, participant);
        }

        ExtCheckBox envelopeBox = ExtJSFormFactory.createCheckBox();

        envelopeBox.setProperty(ExtJSProperty.NAME, ENVELOPE_BOX_NAME);
        envelopeBox.setProperty(ExtJSProperty.CHECKED, Boolean.TRUE);
        envelopeBox.setProperty(ExtJSProperty.STYLE, "{ marginLeft: '15px' }");
        envelopeBox.setProperty(ExtJSProperty.BOX_LABEL, "Generate message flow with message-icon on it");

        ExtCheckBox avoidBox = ExtJSFormFactory.createCheckBox();

        avoidBox.setProperty(ExtJSProperty.NAME, IMPLICIT_BOX_NAME);
        avoidBox.setProperty(ExtJSProperty.CHECKED, Boolean.TRUE);
        avoidBox.setProperty(ExtJSProperty.STYLE, "{ marginLeft: '15px' }");
        avoidBox.setProperty(ExtJSProperty.BOX_LABEL, "Avoid implicit Splits and Joins");

        form.addItem(fSet);
        form.addItem(envelopeBox);
        form.addItem(avoidBox);

        return form;
    }

    @Override
    protected String getItemText() {
        return "Generate Behavioral Interface";
    }
    
    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        Set<Class<? extends ProcessModel>> classes = new HashSet<Class<? extends ProcessModel>>();
        classes.add( BPMNModel.class );
        return classes;
    }
}
