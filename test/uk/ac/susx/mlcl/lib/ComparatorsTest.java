/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib;

import java.util.Comparator;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author hiam20
 */
public class ComparatorsTest {
    
    public ComparatorsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

//    /**
//     * Test of reverse method, of class Comparators.
//     */
//    @Test
//    public void testReverse() {
//        System.out.println("reverse");
//        Comparator<T> comp = null;
//        Comparator expResult = null;
//        Comparator result = Comparators.reverse(comp);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of fallback method, of class Comparators.
//     */
//    @Test
//    public void testFallback() {
//        System.out.println("fallback");
//        Comparator<T> a = null;
//        Comparator<T> b = null;
//        Comparator expResult = null;
//        Comparator result = Comparators.fallback(a, b);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of naturalOrder method, of class Comparators.
//     */
//    @Test
//    public void testNaturalOrder() {
//        System.out.println("naturalOrder");
//        Comparator expResult = null;
//        Comparator result = Comparators.naturalOrder();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of naturalOrderIfPossible method, of class Comparators.
//     */
//    @Test
//    public void testNaturalOrderIfPossible() {
//        System.out.println("naturalOrderIfPossible");
//        Comparator expResult = null;
//        Comparator result = Comparators.naturalOrderIfPossible();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
    
    @Test(expected=StringIndexOutOfBoundsException.class)
    public void testNeighbourhoodComparator_OnEmptyString() {
        
        Comparators.NeighbourComparator instance = new Comparators.NeighbourComparator();
        String a = "xxxx";
        String b = "xxxx";
        
        instance.compare(a, b);
        
        
    }
}
