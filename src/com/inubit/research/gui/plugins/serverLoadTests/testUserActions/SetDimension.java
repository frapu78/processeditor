/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.testUserActions;

import com.inubit.research.client.XMLHttpRequestException;
import com.inubit.research.gui.plugins.serverLoadTests.tests.UserAction;
import java.awt.Dimension;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author uha
 */
public class SetDimension extends UserAction {

    public void run() {
        try {
            int x = getExecutingUser().getSeed().decide(200) + 1;
            int y = getExecutingUser().getSeed().decide(200) + 1;
            getExecutingUser().getRandomNode().setDimension(new Dimension(x, y));
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SetDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SetDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLHttpRequestException ex) {
            Logger.getLogger(SetDimension.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
