/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server;

import com.inubit.research.server.persistence.FileSystemConnector;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fel
 */
public class ImageStore {

    public static final int DIR_SOURCE = 0;
    public static final int DIR_TARGET = 1;
    private static final int PREVIEW_SIZE = 32;

    private static Map<String, BufferedImage> recentImages =
               new HashMap<String, BufferedImage>();

    private static void addRecentImage(String id, BufferedImage image) {
        recentImages.put(id, image);
    }

    private static BufferedImage getRecentImage(String id) {
        return recentImages.get(id);
    }

    /**
     * Derive a new ID for the changed image of the given node
     * @param currentModel the current model
     * @param currentNode the current node
     * @param img the node's image
     * @return the new ID
     */
    public static String add(ProcessModel currentModel, ProcessNode currentNode, BufferedImage img) {
        String modelId = currentModel.getId();
        String newId = "0" + currentNode.getId();
        String fullID = modelId + "_" + newId;

        //create new image ID by adding leading zeros
        while (recentImages.containsKey(fullID)) {
            recentImages.put(fullID, null);
            newId = "0" + newId;
            fullID = modelId + "_" + newId;
        }
        addRecentImage(fullID, img);
        addRecentImage(modelId + "_" + currentNode.getId(), img);
        return newId;
    }

     /**
     * Get an image for the a single node in a given model. If it is found in the cache
     * use the cache version, else create a new one
     *
     * @param nodeId ID of the requested node
     * @param currentModel the current model
     *
     * @return the node's image
     * @throws Exception
     */
    public static BufferedImage getImage(String nodeId, ProcessModel model) throws Exception {
        BufferedImage img;
        String fullID = model.getId() + "_" + nodeId;

        //get image from cache or create a new one
        if (recentImages.containsKey(fullID)) {
            img = getRecentImage(fullID);
        } else {
            ProcessNode node = model.getNodeById(nodeId);
            img = ProcessEditorServerUtils.createNodeImage(node);
            addRecentImage(fullID, img);
        }
        return img;
    }

    public static BufferedImage getDummyNodeImage(String className, boolean preview) throws Exception{
        BufferedImage img;
        String key = className + "_" + preview;
        if (recentImages.containsKey(key)) {
            img = recentImages.get(key);
        } else {
            if (preview) {
                String fileName = FileSystemConnector.loadServerConfig().getNodeImageFileName(Class.forName(className));

                if ( fileName != null && ( new File( fileName ).exists() )) {
                    BufferedImage _buff = ImageIO.read( new File( fileName ));
                    img = ProcessUtils.toBufferedImage(_buff.getScaledInstance(PREVIEW_SIZE, PREVIEW_SIZE, Image.SCALE_SMOOTH));
                } else
                    img = ProcessUtils.toBufferedImage(ProcessUtils.createPreviewImage(Class.forName(className), PREVIEW_SIZE));
            }
            else {
                ProcessNode currentNode = (ProcessNode) Class.forName(className).newInstance();
                img = ProcessEditorServerUtils.createNodeImage(currentNode);
            }

            recentImages.put(key, img);
        }

        return img;
    }

    public static BufferedImage getEdgeShapeImage(String key) {
        //@TODO think about creating a new shape if none is found
        return getRecentImage(key);
    }

    public static String addEdgeShapeImage(ProcessEdge edge, int direction) {
        String key = createEdgeKey(edge, direction);

        if ((direction == DIR_SOURCE && edge.getSourceShape() == null) ||
            (direction == DIR_TARGET && edge.getTargetShape() == null))
                return null;

        if (!recentImages.containsKey(key)) {
            //there was no image created yet, so create one
            boolean outline;
            Shape shape;

            if (direction == DIR_SOURCE) {
                outline = edge.isOutlineSourceArrow();
                shape = edge.getSourceShape();
            } else if (direction == DIR_TARGET) {
                outline = edge.isOutlineTargetArrow();
                shape = edge.getTargetShape();
            } else {
                return null;
            }

            if (shape == null) return null;

            int completeWidth = shape.getBounds().width;
            if ( shape.getBounds().x > 0)
                completeWidth += shape.getBounds().x;
            int completeHeight = shape.getBounds().height;
            if ( shape.getBounds().y > 0)
                completeHeight += shape.getBounds().y;

            BufferedImage img = new BufferedImage(completeWidth + 2,
                                                  completeHeight + 2,
                                                  BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setBackground(new Color(255,255,0,0));
            g.clearRect(0, 0, completeWidth + 2, completeHeight + 2);
            
            if (direction == DIR_TARGET)
                g.translate(completeWidth + 1,completeHeight / 2 + 1);
            else
                g.translate(1, completeHeight / 2 + 1);

            g.setColor(edge.getColor());

            if (outline)
                g.draw(shape);
            else
                g.fill(shape);

            addRecentImage(key, img);
        }

        return key;
    }

    private static String createEdgeKey(ProcessEdge edge, int direction) {
        StringBuilder key = new StringBuilder(100);
        key.append(edge.getClass().getName());

        if (direction == DIR_SOURCE) {
            key.append("_source_" + edge.isOutlineSourceArrow());
        } else if (direction == DIR_TARGET) {
            key.append("_target_" + edge.isOutlineTargetArrow());
        } else {
            return null;
        }

        key.append("_color_" + edge.getColor().getRGB());
        key.append("_type_" + edge.getProperty("sequence_type"));

        return key.toString();
    }

    public static void removeAllRelatedImages(String id) {
        List<String> ids = new LinkedList<String>();
        for (String key : recentImages.keySet()) {
            if (key.startsWith(id)) {
                ids.add(key);
            }
        }

        for (String key : ids) {
            recentImages.remove(key);
        }
    }
}
