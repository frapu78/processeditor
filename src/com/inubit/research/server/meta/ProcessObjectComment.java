/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.meta;

import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.user.LoginableUser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for elements that represent comments for models and nodes
 * @author fel
 */
public class ProcessObjectComment implements Comparable<ProcessObjectComment> {
    private static final String TAG_NAME = "comment";
    private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd. MMMM yyyy HH:mm:ss", Locale.GERMAN);

    private String commentId;
    private String elementId;
    private Date timeStamp;
    private String user;
    private int validFrom;
    private int validUntil;
    private String text;

    public ProcessObjectComment( String elementId,
                                 String user,
                                 int validFrom,
                                 String text) {

        this.commentId = null;
        this.elementId = elementId;
        this.timeStamp = new Date();
        this.user = user;
        this.validFrom = validFrom;
        this.validUntil = Integer.MAX_VALUE;
        this.text = text;
    }

    private ProcessObjectComment(   String commentId,
                                    String elementId,
                                    Date timeStamp,
                                    String user,
                                    int validFrom,
                                    int validUntil,
                                    String text
                                    ) {

        this.commentId = commentId;
        this.elementId = elementId;
        this.timeStamp = timeStamp;
        this.user = user;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.text = text;
    }


    public String getElementId() {
        return this.elementId;
    }

    public String getUser() {
        return this.user;
    }

    public int getValidFrom() {
        return this.validFrom;
    }

    public int getValidUntil() {
        return this.validUntil;
    }

    public String getText() {
        return this.text;
    }

    public Date getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * Set the ID of this comment, only if it has not been set before.
     * @param commentId the id
     */
    public void setCommentId(String commentId) {
        if (this.commentId == null)
            this.commentId = commentId;
    }

    public JSONObject toJSON( LoginableUser user ) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put("id", this.commentId);
            jo.put("elementid", this.elementId);
            jo.put("timestamp", this.getFormattedTimeStamp());
            jo.put("user", this.user);
            jo.put("validuntil", this.validUntil);
            jo.put("text", this.text);
            jo.put("editable", this.user.equals(user.getName()));
        } catch ( JSONException ex ) {
            ex.printStackTrace();
        }
        
        return jo;
    }

    public Element addToXML( LoginableUser user, Document doc, Element parentEl ) {
        Element commentEl;

        if ( parentEl == null )
            commentEl = XMLHelper.addDocumentElement(doc, TAG_NAME);
        else
            commentEl = XMLHelper.addElement(doc, parentEl, TAG_NAME);

        XMLHelper.addElement(doc, commentEl, "id").setTextContent( this.commentId );
        XMLHelper.addElement(doc, commentEl, "timestamp").setTextContent( this.getFormattedTimeStamp() );
        XMLHelper.addElement(doc, commentEl, "user").setTextContent( this.user );
        XMLHelper.addElement(doc, commentEl, "validuntil").setTextContent( String.valueOf(this.validUntil) );
        XMLHelper.addElement(doc, commentEl, "text").setTextContent(ProcessEditorServerUtils.escapeString(this.text));
        XMLHelper.addElement(doc, commentEl, "editable").setTextContent( String.valueOf(this.user.equals(user.getName())) );

        return commentEl;
    }

    @Override
    public int compareTo(ProcessObjectComment poc) {
        return this.timeStamp.compareTo(poc.timeStamp);
    }

    void serialize( Document doc, Node parentNode ) {
        try {
            Element comment = doc.createElement("elementcomment");
            comment.setAttribute("elementid", this.elementId);
            comment.setAttribute("id", this.commentId);
            comment.setAttribute("validfrom", String.valueOf(this.validFrom));
            comment.setAttribute("validuntil", String.valueOf(this.validUntil));

            Element time = doc.createElement("timestamp");
            time.setTextContent(this.getFormattedTimeStamp());

            Element userEl = doc.createElement("user");
            userEl.setTextContent(this.user);

            Element textEl = doc.createElement("text");
            textEl.setTextContent(this.text);

            comment.appendChild(time);
            comment.appendChild(userEl);
            comment.appendChild(textEl);

            parentNode.appendChild(comment);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFormattedTimeStamp() {
        return sdf.format(this.timeStamp);
    }

    public static ProcessObjectComment forNode( Node node ) {
        try {
            NamedNodeMap attributes = node.getAttributes();
            String elId = attributes.getNamedItem("elementid").getNodeValue();
            String id = attributes.getNamedItem("id").getNodeValue();
            int from = Integer.parseInt(attributes.getNamedItem("validfrom").getNodeValue());
            int until = Integer.parseInt(attributes.getNamedItem("validuntil").getNodeValue());

            NodeList children = node.getChildNodes();
            String u = null;
            String t = null;
            Date time = null;
            for (int i = 0; i < children.getLength(); i++) {
                Node n = children.item(i);

                if (n.getNodeName().equals("timestamp")) {
                    time = sdf.parse(n.getTextContent());
                } else if (n.getNodeName().equals("user")) {
                    u = n.getTextContent();
                } else if (n.getNodeName().equals("text")) {
                    t = n.getTextContent();
                }
            }

            return new ProcessObjectComment(id, elId, time, u, from, until, t);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ProcessObjectComment forAttributes( String commentId,
                                    String elementId,
                                    Date timeStamp,
                                    String user,
                                    int validFrom,
                                    int validUntil,
                                    String text
                                    ) {
        return new ProcessObjectComment(commentId, elementId, timeStamp, user, validFrom, validUntil, text);
    }
}