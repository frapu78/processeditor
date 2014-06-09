/**
 *
 * Process Editor - BPMN Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 *
 * @author fpu
 */
public class UserArtifact extends Artifact {

	public static final String PROP_IMAGE_LOCATION = "image";

    private ImageIcon img;

    public UserArtifact() {
        super();
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_IMAGE_LOCATION, "");
        setSize(80,80);
    }

    @Override
    public void setProperty(String key, String value) {
        super.setProperty(key, value);
        if (key.equals(PROP_IMAGE_LOCATION) ||
                key.equals(PROP_WIDTH) || key.equals(PROP_HEIGHT)) {
            // Try to load image
            img = null;
            String file = getProperty(PROP_IMAGE_LOCATION);
            if (getProperty(PROP_IMAGE_LOCATION) == null) {
                return;
            }
            try {                                
                if (file.startsWith("http")) {
                    URI uri = new URI(file);
                    img = new ImageIcon(uri.toURL());
                } else {
                    File f = new File(file);
                    if (f.exists()) {
                        img = new ImageIcon(file);
                    } else {
                    	if(file.length() > 0) {
	                    	URL _url = getClass().getResource(file);
	                    	if(_url != null) {
	                    		img = new ImageIcon(_url);
	                    	}else {
	                    		setDefaultImage();
	                    	}
                    	}else {
                    		setDefaultImage();
                    	}
                    }
                }
                img = new ImageIcon(img.getImage().getScaledInstance(getSize().width, getSize().height, Image.SCALE_SMOOTH));
            } catch (Exception e) {
            	setDefaultImage();
                img = new ImageIcon(img.getImage().getScaledInstance(getSize().width, getSize().height, Image.SCALE_SMOOTH));
            }
        }
    }

	public void setDefaultImage() {
		img = new ImageIcon(getClass().getResource("/_logo/logo_32.png"));
	}

    @Override
    public Rectangle getBoundingBox() {
        // Get bounds of text
        BufferedImage dummyImg = new BufferedImage(100, 50, BufferedImage.BITMASK);
        Graphics2D g2 = dummyImg.createGraphics();
        g2.setFont(BPMNUtils.defaultFont);
        Rectangle gfxBounds = super.getBoundingBox();
        Rectangle textBounds = new Rectangle(gfxBounds);
        if (getText() != null) {
           String text = getText();
           textBounds = BPMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2),
                   getSize().width + 100, text, BPMNUtils.Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        //Shape outline = getOutlineShape();

        //g2.setPaint(getBackground());
        //g2.fill(outline);

        if (img != null) img.paintIcon(null, g2, getPos().x-getSize().width/2,
                getPos().y-getSize().height/2);

        g2.setPaint(Color.GRAY);
        g2.setFont(BPMNUtils.defaultFont);
        BPMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2), getSize().width + 100, getText(), BPMNUtils.Orientation.TOP);

    }

    @Override
    protected Shape getOutlineShape() {
        return new Rectangle2D.Double(getPos().x-getSize().width/2,
                getPos().y-getSize().height/2,
                getSize().width,
                getSize().height);
    }

}
