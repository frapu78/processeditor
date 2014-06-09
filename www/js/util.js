function Util() {
}

Util.parseProperties = function(xmlDoc) {
    var properties = new Array();
    var propertyTags = xmlDoc.getElementsByTagName("property");

    for (var i = 0; i < propertyTags.length; i++) {
        properties[propertyTags[i].getAttribute("name")] =
        propertyTags[i].getAttribute("value");
    }
    return properties;
}

Util.parsePropertiesFromJSON = function( json ) {
    var properties = new Array();

    for ( var i = 0; i < json.length; i++ ) {
        var value = json[i].value ? json[i].value : "";
        properties[json[i].name] = value;
    }

    return properties;
}

Util.parseParameters = function(query) {
    var params = new Array();
    var queryParts = query.split("&");

    for (var i = 0; i < queryParts.length; i++) {
        var index = queryParts[i].indexOf("=");

        var name = queryParts[i].substring(0, index);
        var value = queryParts[i].substring( index + 1 );

        params[name] = value;
    }

    return params;
}

Util.isAttachable = function(target, attachable) {
    if (target == null || !target.isAttachmentPossible || !attachable.isAttachable) return false;

    if ((attachable.x - 10 >= target.x + target.width / 2 - 10 &&
        attachable.x - 10 <= target.x + target.width / 2) ||
        (attachable.x + 10 >= target.x + target.width / 2 - 10 &&
        attachable.x + 10 <= target.x + target.width / 2) ||
        (attachable.x - 10 <= target.x - target.width / 2 + 10 &&
        attachable.x - 10 >= target.x - target.width / 2) ||
        (attachable.x + 10 <= target.x - target.width / 2 + 10 &&
        attachable.x + 10 >= target.x - target.width / 2) ||
        (attachable.y - 10 >= target.y + target.height / 2 - 10 &&
        attachable.y - 10 <= target.y + target.height / 2) ||
        (attachable.y + 10 >= target.y + target.height / 2 - 10 &&
        attachable.y + 10 <= target.y + target.height / 2) ||
        (attachable.y - 10 <= target.y - target.height / 2 + 10 &&
        attachable.y - 10 >= target.y - target.height / 2) ||
        (attachable.y + 10 <= target.y - target.height / 2 + 10 &&
        attachable.y + 10 >= target.y - target.height / 2)) {
            return true;
        }


    return false;
}

Util.stopEvent = function(event) {
    event.cancelBubble =  true;

    if (event.stopPropagation)
        event.stopPropagation();
}

Util.isPointInArea = function(x, y, topX, topY, bottomX, bottomY) {
    return (x >= topX && x <= bottomX && y >= topY && y <= bottomY) ||
            (x <= topX && x >= bottomX && y <= topY && y >= bottomY) ||
            (x <= topX && x >= bottomX && y >= topY && y <= bottomY) ||
            (x >= topX && x <= bottomX && y <= topY && y >= bottomY);
}

Util.checkContainment = function( array , object ) {
    for ( var i=0; i < array.length; i++ )
        if ( array[i] == object )
            return true;

    return false;
}

Util.getPath = function(uri) {
    var splitPos = 0;
    if (uri.indexOf("http://") == 0)
        splitPos = uri.indexOf("/", 7);
    else if (uri.indexOf("https://") == 0)
        splitPos = uri.indexOf("/", 8);
    else
        splitPos = uri.indexOf("/");

    return uri.substring(splitPos, uri.length);
}

Util.toJavaRGB = function(hex) {
    if (("" + hex).indexOf("-") == 0) return hex;

    var value = -1;
    for (var i = 0; i < 6; i++) {
        var hexchar = hex[5-i];
        if (!hexchar) return -1
        value -= Math.pow(16, i) * (15 - "0123456789ABCDEF".indexOf(hexchar.toUpperCase()));
    }

    return value;
}

Util.fromJavaRGB = function(hex) {
    var value = parseInt(hex);
    var rgb = "";
    for (var i = 5; i>= 0; i--) {
        var count = 15;
        while ((value + Math.pow(16, i)) < 0) {
            value += Math.pow(16, i);
            count--;
        }

        if (count <= 9) rgb += count;
        if (count == 10) rgb += "a";
        if (count == 11) rgb += "b";
        if (count == 12) rgb += "c";
        if (count == 13) rgb += "d";
        if (count == 14) rgb += "e";
        if (count == 15) rgb += "f";
    }

    return rgb;
}

