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

import java.awt.Font;
import java.awt.Graphics2D;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class ORConnector extends Connector {

    @Override
    protected void drawMarker(Graphics2D g2) {
        super.drawMarker(g2);

        g2.setFont(new Font("Arial Narrow", Font.BOLD, 14));

        ProcessUtils.drawText(g2, getPos().x, getPos().y-2,
                getSize().width+50, "OR",
                ProcessUtils.Orientation.CENTER);

    }

}
