/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server.merger.animator;

import com.inubit.research.server.merger.ModelComparator;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.converter.ProcessEditorImporter;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.bpmn.BPMNEditor;
import net.frapu.code.visualization.bpmn.BPMNModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author uha
 */
public class ProcessMergeAnimatorTest {

   ProcessMergeAnimator testAnimator;
   ProcessEditor editor;

    public ProcessMergeAnimatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {




    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {

        editor = new BPMNEditor(new BPMNModel());
        editor.setAnimationEnabled(true);
        testAnimator = editor.getMergeAnimator();



    }

    @After
    public void tearDown() {
    }

    private ProcessModel load(String filename) {
        try {
            ProcessEditorImporter importer = new ProcessEditorImporter();
            File file = new File("models\\" + filename );
            return importer.parseSource(file).get(0);
        } catch (Exception ex) {
            Logger.getLogger(ProcessMergeAnimatorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    /**
     * Test of animateModelTransition method, of class ProcessMergeAnimator.
     */
    @Test
    public void testCreationWimmelbild() {
       testModelCreation("Layout-Tests\\BPMN - reduced Wimmelbild.model");
        
    }



    private void testModelCreation(String filename) {
        testAnimationResult(filename);
        testAnimation(filename);
    }

    private void testAnimation(String filename) {
        final ProcessModel newModel = load(filename);
        testAnimator.animateModelTransitionWithoutResetingModel(newModel.clone(),false, false);
        final ModelComparator comp = new ModelComparator();
        testAnimator.getAnimationQueue().queue(new AnimationSequence(editor) {
            @Override
            public void run() {
                assertTrue(comp.modelEquals(newModel, editor.getModel()));
            }
        });

    }

    private void testAnimationResult(String filename) {
        System.out.println("animateModelTransition");
        final ProcessModel newModel = load(filename);
        testAnimator.animateModelTransition(newModel.clone());
        final ModelComparator comp = new ModelComparator();
        testAnimator.getAnimationQueue().queue(new AnimationSequence(editor) {

            @Override
            public void run() {
                assertTrue(comp.modelEquals(newModel, editor.getModel()));
            }
        });
    }






}