Util.computeDistanceLineToPoint = function( l1, l2, p ) {
    var foot = Util.getFootPoint(l1, l2, p);

    // Entfernung zwischen Punkt und Fußpunkt
    return Math.sqrt((foot.x - p.x) * (foot.x - p.x) + (foot.y - p.y) * (foot.y - p.y));
}

Util.getFootPoint = function( l1, l2, p ) {
    var line = new Array();
    var foot = new Array();

    line.x = l1.x - l2.x; // Richtungsvektor
    line.y = l1.y - l2.y;

    var line_len = Math.sqrt(line.x * line.x + line.y * line.y);

    line.x /= line_len; // Einheitsvektor
    line.y /= line_len;

    // orthogonale Projektion
    var lambda = (p.x - l2.x) * line.x + (p.y - l2.y) * line.y;

    foot.x = l2.x + lambda * line.x; // Fußpunkt auf Geraden
    foot.y = l2.y + lambda * line.y;

    return foot;
}

Util.escapeString = function(str) {
     if (str == null) return null;

     str = str.replace(/%/g,"%25");
     str = str.replace(/ /g,"%20");
     str = str.replace(/</g,"%3C");
     str = str.replace(/>/g,"%3E");
     str = str.replace(/#/g,"%23");
     str = str.replace(/\{/g,"%7B");

     str = str.replace(/\}/g,"%7D");
     str = str.replace(/\|/g,"%7C");
     str = str.replace(/\\/g,"%5C");
     str = str.replace(/\n/g,"%0A");
     str = str.replace(/\^/g,"%5E");
     str = str.replace(/~/g,"%7E");

     str = str.replace(/\[/g,"%5B");
     str = str.replace(/\]/g,"%5D");
     str = str.replace(/'/g,"%60");
     str = str.replace(/;/g,"%3B");
     str = str.replace(/,/g,"%2F");

     str = str.replace(/\?/g,"%3F");
     str = str.replace(/:/g,"%3A");
     str = str.replace(/@/g,"%40");
     str = str.replace(/=/g,"%3D");
     str = str.replace(/&/g,"%26");
     str = str.replace(/\$/g,"%24");

     str = str.replace(/\u00e4/g, "%E4");
     str = str.replace(/\u00c4/g, "%C4");
     str = str.replace(/\u00f6/g, "%F6");
     str = str.replace(/\u00f6/g, "%D6");
     str = str.replace(/\u00fc/g, "%FC");
     str = str.replace(/\u00dc/g, "%DC");
     str = str.replace(/\u00df/g, "%DF");

     return str;
}

Util.unEscapeString = function(str) {
     if (!str) return null;

     str = str.replace(/%25/g,"%");
     str = str.replace(/%20/g," ");
     str = str.replace(/%3C/g,"<");
     str = str.replace(/%3E/g,">");
     str = str.replace(/%23/g,"#");
     str = str.replace(/%7B/g,"{");

     str = str.replace(/%7D/g,"}");
     str = str.replace(/%7C/g,"|");
     str = str.replace(/%5C/g,"\\");
     str = str.replace(/%0A/g,"\n");
     str = str.replace(/%5E/g,"^");
     str = str.replace(/%7E/g,"~");

     str = str.replace(/%5B/g,"[");
     str = str.replace(/%5D/g,"]");
     str = str.replace(/%60/g,"'");
     str = str.replace(/%3B/g,";");
     str = str.replace(/%2F/g,",");

     str = str.replace(/%3F/g,"?");
     str = str.replace(/%3A/g,":");
     str = str.replace(/%40/g,"@");
     str = str.replace(/%3D/g,"=");
     str = str.replace(/%26/g,"&");
     str = str.replace(/%24/g,"$");

     str = str.replace(/%E4/g, "\u00e4");
     str = str.replace(/%C4/g, "\u00c4");
     str = str.replace(/%F6/g, "\u00f6");
     str = str.replace(/%D6/g, "\u00f6");
     str = str.replace(/%FC/g, "\u00fc");
     str = str.replace(/%DC/g, "\u00dc");
     str = str.replace(/%DF/g, "\u00df");

     return str;
}

Util.readCookie = function(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

Util.getComponentSize = function(cmp) {
    var width = 0;
    var height = 0;

    if (cmp == document.body) {
        if (window.innerWidth) {
            width = parseInt(window.innerWidth);
        } else if (document.body && document.body.offsetWidth) {
            width = parseInt(document.body.offsetWidth);
        }

        if (window.innerHeight) {
            height = parseInt(window.innerHeight);
        } else if (document.body && document.body.offsetHeight) {
            height = parseInt(document.body.offsetHeight);
        }

    } else {
        if (cmp.style) {
            width = cmp.style.width;
            height = cmp.style.height;
        } else {
            return cmp.getSize();
        }
    }

    return {width: width, height: height};
}

Util.getUsers = function() {
    var req =  new XMLHttpRequest();
    req.open("GET", Util.getContext() + "/users/users", false);
    req.send(null);

    var users = new Array();
    var userEls = req.responseXML.getElementsByTagName("user");

    for (var i = 0 ; i < userEls.length; i++) {
        users.push(userEls[i].getAttribute("name"));
    }

    return users;
}

Util.getCurrentUserInfo = function() {
    var req = new XMLHttpRequest();
    req.open("GET", Util.getContext() + "/users/this", false);
    req.send(null);

    return Util.parseProperties(req.responseXML.getElementsByTagName("user")[0]);
}

Util.logoutCurrentUser = function() {
    var req = new XMLHttpRequest();
    req.open("POST", Util.getContext() + "/users/logout", false);
    req.send(null);

    if ( req.responseXML.getElementsByTagName("logout")[0].textContent == "true")
        window.location = '.';
}

Util.getGroups = function() {
    var req =  new XMLHttpRequest();
    req.open("GET", Util.getContext() + "/users/groups", false);
    req.send(null);

    var groups = new Array();
    var groupEls = req.responseXML.getElementsByTagName("group");

    for (var i = 0 ; i < groupEls.length; i++) {
        groups.push(groupEls[i].getAttribute("name"));
    }

    return groups;
}

Util.getContext = function( customPath ) {
    var uri = "" + window.location;
    var path = Util.getPath(uri);

    if ( path.indexOf("?") > - 1 )
        path = path.substring(0, path.indexOf("?"));

    var context;

    if ( path.indexOf("/models") > - 1)
        context = path.substring(0, path.indexOf("/models"));
    else if ( path.indexOf("/admin") > - 1)
        context = path.substring(0, path.indexOf("/admin"));
    else if ( Ext.isDefined(customPath) && path.indexOf(customPath) > -1 )
    	context = path.substring(0, path.indexOf(customPath));    	
    else
        context = path;

    if ( context.length > 0 && context[context.length - 1] == "/")
        context = context.substring(0, context.length - 1);

//    if ( context[0] != "/" )
//        context = "/" + context;

    return context;
}

Util.generateAttributeName = function (object) {
	var name = "newAttribute";
        return name;
}

//Color constants taken from inubit colors
Util.COLOR_GREEN = "#9FBB64";
Util.COLOR_LIGHTGREEN = "#C8D8A6";
Util.COLOR_RED = "#B33C1B";
Util.COLOR_LIGHTRED = "#E1B1A4";
Util.COLOR_BLUE = "#384A7C";
Util.COLOR_LIGHTBLUE = "#87CEFA";
Util.COLOR_ORANGE = "#FF6C00";
Util.COLOR_GRAY = "#F3F3EE";

//Icons and Images
Util.ICON_NEW = '/pics/menu/new_small.gif';
Util.ICON_SAVE = '/pics/menu/save_small.gif';
Util.ICON_IMPORT = '/pics/menu/import.gif';
Util.ICON_EXPORT = '/pics/menu/export.gif';
Util.ICON_LAYOUT = '/pics/menu/icon_16x16_auto-ausrichten.gif';
Util.ICON_CONNECT = '/pics/is_16.gif';

Util.ICON_PDF = '/pics/menu/pdf.png';
Util.ICON_RSS = '/pics/menu/rss.gif';
Util.ICON_PNG = '/pics/menu/image.png';
Util.ICON_HOME = '/pics/menu/home.png';
Util.ICON_SHARED = '/pics/menu/icon_shared.gif';

Util.ICON_CUT = '/pics/menu/cut_small.gif';
Util.ICON_COPY = '/pics/menu/copy_small.gif';
Util.ICON_PASTE = '/pics/menu/paste_small.gif';
Util.ICON_UNDO = '/pics/toolbar/arrow_undo.png';
Util.ICON_REDO = '/pics/toolbar/arrow_redo.png';

Util.ICON_PLUS = '/pics/menu/add.png';
Util.ICON_MINUS = '/pics/menu/delete.png';
Util.ICON_PENCIL = '/pics/menu/pencil_small.gif';
Util.ICON_MAN = '/pics/menu/user_small.gif';
Util.ICON_GROUP = '/pics/menu/group_small.gif';
Util.ICON_LOGOUT = '/pics/menu/logout_small.gif';
//Util.ICON_LOGIN = '/pics/menu/login_small.gif';
Util.ICON_LOGIN = '/pics/menu/key_small.gif';
Util.ICON_KEY = '/pics/menu/key_small.gif';
Util.ICON_CHART = '/pics/menu/icon_16x16_barchart.gif';
Util.ICON_MAIL = '/pics/menu/mail_small.gif';
Util.ICON_LINK = '/pics/icon_16x16_links.gif';

Util.ICON_FOLDER = '/pics/ext/folder.gif';
Util.ICON_NEWFOLDER = '/pics/menu/new_folder_small.gif';
Util.ICON_MODEL = '/pics/icon_small.png'

Util.ICON_CHECKMARK = '/pics/menu/checkmark_small.gif';
Util.ICON_REFRESH = '/pics/symbols/refresh15x16.png';
Util.ICON_FOLLOW = '/pics/menu/follow_link.gif';

Util.ICON_CHECKALL = '/pics/menu/checkAll.png';
Util.ICON_UNCHECKALL = '/pics/menu/uncheckAll.png';

Util.ICON_PLUGIN = '/pics/menu/plugin.png';

Util.ICON_DELETE = '/pics/menu/delete_small.gif';
Util.ICON_NEW_EDGE = '/pics/menu/mini_arrow_top_right.gif';
Util.ICON_NEW_COMMENT = '/pics/menu/add_comment_small.gif';
Util.ICON_EDIT_COMMENT = '/pics/menu/edit_comment_small.gif';
Util.ICON_COMMENTS = '/pics/menu/comments_small.gif';
Util.ICON_DISCUSS = '/pics/menu/discuss_small.gif'
Util.ICON_REFACTORING = '/pics/menu/refactoring.gif';
Util.ICON_LANE_BELOW = '/pics/menu/add_lane_above.gif';
Util.ICON_LANE_RESIZE = '/pics/ext/mini-bottom.gif';
Util.ICON_SE_RESIZE = '/pics/ext/se-handle-dark.gif';
Util.ICON_MOVE_UP = '/pics/ext/col-move-bottom.gif';
Util.ICON_MOVE_DOWN = '/pics/ext/col-move-top.gif';
Util.IMG_INUBIT = '/pics/inubit.png';
Util.IMG_IWB = '/pics/icon.png';
Util.IMG_FOLDER = '/pics/folder.png';
Util.IMG_TRASH = '/pics/trash.png';

// Animation times
Util.ANIMATION_FADE_IN_TIME = 0.5;
Util.ANIMATION_FADE_OUT_TIME = 0.7;

// Preview sizes
Util.PREVIEW_SMALL = 100;
Util.PREVIEW_MEDIUM = 150;
Util.PREVIEW_LARGE = 200;
Util.PREVIEW_XLARGE = 300;

// Preview labels
Util.PREVIEW_SMALL_LABEL = "Small";
Util.PREVIEW_MEDIUM_LABEL = "Medium";
Util.PREVIEW_LARGE_LABEL = "Large";
Util.PREVIEW_XLARGE_LABEL = "X-Large";