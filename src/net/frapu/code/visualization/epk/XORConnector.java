/**
 *
 * Process Editor - EPK Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.epk;

import java.awt.Color;
import java.awt.Graphics2D;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class XORConnector extends Connector {

    @Override
    protected void drawMarker(Graphics2D g2) {
        super.drawMarker(g2);

        g2.setPaint(Color.BLACK);
        g2.setStroke(ProcessUtils.defaultStroke);

        g2.drawLine(getPos().x-getSize().width/4, getPos().y-getSize().height/4,
                getPos().x+getSize().width/4, getPos().y+getSize().height/4);

        g2.drawLine(getPos().x+getSize().width/4, getPos().y-getSize().height/4,
                getPos().x-getSize().width/4, getPos().y+getSize().height/4);

    }

}
