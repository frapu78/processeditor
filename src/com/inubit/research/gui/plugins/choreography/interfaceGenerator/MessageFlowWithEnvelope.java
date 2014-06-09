/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.interfaceGenerator;

import java.awt.Graphics;
import java.awt.Point;
import java.util.List;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Message;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * a kind of MessageFlow, that shows an Envelope in its centre
 * @author tmi
 */
public class MessageFlowWithEnvelope extends MessageFlow {

  /**
   * property for the label of the envelope
   */
  public static final String PROP_ENVELOPE_LABEL = "envelopeLabel";
  /**
   * property for determining, wheter the envelope should be marked as initiating
   * (white background) or not (grey background).
   */
  public static final String PROP_INITIATING = "initiating";
  private Message envelope;

  /**
   * creates an intiating MessageFlow, with an empty envelope label.
   */
  public MessageFlowWithEnvelope() {
    super();
    initialize("", true);
  }

  /**
   * creates an intiating MessageFlow, with an empty envelope label starting
   * at source and ending at target
   */
  public MessageFlowWithEnvelope(ProcessNode source, ProcessNode target) {
    super(source, target);
    initialize("", true);
  }

  public MessageFlowWithEnvelope(
          ProcessNode source, ProcessNode target, String envelopeLabel,
          boolean initiating) {
    super(source, target);
    initialize(envelopeLabel, initiating);
  }

  private void initialize(String envelopeLabel, boolean initiate) {
    setClassType();
    setProperty(PROP_INITIATING, initiate ? TRUE : FALSE);
    setProperty(PROP_ENVELOPE_LABEL, envelopeLabel);
    setPropertyEditor(PROP_INITIATING, new BooleanPropertyEditor());
    envelope = new Message();
    setMessageInitiating(initiate);
    setMessageText(envelopeLabel);

  }

  @Override
  public Element getSerialization(Document xmlDoc) {
    /*This method is overriden, because the MessageFlowWithEnvelope is serialized
    as a normal message flow (because it cannot be loaded as M...WithEnvelope,
    because this class is not known to the workbench) and in order not to loose
    the envelope label, the normal MessageFlow´s label is set to the envelope label*/
    Element result = super.getSerialization(xmlDoc);
    NodeList properties = result.getElementsByTagName("property");
    for (int i = 0; i < properties.getLength(); ++i) {
      if (properties.item(i).getAttributes().getNamedItem("name").getNodeValue().
              equals(PROP_LABEL)) {
        properties.item(i).getAttributes().getNamedItem("value").
                setNodeValue(getEnvelopeLabel());
      }
      if (properties.item(i).getAttributes().getNamedItem("name").getNodeValue().
              equals(PROP_ENVELOPE_LABEL)) {
        result.removeChild(properties.item(i));
      }
    }
    return result;
  }

  private void setClassType() {//fooling the layouter (and the serialization)
    setProperty(PROP_CLASS_TYPE, "net.frapu.code.visualization.bpmn.MessageFlow");
  }

  public void setEnvelopeLabel(String envelopeLabel) {
    setProperty(PROP_ENVELOPE_LABEL, envelopeLabel);
  }

  public String getEnvelopeLabel() {
    return getProperty(PROP_ENVELOPE_LABEL);
  }

  /**
   * sets property PROP_INITIATING, which states, wheter the envelope on this
   * edge should be shown as initiating or not.
   */
  public void setInitiating(boolean initiating) {
    if (initiating) {
      setProperty(PROP_INITIATING, TRUE);
    } else {
      setProperty(PROP_INITIATING, FALSE);
    }
  }

  /**
   * @return true, iff the envelope is shown as initiating
   */
  public boolean isInitiating() {
    return getProperty(PROP_INITIATING).equals(TRUE);
  }

  /**
   * @return true, iff the envelope is shown as initiating
   */
  public boolean getInitiating() {
    return isInitiating();
  }

  @Override
  public void paint(Graphics graphics) {
    super.paint(graphics);
    setMessagePosition();
    setMessageText();
    setMessageInitiating();
    envelope.paint(graphics);
  }

  @Override
  public void copyPropertiesFrom(ProcessEdge edge) {
    super.copyPropertiesFrom(edge);
    if (edge.getPropertyKeys().contains(PROP_ENVELOPE_LABEL)) {
      setEnvelopeLabel(edge.getProperty(PROP_ENVELOPE_LABEL));
    }
    if (edge.getPropertyKeys().contains(PROP_INITIATING)) {
      setInitiating(edge.getProperty(PROP_INITIATING).equals(TRUE));
    }
  }

  @Override
  public void clonePropertiesFrom(ProcessEdge edge) {
    super.clonePropertiesFrom(edge);
    if (edge.getPropertyKeys().contains(PROP_ENVELOPE_LABEL)) {
      setEnvelopeLabel(edge.getProperty(PROP_ENVELOPE_LABEL));
    }
    if (edge.getPropertyKeys().contains(PROP_INITIATING)) {
      setInitiating(edge.getProperty(PROP_INITIATING).equals(TRUE));
    }
  }

  @Override
  public MessageFlowWithEnvelope clone() {
    MessageFlowWithEnvelope clone =
            new MessageFlowWithEnvelope(getSource(), getTarget());
    clone.copyPropertiesFrom(this);
    return clone;
  }

  private void setMessagePosition() {
    List<Point> routingPoints = getRoutingPoints();

    int x, y, routingPointCount = routingPoints.size();
    if (routingPointCount % 2 == 0) {
      x = (routingPoints.get(routingPointCount / 2).x
              + routingPoints.get(routingPointCount / 2 - 1).x) / 2;
      y = (routingPoints.get(routingPointCount / 2).y
              + routingPoints.get(routingPointCount / 2 - 1).y) / 2;
    } else {
      x = routingPoints.get((routingPointCount - 1) / 2).x;
      y = routingPoints.get((routingPointCount - 1) / 2).y;
    }
    envelope.setPos(x, y);
  }

  /**
   * sets the text of the Message-object, that is used for displaying the envelope
   */
  private void setMessageText(String text) {
    envelope.setText(text);
  }

  /**
   * sets the text of the Message-object, that is used for displaying the
   * envelope to the value of the property PROP_ENVELOPE_LABEL
   */
  private void setMessageText() {
    setMessageText(getEnvelopeLabel());
  }

  /**
   * sets the initiating-property of the Message-objectthat is used for
   * displaying the envelope
   */
  private void setMessageInitiating(String value) {
    envelope.setProperty(Message.PROP_INITIATE, value);
  }

  /**
   * sets the initiating-property of the Message-objectthat is used for
   * displaying the envelope
   */
  private void setMessageInitiating(boolean value) {
    setMessageInitiating(value ? TRUE : FALSE);
  }

  /**
   * sets the initiating-property of the Message-objectthat is used for
   * displaying the envelope to the value of the property PROP_INITIATING
   */
  private void setMessageInitiating() {
    setMessageInitiating(getProperty(PROP_INITIATING));
  }
}
