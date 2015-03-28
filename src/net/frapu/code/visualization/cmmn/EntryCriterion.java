/**
 * Process Editor - CMMN Package
 *
 * (C) 2014 the authors
 */
package net.frapu.code.visualization.cmmn;

import java.awt.*;

/**
 * @author Stephan
 * @version 14.10.2014
 */
public class EntryCriterion extends Criterion {

    public EntryCriterion() {
        super();
        initializeProperties();
    }

    public EntryCriterion(int x, int y, String label) {
        super(x, y, label);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_BACKGROUND, "" + Color.WHITE.getRGB());
        setProperty(PROP_SOURCE_NODE, "");
    }
}
