/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.JCommander;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author hiam20
 */
public class DeleteCommandTest {

    @Test(timeout = 1000)
    public void testCLI() throws IOException, Exception {
        System.out.println("Testing command line usage.");
        File tmp = File.createTempFile(getClass().getName(), "");
        DeleteCommand instance = new DeleteCommand();
        String[] args = {"-f", tmp.toString()};
        JCommander jc = new JCommander();
        jc.addObject(instance);
        jc.parse(args);
        
        assertTrue(tmp.exists());
        assertEquals(tmp, instance.getFile());
        
        instance.runCommand();
        
        
        assertFalse(tmp.exists());
        
        jc.usage();
    }

}
