function Canvas(width, height, region, rWidth, rHeight, mouseListener) {
    this.width = width;
    this.height = height;

    this.mouseListener = mouseListener;

    this.drawGatter = function(hDist, vDist) {
        var height = 10000;
        var width = 10000;
        var currentH = 0;
        var currentV = 0;

        while (currentH <= height) {
            var gatterLine = this.canvas.path("M0 "+currentH+" L"+width+" "+currentH);
            gatterLine.attr({stroke:"lightgray", "stroke-width":0.25});

            currentH += hDist;
        }

        while (currentV <= width) {
            var gatterLine = this.canvas.path("M"+currentV+" 0 L"+currentV+" "+height);
            gatterLine.attr({stroke:"lightgray", "stroke-width":0.25});

            currentV += vDist;
        }
    }

    this.setInitialSize = function( width, height ) {
        var canvasDOM = this.getCanvasDOMElement();

        var canvasHeight = canvasDOM.getAttribute("height");
        var canvasWidth = canvasDOM.getAttribute("width");

        if (width > canvasWidth || height > canvasHeight)
            this.setSize(width, height);
    }

    this.setSize = function(newWidth, newHeight) {
        var canvasDOM = this.getCanvasDOMElement();

        if (newWidth) {
            var eWidth = parseInt(this.extCmp.getWidth());
            var width = Math.max(newWidth, eWidth);

            canvasDOM.setAttribute("width", width);
            this.width = width;
        }

        if (newHeight) {
            var eHeight = parseInt(this.extCmp.getHeight());
            var height = Math.max(newHeight, eHeight);
            canvasDOM.setAttribute("height", height);
            this.height = height;
        }
       
        Ext.getCmp('center_region').doLayout();
    }

    this.resize = function(cWidth, cHeight) {
        var eWidth = parseInt(this.extCmp.getWidth());
        var eHeight = parseInt(this.extCmp.getHeight());
        if (eWidth < cWidth) {
            var mWidth = this.width//parseInt(canvasDOM.getAttribute("width"))

            if (cWidth > mWidth) {
                var canvasDOM = this.getCanvasDOMElement();
                canvasDOM.setAttribute("width", cWidth);
                this.width = cWidth;
            }
        }

        if (eHeight < cHeight) {
            var mHeight = this.height; //parseInt(canvasDOM.getAttribute("height"))

            if (cHeight > mHeight) {
                var canvasDOM = this.getCanvasDOMElement();
                canvasDOM.setAttribute("height", cHeight);
                this.height = cHeight;
            }
        }

        this.extCmp.setSize(cWidth, cHeight);
    }

    this.getCanvasDOMElement = function() {
        //@TODO in IE we won't have a svg-element
        return this.extCmp.el.dom.getElementsByTagName("svg")[0];
    }

    this.nodeReaches = function(x, y, visible) {
        var scrollPos = this.extCmp.body.getScroll()

        if (visible) {
            if (x > scrollPos.left + this.extCmp.getWidth()) {
                var extend = Math.max(15, x - (scrollPos.left + this.extCmp.getWidth()));
                this.extCmp.body.dom.scrollLeft += extend;
            }
            if (x < scrollPos.left) {
                var reduce = Math.min(-15, x - scrollPos.left);
                var newScroll = Math.max(0, scrollPos.left + reduce);
                this.extCmp.body.dom.scrollLeft = newScroll;
            }

            if (y > scrollPos.top + this.extCmp.getHeight()) {
                var extend = Math.max(15, y - (scrollPos.top + this.extCmp.getHeight()));
                this.extCmp.body.dom.scrollTop += extend;
            }

            if (y < scrollPos.top) {
                var reduce = Math.min(-15, y - scrollPos.top);
                var newScroll = Math.max(0, scrollPos.top + reduce);
                this.extCmp.body.dom.scrollTop = newScroll;
            }
        }
        if (x > this.width) {
            var extend = Math.max(25, x - this.width)
            this.setSize(this.width + extend, null);
        }

        if (y > this.height) {
            var extend = Math.max(25, y - this.height)
            this.setSize(null, this.height + extend);
        }
    }

    this.getCanvas = function() {
        return this.canvas;
    }

    this.getExtCmp = function() {
        return this.extCmp;
    }


    region.add(new Ext.Panel({
            id: Canvas.EXT_ID,
            height: rHeight,
            width: rWidth,
            autoScroll: true,
            layout: 'absolute'
    }));

    this.extCmp = Ext.getCmp(Canvas.EXT_ID);

    region.doLayout();

    var mListener = this.mouseListener;

    //bind events
    this.extCmp.body.on('mouseup', function(event) {
        mListener.mouseUp(event);
    });

    this.extCmp.body.on('mousemove', function(event) {
        mListener.mouseMove(event);
    });

    this.canvas = Raphael(this.extCmp.body.dom, this.width, this.height);

    this.extCmp.body.on('mousedown', function(event) {
        if ( this.extCmp.body.isScrollable()) {
            var x = this.extCmp.body.getX() + this.extCmp.body.getWidth() - 16;
            
            if (event.getPageX() > x )
                return false;

            var y = this.extCmp.body.getY() + this.extCmp.body.getHeight() - 16;

            if ( event.getPageY() > y )
                return false;

        }
        mListener.mouseDown(event);
    }, this);

}

Canvas.EXT_ID = 'editor';
