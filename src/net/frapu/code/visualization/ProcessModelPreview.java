/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import net.frapu.code.converter.ConverterHelper;

/**
 *
 * This class provides a preview of a ProcessModel
 *
 * @author frank
 */
public class ProcessModelPreview extends JLabel implements PropertyChangeListener {

    private static ProcessEditor editor = new ProcessEditor();

    /**
     *
     */
    private static final long serialVersionUID = -7179568465183222089L;
    private static final int PREFERRED_WIDTH = 200;
    private static final int PREFERRED_HEIGHT = 200;
    private static File lastFile = null;
    /** The curve size for the preview */
    final static int CURVESIZE = 10;

    public ProcessModelPreview(JFileChooser chooser) {
        setVerticalAlignment(JLabel.CENTER);
        setHorizontalAlignment(JLabel.CENTER);
        chooser.addPropertyChangeListener(this);
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        setBorder(new EtchedBorder());
        setText("No Preview");
    }

    @Override
    public void propertyChange(PropertyChangeEvent changeEvent) {
        String changeName =
                changeEvent.getPropertyName();
        if (changeName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            final File file = (File) changeEvent.getNewValue();
            lastFile = file;
            if (file != null) {
                if (!file.isDirectory()) {
                    // Try to load model
                    try {
                        setText("Loading preview");
                        new Thread() {

                            public void run() {
                                try {
                                    ProcessModel model = ConverterHelper.importModels(file).get(0);
                                    //if file to show preview for has not changed in the meantime...
                                    if (ProcessModelPreview.lastFile == file) {
                                        setText("");
                                        setIcon(createIcon(model, PREFERRED_WIDTH, PREFERRED_HEIGHT));
                                    }
                                } catch (Exception ex) {
                                    setText("Error while importing model");
                                }
                            }
                        }.start();
                    } catch (Exception ex) {
                        setIcon(null);
                        // Default icon
                        setText("No Preview");
                    }
                }
            }
        }

    }

    public static synchronized BufferedImage createStyledPreview(ProcessModel model, int width) {
        editor.setModel(model);
        editor.setEditable(false);
        editor.setDrawBackground(false);
        Dimension dim = editor.getPreferredSize();
        int size = dim.width;
        if (dim.height > size) {
            size = dim.height;
        }
        BufferedImage img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        // Clear background and paint model
        //g2.setPaint(Color.WHITE);
        //g2.fillRect(0, 0, img.getWidth(), img.getHeight());       

        try {
            editor.paintComponent(g2);
        } catch (Exception ex) {
            g2.setPaint(new Color(240,212,212));
            g2.fillRect(0, 0, img.getWidth(), img.getHeight());
            g2.drawString("n/a",30,30);
        }
        // Detect top-left corner
        int x1 = Integer.MAX_VALUE;
        int y1 = Integer.MAX_VALUE;
        for (ProcessNode n : model.getNodes()) {
            Rectangle2D box = n.getBoundingBox();
            if (box.getX() < x1) {
                x1 = (int) box.getX();
            }
            if (box.getY() < y1) {
                y1 = (int) box.getY();
            }
        }
        // Check for empty model
        if (x1 == Integer.MAX_VALUE) {
            x1 = 0;
        }
        if (y1 == Integer.MAX_VALUE) {
            y1 = 0;
        }
        // Check for negative values
        if (x1 < 0) {
            x1 = 0;
        }
        if (y1 < 0) {
            y1 = 0;
        }

        // Cut image to actual size
        img = img.getSubimage(x1, y1, img.getWidth() - x1, img.getHeight() - y1);

        Image imgScaled = null;
        if (img.getWidth() < img.getHeight()) {
            imgScaled = img.getScaledInstance(-1, width - 10, Image.SCALE_SMOOTH);
        } else {
            imgScaled = img.getScaledInstance(width - 10, -1, Image.SCALE_SMOOTH);
        }
        BufferedImage img2 = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
        g2 = (Graphics2D) img2.getGraphics();
        g2.setBackground(new Color(255,255,255,0));
        g2.clearRect(0,0,img2.getWidth() - 1, img2.getHeight() - 1);
        g2.setBackground(Color.WHITE);
        g2.setPaint(new Color(255,255,255,255));
        g2.fillRoundRect(0, 0, img2.getWidth() - 1, img2.getHeight() - 1, CURVESIZE, CURVESIZE);
        //g2.fillRect(0, 0, img2.getWidth(), img2.getHeight());

        if (img.getWidth() < img.getHeight()) {
            int realWidth = (int) ((double) img.getWidth() * ((double) (width - 10) / (double) img.getHeight()));
            g2.drawImage(imgScaled, (int) (width / 2) - (realWidth / 2), 5, null);
        } else {
            int realHight = (int) ((double) img.getHeight() * ((double) (width - 10) / (double) img.getWidth()));
            g2.drawImage(imgScaled, 5, (int) (width / 2) - (realHight / 2), null);
        }

        // Set stroke
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(ProcessUtils.thinStroke);

        // Draw Alpha overlay
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
        Path2D path = new Path2D.Double();

        path.moveTo(0.0, 0.0);
        path.lineTo(img2.getWidth(), img2.getHeight());
        path.lineTo(0.0, img2.getHeight());
        path.closePath();
        g2.setPaint(Color.GRAY);
        g2.fill(path);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Draw Outline
        g2.setPaint(Color.BLACK);
        g2.drawRoundRect(0, 0, img2.getWidth() - 1, img2.getHeight() - 1, CURVESIZE, CURVESIZE);

        g2.setPaint(new Color(255,255,255));
        g2.fillRect((int) (img2.getWidth() * 0.8), 0, (int) (img2.getWidth() * 0.2) + 1, (int) (img2.getHeight() * 0.2) + 1);

        // Clear upper right corner
        g2.setBackground(new Color(0,255,255,0));
        g2.clearRect((int)(img2.getWidth() * 0.8), 0, (int)(img2.getWidth() * 0.2), (int)(img2.getHeight() * 0.2));

        // Draw upper right corner
        path = new Path2D.Double();

        path.moveTo(img2.getWidth() * 0.8, 0.0);
        path.lineTo(img2.getWidth() * 0.8, img2.getHeight() * 0.2 - CURVESIZE);

        path.curveTo(
                img2.getWidth() * 0.8, img2.getHeight() * 0.2,
                img2.getWidth() * 0.8, img2.getHeight() * 0.2,
                img2.getWidth() * 0.8 + CURVESIZE, img2.getHeight() * 0.2);

        path.lineTo(img2.getWidth(), img2.getHeight() * 0.2);
        path.closePath();

        g2.setPaint(Color.WHITE);
        g2.fill(path);

        g2.setPaint(Color.BLACK);
        g2.draw(path);

        return img2;
    }

    public static synchronized ImageIcon createIcon(ProcessModel model, int width, int height) {
        editor = new ProcessEditor(model);
        editor.setEditable(false);
        BufferedImage img = new BufferedImage(editor.getPreferredSize().width, editor.getPreferredSize().height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        // Clear background and paint model
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
        editor.paintComponent(g2);

        ImageIcon icon = new ImageIcon(img);
        if (icon.getIconWidth() > width) {
            icon = new ImageIcon(icon.getImage().getScaledInstance(width, -1, Image.SCALE_SMOOTH));
            if (icon.getIconHeight() > height) {
                icon = new ImageIcon(icon.getImage().getScaledInstance(-1, height, Image.SCALE_SMOOTH));
            }
        }

        return icon;
    }
}
