/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.helper;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JTextField;

import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessEditorListener;
import net.frapu.code.visualization.ProcessNode;


/**
 * This Class adds a JTextField to the ProcessEditor, which can be used
 * to change the Text of a given ProcessNode
 * @author ff
 *
 */
public class NameNodeOverlay implements KeyListener,MouseListener  {
	
	private boolean f_ignoreFirstClick = true;
	private long f_creationTime = System.currentTimeMillis();
	private String f_text;
	private JTextField f_textField;
	private ProcessEditor f_editor;
	private ProcessNode f_node;
	
	private int f_textfield_width = 120;
	private int f_textfield_height = 25;
	
	/**
	 * 
	 */
	public NameNodeOverlay(ProcessEditor parent,ProcessNode node) {
		f_editor = parent;
		f_editor.addMouseListener(this);
		f_node = node;
		if(f_node != null) {
			f_text = f_node.getText();
			f_textfield_width = f_node.getSize().width;
                        if (f_textfield_width<100) f_textfield_width = 100;
                        if (f_textfield_width>200) f_textfield_width = 200;
			
			f_textField = new JTextField(f_text);
			f_textField.addKeyListener(this);
								
			f_editor.setLayout(null);
			Point _loc = f_node.getPos();
			_loc.x-= f_textfield_width/2;
			_loc.y -= f_textfield_height/2;
			f_textField.setSize(f_textfield_width, f_textfield_height);
			f_textField.setLocation(_loc);
						
			f_editor.add(f_textField);			
			
			f_textField.requestFocus();
			f_editor.updateUI();
		}
	}
	
	public void close() {
		f_editor.removeMouseListener(this);
		f_editor.remove(f_textField);
		//notifying listeners
		for (ProcessEditorListener l : new ArrayList<ProcessEditorListener>(f_editor.getListeners())) {
            l.processNodeEditingFinished(f_node);
         }
		f_editor.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		//as the NameNodeOverlay is created in MousePressed
		//a mouseClicked event will be called afterwards
		//and would directly close it again.
		//Thus we have to ignore it
		if(f_ignoreFirstClick && (f_creationTime + 100 > System.currentTimeMillis())) {
			f_ignoreFirstClick = false;
		}else {
			if(arg0.getSource().equals(f_editor)) {
				//close and show old text again
				close();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			//close and display new text
			f_node.setText(f_textField.getText());
			close();
		}else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			//close and show old text again
			close();
		}		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	/**
	 * @return
	 */
	public JTextField getTextField() {
		return f_textField;
	}

}
