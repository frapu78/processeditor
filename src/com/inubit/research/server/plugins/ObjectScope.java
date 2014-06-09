/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import com.inubit.research.server.request.XMLHelper;
import java.util.Properties;
import java.util.Set;
import net.frapu.code.visualization.ProcessObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author fel
 */
public interface ObjectScope {

    enum Orientation {
        CENTER("c"),
        RIGHT("r"),
        LEFT("l"),
        BOTTOM("b"),
        TOP("t");

        private String s;
        Orientation( String s ) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

    }

    public class IconOffsetInfo {
        private int verticalOffset;
        private int horizontalOffset;

        private Orientation verticalOrientation;
        private Orientation horizontalOrientation;

        IconOffsetInfo(int x, Orientation xOr, int y, Orientation yOr ) {
            this.horizontalOffset = x;
            this.horizontalOrientation = xOr;
            this.verticalOffset = y;
            this.verticalOrientation = yOr;
        }

        public int getHorizontalOffset() {
            return horizontalOffset;
        }

        public String getHorizontalOrientation() {
            return horizontalOrientation.toString();
        }

        public int getVerticalOffset() {
            return verticalOffset;
        }

        public String getVerticalOrientation() {
            return verticalOrientation.toString();
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject jo = new JSONObject();
            jo.put("x", this.horizontalOffset);
            jo.put("w", this.getHorizontalOrientation().toString());
            jo.put("y", this.verticalOffset);
            jo.put("h", this.getVerticalOrientation().toString());
            return jo;
        }

        public void serialize( Document doc, Element parent ) {
            Element el = XMLHelper.addElement(doc, parent, "iconoffset");
            Properties props = new Properties();
            props.setProperty("x", String.valueOf(this.horizontalOffset));
            props.setProperty("w", this.getHorizontalOrientation().toString());
            props.setProperty("y", String.valueOf(this.verticalOffset));
            props.setProperty("h", this.getVerticalOrientation().toString());
            XMLHelper.addPropertyList(doc, el, props);
        }
    }

    public IconOffsetInfo getIconOffsetInfo();

    public Set<Class<? extends ProcessObject>> getSupportedObjects();
}
