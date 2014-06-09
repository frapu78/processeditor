/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.radial;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import net.frapu.code.visualization.ProcessHelper;

public class RadialDistanceRing extends ProcessHelper {

    private Point f_center;
    private int f_distance;

    public RadialDistanceRing(Point center, int distance) {
        f_center = center;
        f_distance = distance;
        this.setAlpha(0.5f);
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.drawArc(f_center.x - f_distance,
                f_center.y - f_distance, 2 * f_distance, 2 * f_distance, 0, 360);
    }
}
