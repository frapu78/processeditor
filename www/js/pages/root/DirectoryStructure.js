function DirectoryStructure( xml ) {
    this.homeFolder = '/';
    this.sharedFolder = '/shared';
    this.userTrash = '/attic';
    this.isRoot = '/is';

    this.types = new Array();

    this.xml = xml;
    this.structure = new Array();

    this.homeFolder = this.xml.getElementsByTagName("home")[0].textContent;
    this.isRoot = this.xml.getElementsByTagName("isroot")[0].textContent;
    this.sharedFolder = this.xml.getElementsByTagName("shared")[0].textContent;
    this.userTrash = this.xml.getElementsByTagName("trash")[0].textContent;

    this.structure[this.homeFolder] = { name: 'My Documents', children: new Array() };
    this.structure[this.isRoot] = { name: 'My iS', children: new Array() };
    this.structure[this.sharedFolder] = { name: 'Shared Documents', children: new Array() };
    this.structure[this.userTrash] = { name: 'Trash', children: new Array() };

    var folderElements = this.xml.getElementsByTagName("folder");

    for (var i = 0; i < folderElements.length; i++) {
        var folderEl = folderElements[i];
        var props = Util.parseProperties(folderEl);

        var value = props.type;
        if (value == "null")
            value = null;

        this.types[props.path] = value;
    }

    for (var name in this.types) {
        if (name == 'remove')  continue;

        var lastIndex = 0;
        var index = name.indexOf('/');
        var parent = null;
        while (index > -1) {
            var folderName = name.substring(lastIndex, index);
            var fullPath = name.substring(0, index);
            lastIndex = index + 1;
            index = name.indexOf('/', lastIndex);

            if (this.homeFolder.indexOf(fullPath) > -1) {
                parent = this.structure[this.homeFolder];
                continue;
            }

            if (this.sharedFolder.indexOf(fullPath) > -1) {
                parent = this.structure[this.sharedFolder];
                continue;
            }

            if (folderName == "")
                continue;

            if (this.structure[fullPath]) {
                parent = this.structure[fullPath];
            } else {
                var newFolder = { name: folderName, children: new Array() };
                parent.children[fullPath] = newFolder;
                this.structure[fullPath] = newFolder;

                parent = newFolder;
            }
        }

        if (lastIndex < name.length) {
            folderName = name.substring(lastIndex, name.length);

            if (this.structure[name] == null) {
                newFolder = { name: folderName, children: new Array() };
                parent.children[name] = newFolder;

                this.structure[name] = newFolder;
            }
        }
    }

    this.getChildren = function(path) {
        if (this.structure[path]) {
            return this.structure[path].children;
        }
        else
            return new Array();
    }

   
}