/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author jos
 */
public class ImagePanel extends JPanel {

    BufferedImage image;

    ImagePanel(BufferedImage image) {
        this.image = image;
        // or load it in this class
        setLayout(null);
    }

    ImagePanel() {
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        Dimension size = new Dimension(image.getWidth(null), image.getHeight(null));
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = 10;
        int y = 10;
        g.drawImage(image, x, y, this);
    }
}
