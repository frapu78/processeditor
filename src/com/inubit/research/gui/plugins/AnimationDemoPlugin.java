/**
 *
 * Process Editor - inubit Workbench Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins;

import com.inubit.research.animation.AnimationFacade.Type;
import com.inubit.research.gui.Workbench;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TextAnnotation;

/**
 *
 * @author fpu
 */
public class AnimationDemoPlugin extends WorkbenchPlugin implements Runnable {

    private Task f_task1;
    private Task f_task2;
    private Task f_task3;
    private SequenceFlow f_edge;
    private TextAnnotation f_text;
    private ProcessEditor f_pe;
    private Workbench wb;
    public AnimationDemoPlugin(Workbench workbench) {
        wb = workbench;
    }

    @Override
    public Component getMenuEntry() {
        JMenuItem menu = new JMenuItem("Animation demo");

        menu.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(new AnimationDemoPlugin(wb));
                t.start();
            }
        });


        return menu;
    }

    /**
     * showing edge transition
     * @throws InterruptedException
     */
    private void scenario6() throws InterruptedException {
        showText("It works with edges, too", 6000);
        f_pe.getAnimator().addProcessObject(f_task1, 1000, 0);
        f_pe.getAnimator().addProcessObject(f_task2, 1000, 0);
        f_pe.getAnimator().addProcessObject(f_edge, 1000, 0);

        SequenceFlow _newEdge = new SequenceFlow(f_task1, f_task2);
        _newEdge.addRoutingPoint(1, new Point(100, 350));
        _newEdge.addRoutingPoint(2, new Point(500, 350));
        f_pe.getAnimator().animateEdge(f_edge, _newEdge, 3000, 2000);

        _newEdge = new SequenceFlow(f_task1, f_task2); //creating a new container
        _newEdge.addRoutingPoint(1, new Point(200, 150));
        f_pe.getAnimator().animateEdge(f_edge, _newEdge, 3000, 6000);

        _newEdge = new SequenceFlow(f_task1, f_task2);//creating a new container
        _newEdge.addRoutingPoint(1, new Point(180, 20));
        _newEdge.addRoutingPoint(2, new Point(250, 200));
        _newEdge.addRoutingPoint(2, new Point(400, 300));
        f_pe.getAnimator().animateEdge(f_edge, _newEdge, 3000, 10000);

        f_pe.getAnimator().removeProcessObject(f_task1, 1000, 14000);
        f_pe.getAnimator().removeProcessObject(f_task2, 1000, 14000);
        f_pe.getAnimator().removeProcessObject(f_edge, 1000, 14000);

        Thread.sleep(15500);
        restoreObjects();

    }

    /**
     * @throws InterruptedException
     *
     */
    private void scenario5() throws InterruptedException {
        showText("... and even interrupt each other", 6000);
        f_pe.getAnimator().addProcessNode(f_task1, 1000, 0, Type.TYPE_FADE_IN);
        f_pe.getAnimator().animateNode(f_task1, f_task2, 3000, 2000);
        f_pe.getAnimator().animateNode(f_task1, f_task3, 3000, 3500); //starts in the middle of the first
        f_pe.getAnimator().removeProcessObject(f_task1, 1000, 7000);
        Thread.sleep(7500);
        restoreObjects();
    }

    /**
     * node animations can be combined!
     * @throws InterruptedException
     */
    private void scenario4() throws InterruptedException {
        showText("Animations can be combined...", 6000);
        f_pe.getAnimator().addProcessNode(f_task1, 1000, 0, Type.TYPE_FADE_IN);
        f_pe.getAnimator().animateNode(f_task1, f_task2, 3000, 2000);
        f_pe.getAnimator().animateNode(f_task1, f_task3, 3000, 5500);
        f_pe.getAnimator().removeProcessObject(f_task1, 1000, 9000);
        Thread.sleep(9500);
        restoreObjects();
    }

    private void restoreObjects() {
        f_task1 = new Task(100, 100, "Tester");
        f_task1.setAlpha(1.0f);
        f_task2 = new Task(500, 100, "Tester234");
        f_task2.setAlpha(1.0f);
        f_task2.setBackground(Color.RED);
        f_task3 = new Task(100, 200, "Tester234");
        f_task3.setAlpha(1.0f);
        f_edge = new SequenceFlow(f_task1, f_task2);
    }

    /**
     * changing to different nodes simultaneously
     * @throws InterruptedException
     */
    private void scenario3() throws InterruptedException {
        showText("Several nodes at the same time", 6000);
        //order is important here!
        //addProcessNode sets the alpha of f_task2 to 0.0
        //so f_task2 should not be used as a container after adding it, that
        //has to be done before!!!!
        f_pe.getAnimator().addProcessNode(f_task1, 1000, 0, Type.TYPE_FADE_IN);
        f_pe.getAnimator().animateNode(f_task1, f_task2, 3000, 2000);
        f_pe.getAnimator().addProcessNode(f_task2, 1000, 0, Type.TYPE_FADE_IN);
        f_pe.getAnimator().animateNode(f_task2, f_task3, 3000, 2500);
        f_pe.getAnimator().removeProcessObject(f_task1, 1000, 7000);
        f_pe.getAnimator().removeProcessObject(f_task2, 1000, 7000);
        Thread.sleep(8000);
        restoreObjects();
    }

    /**
     *changing the properties of a node
     * @throws InterruptedException
     */
    private void scenario2() throws InterruptedException {
        showText("Their properties can be changed", 6000);
        f_pe.getAnimator().addProcessNode(f_task1, 1000, 0, Type.TYPE_FADE_IN);
        f_task2.setSize(200, 80);
        f_task2.setAlpha(0.5f);
        f_pe.getAnimator().animateNode(f_task1, f_task2, 3000, 2000);
        f_pe.getAnimator().removeProcessObject(f_task1, 1000, 6000);
        Thread.sleep(7500);
        restoreObjects();
    }

    /**
     * fading in and out
     * @throws InterruptedException
     */
    private void scenario1() throws InterruptedException {
        showText("Objects can be faded in and out", 6000);
        //fading in
        f_pe.getAnimator().addProcessNode(f_task1, 3000, 1000, Type.TYPE_FADE_IN);
        //fading out with larger delay
        f_pe.getAnimator().removeProcessObject(f_task1, 3000, 5000);
        Thread.sleep(8500); //Thats how long everything will take
    }

    /**
     * @param string
     * @param i
     */
    private void showText(String string, int time) {
        if (time < 1000) {
            time = 1000;
        }
        System.out.println("Show text: " + string);
        f_text.setText(string);
        // Set back to visible!
        f_text.setAlpha(1.0f);
        f_pe.getAnimator().addProcessObject(f_text, 1000, 0);
        f_pe.getAnimator().removeProcessObject(f_text, 1000, time - 1000);

    }

    public void run() {

                try {

                    f_pe = wb.getSelectedProcessEditor();
                    //enabling animations
                    f_pe.setAnimationEnabled(true);

                    f_text = new TextAnnotation();
                    f_text.setPos(300, 20);
                    f_text.setSize(400, 30);
                    //setting up our test objects
                    restoreObjects();
                    // fading
                    scenario1();
                    //state transition
                    scenario2();
                    //double state transition
                    scenario3();
                    //compound state transition
                    //scenario4();
                    //interrupted compound state transition
                    //scenario5();
                    //edge transition
                    //scenario6();
                    //barchart animation
                    //scenario7();
                    //piechart animation
                    //scenario8();

                    showText("That's it!", 6000);
                    Thread.sleep(4000);
                    showText("Copyright inubit AG 2010", 3000);

                } catch (Exception ex) {
                }

    }
}
