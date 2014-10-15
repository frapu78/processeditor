package net.frapu.code.visualization.cmmn;

import java.awt.*;

/**
 * @author Stephan
 * @version 14.10.2014
 */
public class ExitCriterion extends Criterion {

    public ExitCriterion() {
        super();
        initializeProperties();
    }

    public ExitCriterion(int x, int y, String label) {
        super(x, y, label);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_BACKGROUND, "" + Color.BLACK.getRGB());
    }
}
