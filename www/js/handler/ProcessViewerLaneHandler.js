Ext.define("Inubit.WebModeler.handler.ProcessViewerLaneHandler", {
	extend: 'Ext.util.Observable',
	constructor : function( config ) {
		this.lane = config.lane;
		Inubit.WebModeler.handler.ProcessViewerLaneHandler.superclass.constructor.call( this, config );
	},
	
	handleSelection : function ( event ) {
		ProcessViewer.instance.mouseListener.mouseDownOnNode(event, this.lane);
        this.lane.select();
        return false;
	},
	
	getSurroundingPool : function() {
        if (this.lane.cluster.isBPMNPool())
            return this.lane.cluster;

        return this.lane.cluster.getLaneHandler().getSurroundingPool();
    }
})


function LaneHandler(lane) {
    if (!(lane.prototype instanceof ProcessNode) || !(lane.isBPMNLane()))
        return null;

    this.lane = lane;

    this.handleSelection = function(event) {
        ProcessViewer.instance.mouseListener.mouseDownOnNode(event, this.lane);
        this.lane.select();
        return false;
    }

    this.getSurroundingPool = function() {
        if (this.lane.cluster.isBPMNPool())
            return this.lane.cluster;

        return this.lane.cluster.getLaneHandler().getSurroundingPool();
    }
}


