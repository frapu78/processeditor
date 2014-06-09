/**
 * Abstract superclass for all admin page components
 */
function AdminPageComponent() {
    this.button = null;
    this.component = null;
    
    this.getButton = function(id) {
        if (this.button == null) {
            this.button = this.createButton(id);
        }
        return this.button;
    }

    //"abstract" method, to be implemented by subclasses
    this.createButton = function(id) {
        return false;
    }

    this.getComponent = function(parent) {
        var size = Util.getComponentSize(parent);

        this.component = this.init(size.width, size.height);

        return this.component;
    }

     //"abstract" method, to be implemented by subclasses
    this.init = function(width, height) {
        return false;
    }
}



