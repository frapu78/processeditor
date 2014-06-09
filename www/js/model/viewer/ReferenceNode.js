Ext.define("Inubit.WebModeler.model.viewer.ReferenceNode", {
    extend: 'Inubit.WebModeler.model.viewer.AnnotatedProcessNode',
    constructor : function(config) {
            Inubit.WebModeler.model.viewer.ReferenceNode.superclass.constructor.call(this, config);
    }, 
    select : function() {
        this.setSelected(true, false, Util.COLOR_BLUE);
    },
    init : function(xml, canvas) {
        var properties = Util.parseProperties(xml);
        this.properties = properties;
        this.parseBounds(xml);
        this.updateMetadata();

        this.paint(canvas);
        this.alignZOrder();

        this.fireEvent("load");
    },
    bindEventsToNode : function() {
        var processNode = this;

        //handle mousedown event
        this.graphics.node.onmousedown = function(event) {
            //stop event forwarding
            Util.stopEvent(event);

            //first check for edge selection before proceeding with node selection
            processNode.rootComponent.mouseListener.updateMousePos(event);
            var mouseX = processNode.rootComponent.mouseListener.mouseX;
            var mouseY = processNode.rootComponent.mouseListener.mouseY;

            var edge = processNode.model.getEdgeCloseTo(mouseX, mouseY);

            if (edge) {
                return false;
            }

            if (event.button == 0 || event.button == 1) {
                //else, handle selection
                if (processNode.isBPMNPool()) {
                        return false;
                }
                processNode.rootComponent.mouseListener.mouseDownOnNode(event, processNode);
            }
            //make Firefox move node, not the image it contains
            return false;
        };
    }
});
