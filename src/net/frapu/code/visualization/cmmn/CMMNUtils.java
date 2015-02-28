package net.frapu.code.visualization.cmmn;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;
import net.frapu.code.visualization.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.round;

/**
 * @author Stephan
 * @version 14.10.2014
 */
public class CMMNUtils extends ProcessUtils {
    private List layouters = null;

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        return new Association(source, target);
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        if (layouters == null) {
            layouters = new ArrayList<ProcessLayouter>();
            layouters.add(new SugiyamaLayoutAlgorithm(true, Configuration.getProperties()));
            layouters.add(new SugiyamaLayoutAlgorithm(false,Configuration.getProperties()));
        }
        return layouters;
    }

    @Override
    public List<Class<? extends ProcessNode>> getNextNodesRecommendation(ProcessModel model, ProcessNode node) {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();

        result.add(Task.class);
        result.add(Milestone.class);
        result.add(CaseFileItem.class);
        result.add(Stage.class);
        result.add(EventListener.class);

        return result;
    }

    protected static void drawAutoCompleteMarker(Graphics2D g2, int x, int y, int iconSize) {
        int spacing = 2;
        Rectangle2D marker = new Rectangle2D.Double();

        marker.setFrame(x + spacing - iconSize,
                y + spacing - iconSize,
                2 * (iconSize - spacing),
                2 * (iconSize - spacing));
        g2.setPaint(Color.BLACK);
        g2.fill(marker);
    }

    protected static void drawRepetitionMarker(Graphics2D g2, int x, int y, int iconSize) {
       Font font = new Font("Serif", Font.BOLD, 2 * iconSize);
       g2.setFont(font);
       g2.drawString("#", x - iconSize + 2, y + iconSize - 2);
    }

    protected static void drawRequiredMarker(Graphics2D g2, int x, int y, int iconSize) {
        Font font = new Font("Serif", Font.BOLD, 2 * iconSize);
        g2.setFont(font);
        g2.drawString("!", x - iconSize + 2, y + iconSize - 2);
    }

    protected static void drawManualActivationMarker(Graphics2D g2, int x, int y, int iconSize) {
        int spacing = 2;
        Polygon marker = new Polygon();

        marker.addPoint(x + spacing - iconSize, y + spacing - iconSize);
        marker.addPoint(x + spacing - iconSize, y + iconSize - spacing);
        marker.addPoint(x + iconSize - spacing, y);

        g2.setPaint(Color.BLACK);
        g2.setStroke(defaultStroke);
        g2.draw(marker);
    }

    protected static void drawPlanningTable(Graphics2D g2, int x, int y, int size, boolean visualized) {

        Rectangle2D frame = new Rectangle2D.Double();
        frame.setFrame(x - (int)round(0.75 * size), y - (int)round(size / 2), size * 1.5, size);

        // Draw horizontal lines
        /*g2.drawLine(x - (int)round(1.5 * size), y, x + (int)round(1.5 * size), y);
        g2.drawLine(x - (int)round(1.5 * size), y + (int)round(0.5 * size), x + (int)round(1.5 * size), y + (int)round(0.5 * size));
        g2.drawLine(x - (int) round(1.5 * size), y - (int) round(0.5 * size), x + (int) round(1.5 * size), y - (int) round(0.5 * size));

        //Draw vertical lines
        g2.drawLine(x - (int)round(1.5 * size), y - size, x - (int)round(1.5 * size), y + size);
        g2.drawLine(x - size, y - size, x - size, y + size);
        g2.drawLine(x - (int)round(0.5 * size), y - size, x - (int)round(0.5 * size), y + size);*/

        g2.setPaint(Color.WHITE);
        g2.fill(frame);
        g2.setPaint(Color.BLACK);
        g2.setStroke(defaultStroke);
        g2.draw(frame);

        g2.drawLine(x - (int)round(0.75 * size), y, x + (int)round(0.75 * size), y);
        g2.drawLine(x - (int)round(0.25 * size), y - (int)round(0.5 * size), x - (int)round(0.25 * size), y + (int)round(0.5 * size));
        g2.drawLine(x + (int)round(0.25 * size), y - (int)round(0.5 * size), x + (int)round(0.25 * size), y + (int)round(0.5 * size));
        g2.drawLine((int) (x - (0.183 * size)), y + (size / 4), x + (int)round(0.183 * size), y + (size / 4));
        if (!visualized) {
            g2.drawLine(x, y + (int)round(0.033 * size), x, y + (int)round(0.4 * size));
        }
    }
}
