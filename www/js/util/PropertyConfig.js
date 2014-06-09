function PropertyConfig( modeler ) {
    this.modeler = modeler;

    this.registry = new Array();
    this.classes = new Array();
    this.types = new Array();

    this.fetchLabelsFromServer = function( language, className, asynch ) {
        var url = PropertyConfig.LABEL_URL;
        url += "?lang=" + language;
        url += "&class=" + className;

        var req = new XMLHttpRequest();
        req.open( "GET", url, asynch );

        if ( asynch ) {
            var plg = this;
            req.onreadystatechange = function() {
                if ( req.status == 200 && req.readyState == 4 ) {
                    plg.parseLabelResponse( language, className, req.responseXML );
                }
            }
        }

        req.send( null );

        if ( !asynch && req.status == 200 )
            this.parseLabelResponse( language, className, req.responseXML );
    }

    this.fetchTypesFromServer = function( className, asynch ) {
        var url = PropertyConfig.TYPE_URL;
        url += "?class=" + className;

        var req = new XMLHttpRequest();
        req.open( "GET", url, asynch );

        if ( asynch ) {
            var plg = this;
            req.onreadystatechange = function() {
                if ( req.status == 200 && req.readyState == 4 ) {
                    plg.parseTypeResponse( className, req.responseXML );
                }
            }
        }

        req.send( null );

        if ( !asynch && req.status == 200 )
            this.parseTypeResponse( className, req.responseXML );
    }

    this.fetchLanguage = function( language , asynch ) {
        this.registry[language] = new Array();
        
        for ( var i = 0; i < this.classes.length; i++ )
            this.fetchLabelsFromServer( language, this.classes[i], asynch );
    }

    this.registerClass = function( className , asynch ) {
        var lang = this.modeler.getCurrentLanguage();
        if ( !this.isClassSupported(className) ) {
            this.fetchLabelsFromServer( lang, className, asynch );
            this.fetchTypesFromServer( className, asynch );
            this.classes.push( className );
        } else if ( this.registry[lang] == null ) {
            this.fetchLanguage( lang, asynch );
        } else if ( this.registry[lang][className] == null  ) {
            this.fetchLabelsFromServer( lang, className, asynch );
        }
    }

    this.languageChanged = function( newLanguage ) {
        if ( this.registry[newLanguage] == null )
            this.fetchLanguage( newLanguage, true );
    }

    this.getLabel = function( object , propertyName ) {
        var lang = this.modeler.getCurrentLanguage();
        var className = object.getType();

        var dict = this.registry[lang];
        if ( dict == null ) {
            this.fetchLanguage( lang , false );
            dict = this.registry[lang];
        }

        var section = dict[className];

        if ( section == null ) {
            this.fetchLabelsFromServer( lang, className, false )
            section = dict[className];
        }

        var label = section[propertyName];

        if ( label == null )
            label = propertyName;

        return label;
    }

    this.getType = function( object, propertyName ) {
        if ( this.types[ object.getType() ] == null )
            this.registerClass( object.getType(), false );

        var reg = this.types[ object.getType() ];

        return ( reg != null && reg[propertyName] !=  null ? reg[propertyName] : "base" );
    }

    this.hasExtendedProperties = function( object ) {
        if ( this.types[ object.getType() ] == null )
            this.registerClass( object.getType(), false );

        var reg = this.types[ object.getType() ];

        for ( var name in reg ) {
            if ( reg[name] == "extended" )
                return true;
        }

        return false;
    }

    this.isClassSupported = function( className ) {
        for ( var i = 0; i < this.classes.length; i++ )
            if ( this.classes[i] == className )
                return true;

        return false;
    }

    this.parseLabelResponse = function( language, className, xml ) {
        if ( this.registry[language] == null )
            this.registry[language] = new Array();

        var dict = this.registry[language];

        if ( dict[className] == null )
            dict[className] = new Array();

        var section = dict[className];

        var labelEls = xml.getElementsByTagName("label");
        for ( var i = 0; i < labelEls.length; i++ ) {
            var propName = labelEls[i].getAttribute("property");
            var label = labelEls[i].textContent;

            section[propName] = label;
        }
    }

    this.parseTypeResponse = function( className, xml ) {
        if ( this.types[className] == null )
            this.types[className] = new Array();

        var reg = this.types[className];

        var propEls = xml.getElementsByTagName("property");
        for ( var i = 0; i < propEls.length; i++ ) {
            var propName = propEls[i].getAttribute("name");
            var type = propEls[i].getAttribute("type");

            reg[propName] = type;
        }
    }
}

PropertyConfig.LABEL_URL = Util.getContext() + "/utils/propertylabels"
PropertyConfig.TYPE_URL = Util.getContext() + "/utils/propertytypes"