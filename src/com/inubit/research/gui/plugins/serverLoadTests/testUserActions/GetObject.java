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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author uha
 */
public class GetObject extends UserAction {

    public void run() {
        try {
            getExecutingUser().getRandomNode().getObject();
        } catch (IOException ex) {
            Logger.getLogger(GetObject.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GetObject.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLHttpRequestException ex) {
            Logger.getLogger(GetObject.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GetObject.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(GetObject.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(GetObject.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(GetObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
