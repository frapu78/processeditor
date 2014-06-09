/*
 * Process Editor
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.domainModel;

import com.inubit.research.server.ProcessEditorServerUtils;
import java.util.Map;
import net.frapu.code.visualization.SerializableProcessObject;
import org.w3c.dom.Node;

/**
 *
 * @author fel
 */
public class Attribute extends SerializableProcessObject {

    public static final String NODE_TAG = "attribute";
    public static final String PROP_NAME = "name";
    public static final String PROP_TYPE = "type";
    public static final String PROP_VISIBILITY = "visibility";
    public static final String PROP_MULTIPLICITY = "multiplicity";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_DEFAULT_VALUE = "default";
    public static final String PROP_ID = "#id";
    
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_DATE = "date";
    public static final String TYPE_DATETIME = "datetime";
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_ENUM = "enum";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_INT = "int";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_MLTEXT = "mltext";
    public static final String TYPE_TEXT = "text";

    public Attribute(String name, String type) {
        super.setProperty(PROP_NAME, name);
        this.initializeProperties();
        super.setProperty(PROP_TYPE, type);
    }

    public Attribute(String name) {
        this.initializeProperties();
        super.setProperty(PROP_NAME, name);
    }

    protected void initializeProperties() {
        setProperty(PROP_TYPE, "");
        setProperty(PROP_VISIBILITY, Visibility.PUBLIC.toString());
        setProperty(PROP_MULTIPLICITY, "1");
        setProperty(PROP_DESCRIPTION, "");
        setProperty(PROP_DEFAULT_VALUE, "");
        setProperty(PROP_ID, "");
    }

    public Visibility getVisibility() {
        Visibility v = null;
        if (this.getProperty(PROP_VISIBILITY) != null) {
            v = Visibility.valueOf(this.getProperty(PROP_VISIBILITY));
        }

        if (v == null) {
            v = Visibility.PUBLIC;
        }

        return v;
    }

    public String getDescription() {
        return this.getProperty(PROP_DESCRIPTION);
    }

    public String getName() {
        return this.getProperty(PROP_NAME);
    }

    public String getType() {
        return this.getProperty(PROP_TYPE);
    }

    public String getMultiplicity() {
        return this.getProperty(PROP_MULTIPLICITY);
    }

    public String getId() {
        return this.getProperty(PROP_ID);
    }

    public String getDefault() {
        String defaultv = this.getProperty(PROP_DEFAULT_VALUE);
        if (defaultv.startsWith("(")) {
            defaultv = defaultv.substring(1, defaultv.length());
        }
        return defaultv;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        if (!getId().isEmpty()) {
            sb.append("{");
            sb.append(this.getId());
            sb.append("}");
        }

        if (this.getVisibility().equals(Visibility.PUBLIC)) {
            sb.append("+");
        }

        sb.append(this.getName());

        if (!this.getMultiplicity().isEmpty() && !"1".equals(this.getMultiplicity())) {
            sb.append("[");
            sb.append("1".equals(this.getMultiplicity()) ? "" : this.getMultiplicity());
            sb.append("]");
        }

        if (!this.getType().isEmpty()) {
            sb.append(":");
            sb.append(this.getType());
        }

        if (!this.getProperty(PROP_DEFAULT_VALUE).isEmpty()) {
            sb.append("(");
            sb.append(this.getProperty(PROP_DEFAULT_VALUE));
            sb.append(")");
        }

        return sb.toString();
    }

    public String toUMLAttributeString() {
        StringBuffer sb = new StringBuffer();

        sb.append(this.getVisibility().getUMLString());
        sb.append(this.getName());

        if (!this.getMultiplicity().isEmpty() && !"1".equals(this.getMultiplicity())) {
            sb.append("[");
            sb.append("1".equals(this.getMultiplicity()) ? "" : this.getMultiplicity());
            sb.append("]");
        }

        if (!this.getType().isEmpty()) {
            sb.append(":");
            sb.append(this.getType());
        }

        if (!this.getProperty(PROP_DEFAULT_VALUE).isEmpty()) {
            sb.append(" = ");
            sb.append(this.getDefault());
        }

        return sb.toString();
    }

    @Override
    protected String getXmlTag() {
        return NODE_TAG;
    }

    public static Attribute fromSerialization(Node node) {
        Map<String, String> props = ProcessEditorServerUtils.parseProperties(node);
        Attribute a = new Attribute(props.get(PROP_NAME));

        for (String key : props.keySet()) {
            a.setProperty(key, props.get(key));
        }

        return a;
    }
}
