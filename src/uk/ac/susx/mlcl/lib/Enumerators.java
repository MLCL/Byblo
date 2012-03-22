/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib;

import java.io.*;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.lib.io.Files;

/**
 *
 * @author hiam20
 */
public class Enumerators {

    private Enumerators() {
    }

    public static Enumerator<String> newSimpleStringEnumerator() {
        return new SimpleEnumerator<String>();
    }

    public static void saveStringEnumerator(Enumerator<String> strEnum, File file) throws IOException {
        Files.writeSerialized(strEnum, file, true);
    }

    @SuppressWarnings("unchecked")
    public static Enumerator<String> loadStringEnumerator(File file) throws IOException, ClassNotFoundException {
        return (Enumerator<String>)Files.readSerialized(file, true);
    }

}
