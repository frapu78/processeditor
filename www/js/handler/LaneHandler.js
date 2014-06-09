Ext.namespace("Inubit.WebModeler");

Inubit.WebModeler.LaneHandler = Ext.extend(Object, {
	constructor: function(config) {
		this.lane = config.lane;
	},
	handleSelection : function() {
        if (ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this.getSurroundingPool())) {
            ProcessEditor.instance.currentlyDraggedNode = this.getSurroundingPool();
            ProcessEditor.instance.mouseListener.enableDragging();
            return false;
        }

        ProcessEditor.instance.selectionHandler.dropSelection();
        this.lane.setSelected(true, false);
        this.lane.showContextMenu();
        ProcessEditor.instance.selectionHandler.singleSelect(this.lane);
        ProcessEditor.instance.displayProperties(this.lane);
        return false;
    },
    handleDeletion : function(skipRedraw) {
         //fake request to get new pool image
//        if (skipRedraw == null || !skipRedraw) {
//            this.redrawPool();
//        }

        var childNodes = this.lane.getRecursiveChildNodes();
        var newParent = this.getNewCluster();
        for (var i = 0; i < childNodes.length; i++) {
           if (childNodes[i].isBPMNLane()) {
               childNodes[i].children = new Array();
               childNodes[i].remove(childNodes[i], true);
           } else {
               childNodes[i].updateCluster(newParent);
           }
        }

//        this.lane.cluster.updateBoundsFromServer(true);
    },
    isVertical : function() {
    	if(!Ext.isDefined(this.getSurroundingPool())) {
    		return false;
    	}
   		if (this.getSurroundingPool().getProperty('vertical_Pool') == '1') {
			return true;
    	}
    	return false;
    },
    getSurroundingPool : function() {
        if (this.lane.cluster.isBPMNPool())
            return this.lane.cluster;

        return this.lane.cluster.getLaneHandler().getSurroundingPool();
    },
    redrawPool : function() {
        //fake request to get new pool image
//        this.getSurroundingPool().updateProperty("width", ""+this.getSurroundingPool().width);
        this.getSurroundingPool().updateBoundsFromServer(true);
    },
    updateProperty : function(key, value) {
        this.lane.setProperty(key, Util.unEscapeString(value));
        this.getSurroundingPool().updateLane(this.lane.getId(), key, value);
    },
    getNewCluster : function() {
        if (this.lane.cluster.children.length >= 1) {
            var lane = null;
            if (this.isLowestLane()) {
                lane = this.getLaneAbove();
                return lane.getLaneHandler().getLowestAndDeepestChildLane();
            } else {
                lane = this.getLaneBelow();
                return lane.getLaneHandler().getHighestAndDeepestChildLane();
            }

        } else {
            return this.lane.cluster;
        }

        return null;
    },
    isLowestLane : function() {
        var y = this.lane.y;

        var siblings = this.lane.cluster.children;

        for (var i = 0; i < siblings.length; i++)
            if (siblings[i].y > y)
                return false;

        return true;
    },
    getLaneBelow : function() {
        var y = this.lane.y;
        var diff = null;
        var lane = null;

        var siblings = this.lane.cluster.children;

        for (var i = 0; i < siblings.length; i++) {
            if (siblings[i].y <= y)
                continue;

            if (diff == null) {
                lane = siblings[i];
                diff = siblings[i].y - y;
                continue;
            }

            if (siblings[i].y - y < diff) {
                diff = siblings[i].y - y;
                lane = siblings[i];
            }
        }

        return lane;
    },
    getLaneAbove : function() {
        var y = this.lane.y;
        var diff = null;
        var lane = null;

        var siblings = this.lane.cluster.children;

        for (var i = 0; i < siblings.length; i++) {
            if (siblings[i].y >= y)
                continue;

            if (diff == null) {
                lane = siblings[i];
                diff = y - siblings[i].y;
                continue;
            }

            if (y - siblings[i].y < diff) {
                diff = y - siblings[i].y;
                lane = siblings[i];
            }
        }

        return lane;
    },
    getHighestAndDeepestChildLane : function() {
        if (this.lane.children.length == 0 || !this.lane.children[0].isBPMNLane())
            return this.lane;

        var y = null;
        var lane = null;
        for (var i = 0; i <this.lane.children.length; i++) {
            if (y == null) {
                y = this.lane.children[i].y;
                lane = this.lane.children[i];
                continue;
            }

            if (this.lane.children[i].y < y) {
                y = this.lane.children[i].y;
                lane = this.lane.children[i];
            }
        }

        return lane.getLaneHandler().getHighestAndDeepestChildLane();
    },
    getLowestAndDeepestChildLane : function() {
        if (this.lane.children.length == 0 || !this.lane.children[0].isBPMNLane())
            return this.lane;

        var y = null;
        var lane = null;
        for (var i = 0; i <this.lane.children.length; i++) {
            if (y == null) {
                y = this.lane.children[i].y;
                lane = this.lane.children[i];
                continue;
            }

            if (this.lane.children[i].y > y) {
                y = this.lane.children[i].y;
                lane = this.lane.children[i];
            }
        }

        return lane.getLaneHandler().getLowestAndDeepestChildLane();
    },
    updateMetaData : function() {
        var selected = (this.lane.selectionFrame != null);
        this.lane.setSelected(false);
        this.lane.updateMetadata();

        for (var i = 0; i < this.lane.children.length; i++) {
            if (this.lane.children[i].isBPMNLane()) {
                this.lane.children[i].getLaneHandler().updateMetaData();
            }
        }

        this.lane.setSelected(selected, false);
    },
    /**
     * @return return true if the its the lane at the right border
     */
    isRightBorderLane : function() {
    	var pool = this.getSurroundingPool();
    	if(pool.children.length <= 0) {
    		return false;
    	}
    	var x = this.lane.x;
    	for (var i = 0; i < pool.children.length; i++) {
    		if (pool.children[i].isBPMNLane()) {
    			if(pool.children[i].x > x) {
    				return false;
    			}
    		}
    	}
    	return true;
    }

});