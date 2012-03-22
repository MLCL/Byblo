/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib;

import com.google.common.base.Function;
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
        return (Enumerator<String>) Files.readSerialized(file, true);
    }

    public static <T> Function<T, Integer> encoder(final Enumerator<T> en) {
        return new Function<T, Integer>() {

            @Override
            public Integer apply(T val) {
                return en.index(val);
            }

        };
    }



    public static <T> Function<Integer, T> decoder(final Enumerator<T> en) {
        return new Function<Integer, T>() {

            @Override
            public T apply(Integer idx) {
                return en.value(idx);
            }

        };
    }

}
