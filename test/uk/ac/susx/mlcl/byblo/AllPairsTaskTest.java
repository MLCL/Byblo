/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import com.google.common.base.Objects.ToStringHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.susx.mlcl.ExitTrapper;
import static org.junit.Assert.*;

/**
 *
 * @author hiam20
 */
public class AllPairsTaskTest {

    public AllPairsTaskTest() {
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

    @Test
    public void testMainRun() throws Exception {
        ExitTrapper.enableExistTrapping();
        Main.main(new String[]{"allpairs",
                    "-i", "/Volumes/Local Scratch HD/Local Home/Documents/Data/lm-medline/r2/lm-800k.dtt-tb-tf-cb-cf-cw-55.entry-features.filtered.sample",
                    "-o", "/Volumes/Local Scratch HD/Local Home/Documents/Data/lm-medline/r2/lm-800k.dtt-tb-tf-cb-cf-cw-55.entry-features.sims",
                    "-C", "500"});
        ExitTrapper.disableExitTrapping();
    }
//    /**
//     * Test of runCommand method, of class AllPairsTask.
//     */
//    @Test
//    public void testRunCommand() throws Exception {
//        System.out.println("runCommand");
//        AllPairsTask instance = new AllPairsTask();
//        instance.runCommand();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toStringHelper method, of class AllPairsTask.
//     */
//    @Test
//    public void testToStringHelper() {
//        System.out.println("toStringHelper");
//        AllPairsTask instance = new AllPairsTask();
//        ToStringHelper expResult = null;
//        ToStringHelper result = instance.toStringHelper();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
