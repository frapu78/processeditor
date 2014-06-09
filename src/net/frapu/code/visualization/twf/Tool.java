/**
 *
 * Process Editor - TWF Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.twf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 *
 * @author fpu
 */
public class Tool extends Cluster {

    //public final static String PROP_DOCKER_LEFT = "left_docker";
    public final static String PROP_DOCKER_RIGHT = "right_docker";
    public final static String PROP_ERROR_CONNECTION1 = "error_connection1";
    public final static String PROP_ERROR_CONNECTION2 = "error_connection2";

    private ToolDocker f_leftDocker; //is always there
    private ToolDocker f_rightDocker;
    
    private ToolErrorConnector f_errorCon1;
    private ToolErrorConnector f_errorCon2;
    
    
    
    public Tool() {
        super();
        initializeProperties();    
    }
    
    @Override
    public void addContext(ProcessModel model) {
    	super.addContext(model);
    	f_leftDocker = new ToolDocker(this,true); //is always there
    	model.addNode(f_leftDocker);
    	if(getProperty(PROP_DOCKER_RIGHT).equals(TRUE)) {
	    	f_rightDocker = new ToolDocker(this,false);
	        model.addNode(f_rightDocker);
    	}
    	if(getProperty(PROP_ERROR_CONNECTION1).equals(TRUE)) {
        f_errorCon1 = new ToolErrorConnector(this,0);
        model.addNode(f_errorCon1);
    	}
    	if(getProperty(PROP_ERROR_CONNECTION2).equals(TRUE)) {
	        f_errorCon2 = new ToolErrorConnector(this,1);
	        model.addNode(f_errorCon2);
    	}
    }
    
    @Override
    public void removeContext(ProcessModel model) {
    	super.removeContext(model);
    	model.removeNode(f_leftDocker);
        model.removeNode(f_rightDocker);
        model.removeNode(f_errorCon1);
        model.removeNode(f_errorCon2);
    }

    protected void initializeProperties() {
        setSize(200,150);
        setText("Tool");
        setProperty(PROP_DOCKER_RIGHT, TRUE);
        setPropertyEditor(PROP_DOCKER_RIGHT, new BooleanPropertyEditor());
        setProperty(PROP_ERROR_CONNECTION1, TRUE);
        setPropertyEditor(PROP_ERROR_CONNECTION1, new BooleanPropertyEditor());
        setProperty(PROP_ERROR_CONNECTION2, TRUE);
        setPropertyEditor(PROP_ERROR_CONNECTION2, new BooleanPropertyEditor());

    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(ProcessUtils.defaultStroke);
        Shape outline = getOutlineShape();

        g2.setPaint(new Color(250,250,250));
        g2.fill(outline);
         
        //drawing the dark grey background for the errorConnections
        if(f_errorCon1 != null || f_errorCon2 != null) {
	        g2.setPaint(new Color(230,230,230));
			Point _topLeftAreaPos = new Point(getPos().x-getSize().width/2, 
					getPos().y+getSize().height/2 - ToolErrorConnector.AREA_HEIGHT);
		    g2.fillRect(_topLeftAreaPos.x,_topLeftAreaPos.y,getSize().width, ToolErrorConnector.AREA_HEIGHT);
        }        
        g2.setPaint(Color.BLACK);
        g2.draw(outline);
        
        g2.setFont(TWFUtils.defaultFont);   
        ProcessUtils.drawText(g2, getPos().x, getPos().y - (getSize().height/2),
                getSize().width, getText(),
                ProcessUtils.Orientation.TOP);
    }

    @Override
    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
    }

    @Override
    public Set<Point> getDefaultConnectionPoints() {
        HashSet<Point> cp = new HashSet<Point>();
        return cp;
    }
    
    public ToolDocker getLeftDocker() {
    	return f_leftDocker;
    }
    
    public ToolDocker getRightDocker() {
    	return f_rightDocker;
    }
    
    public List<ToolErrorConnector> getErrorConnectors() {
    	List<ToolErrorConnector> _result = new ArrayList<ToolErrorConnector>();
    	if(f_errorCon1 != null) _result.add(f_errorCon1);
    	if(f_errorCon2 != null) _result.add(f_errorCon2);
    	return _result;
    }

    @Override
    public String toString() {
        return getText() + " (Tool)";
    }
    
    @Override
    public void setProperty(String key, String value) {
    	if(key.equals(PROP_DOCKER_RIGHT)) {
    		if(value.equals(TRUE)) {
    			if(f_rightDocker == null) {
    				f_rightDocker = new ToolDocker(this,false);
	    			for(ProcessModel model:getContexts()) {
	    				model.addNode(f_rightDocker);
	    			}
	    			//addProcessNode(f_rightDocker);
    			}
    		}else {
    			removeElement(f_rightDocker);
    			f_rightDocker = null;
    		}
    	}else if(key.equals(PROP_ERROR_CONNECTION1)) {
    		f_errorCon1 = checkErrorConnector(value,f_errorCon1,0);
    	}else if(key.equals(PROP_ERROR_CONNECTION2)) {
    		f_errorCon2 = checkErrorConnector(value,f_errorCon2,1);
    	}
    	super.setProperty(key, value);    	
    }

	private void removeElement(ProcessNode node) {
		//removeProcessNode(node);
		for(ProcessModel model:getContexts()) {
			model.removeNode(node);
		}		
	}

	private ToolErrorConnector checkErrorConnector(String value, ToolErrorConnector con, int number) {
		if(value.equals(TRUE)) {
			if(con == null) {
				ToolErrorConnector _con = new ToolErrorConnector(this,number);
				for(ProcessModel model:getContexts()) {
					model.addNode(_con);
				}
				//addProcessNode(_con);
				return _con;
			}
		}else {
			for(ProcessModel model:getContexts()) {
				model.removeNode(con);
			}
			//removeProcessNode(con);
			return null;
		}
		return con;
	}

	/**
	 * @param toolErrorConnector
	 * @param number
	 */
	public void setErrorConnector(ToolErrorConnector tec,int number) {
		if(number == 0) {
    		removeElement(f_errorCon1);
    		f_errorCon1 = tec;
    		//addProcessNode(f_errorCon1);
    	}else{
    		removeElement(f_errorCon2);
    		f_errorCon2 = tec;
    		//addProcessNode(f_errorCon2);
    	}
	}
	
	/**
	 * @param toolErrorConnector
	 * @param number
	 */
	public void setToolDocker(ToolDocker docker,
			boolean left) {
		if(left) {
    		removeElement(f_leftDocker);
    		f_leftDocker = docker;
    		//addProcessNode(f_leftDocker);
    	}else{
    		removeElement(f_rightDocker);
    		f_rightDocker = docker;
    		//addProcessNode(f_rightDocker);
    	}
	}
    
}
