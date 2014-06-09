/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.frapu.code.visualization;

import net.frapu.code.visualization.domainModel.DomainClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fel
 */
public class DomainClassTest {

    private DomainClass dc;
    public DomainClassTest() {
    }

    @Before
    public void setUp() {
        dc = new DomainClass("MyClass");
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    public void testAddAttribute() {
        assertTrue( 0 == dc.getAttributesByIDs().values().size() );
        dc.addAttribute("attr1");
        assertTrue( 1 == dc.getAttributesByIDs().values().size() );
        dc.addAttribute("attr2");
        assertTrue( 2 == dc.getAttributesByIDs().values().size() );
    }

}