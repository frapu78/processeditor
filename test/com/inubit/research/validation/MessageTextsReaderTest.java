/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author tmi
 */
public class MessageTextsReaderTest {

    MessageTexts reader;

    @Before
    public void setUp() {
        InputStream stream = this.getClass().getResourceAsStream("/validation-messages.xml");
        
        try {
            reader = new MessageTexts(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream));
        } catch (IOException ex) {
            Logger.getLogger(MessageTextsReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MessageTextsReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(MessageTextsReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testLongText() {
        assertEquals(
                "Every Activity should have a label, that indicates, what is done.",
                reader.getLongText("unlabeledActivity"));
    }

    @Test
    public void testShortText() {
        assertEquals("EventBasedGateway with outgoing conditional Flow",
                reader.getShortText("conditionalFlowFromEBGW"));
    }

    @Test
    public void testLevel() {
        assertEquals(ValidationMessage.TYPE_ERROR,
                reader.getLevel("adHocTransaction"));
        assertEquals(ValidationMessage.TYPE_WARNING,
                reader.getLevel("receiveTaskWithoutMessageFlow"));
        assertEquals(ValidationMessage.TYPE_INFO,
                reader.getLevel("unlabeledComplexGateway"));
    }
}
