/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib;

import java.io.*;
import java.lang.String;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import net.kotek.jdbm.DB;
import net.kotek.jdbm.DBMaker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.commands.FilterCommand;
import uk.ac.susx.mlcl.lib.collect.ForwardingBiMap;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.io.TSV;

/**
 *
 * @author hiam20
 */
public class Enumerators {

    private static final Log LOG = LogFactory.getLog(Enumerators.class);

    private Enumerators() {
    }

    public static <T> Enumerator<T> nullEnumerator() {
        return new Enumerator<T>() {

            private final String ERROR = "Null Enumerator should never be accessed.";

            @Override
            public int indexOf(T obj) {
                throw new UnsupportedOperationException(ERROR);
            }

            @Override
            public T valueOf(int id) {
                throw new UnsupportedOperationException(ERROR);
            }

            @Override
            public Iterator<Entry<Integer, T>> iterator() {
                throw new UnsupportedOperationException(ERROR);
            }

        };
    }

    static Map<File, Entry<DB, Enumerator<String>>> DBs =
            new HashMap<File, Entry<DB, Enumerator<String>>>();

    public static synchronized Enumerator<String> newJDBCEnumerator(File file) {
        if (DBs.containsKey(file))
            return DBs.get(file).getValue();

        DB db = DBMaker.openFile(file.toString()).make();
        Map<Integer, String> forwards = db.<Integer, String>createHashMap("forwards");
        Map<String, Integer> backwards = db.<String, Integer>createHashMap("backwards");

        ForwardingBiMap<Integer, String> map = ForwardingBiMap.<Integer, String>create(
                forwards, backwards);

        Enumerator<String> instance;
        instance = new BiMapEnumerator<String>(map, new AtomicInteger(0));
        instance.indexOf(FilterCommand.FILTERED_STRING);

        DBs.put(file, new HashMap.SimpleEntry<DB, Enumerator<String>>(db, instance));

        assert instance.indexOf(FilterCommand.FILTERED_STRING) == 0;
        return instance;
    }

    public static synchronized Enumerator<String> openJDBCEnumerator(File file) {
        if (DBs.containsKey(file))
            return DBs.get(file).getValue();

        DB db = DBMaker.openFile(file.toString()).make();
        Map<Integer, String> forwards = db.<Integer, String>getHashMap("forwards");
        Map<String, Integer> backwards = db.<String, Integer>getHashMap("backwards");

        ForwardingBiMap<Integer, String> map = ForwardingBiMap.<Integer, String>create(
                forwards, backwards);

        Enumerator<String> instance;
        instance = new BiMapEnumerator<String>(map, new AtomicInteger(0));
        instance.indexOf(FilterCommand.FILTERED_STRING);

        DBs.put(file, new HashMap.SimpleEntry<DB, Enumerator<String>>(db, instance));

        assert instance.indexOf(FilterCommand.FILTERED_STRING) == 0;
        return instance;
    }

    public static synchronized void saveJDBCEnumerator(
            Enumerator<String> strEnum, File file) throws IOException {
        LOG.info("Saving string index: " + file);
        if (!DBs.containsKey(file))
            throw new IllegalStateException();

        DBs.get(file).getKey().commit();
    }

    public static synchronized void closeJDBCEnumerator(
            Enumerator<String> strEnum, File file) throws IOException {
        saveJDBCEnumerator(strEnum, file);
        LOG.info("Closing string index: " + file);
        if (!DBs.containsKey(file))
            throw new IllegalStateException();
        DBs.get(file).getKey().close();
        DBs.remove(file);
    }
//
//    @SuppressWarnings("unchecked")
//    public static Enumerator<String> loadStringEnumerator(File file) throws IOException {
//
//        LOG.info("Loading string index: " + file);
//
//        AtomicInteger nextId = new AtomicInteger(0);
//
//        ForwardingBiMap<Integer, String> map = ForwardingBiMap.<Integer, String>create(
//                new HashMap<Integer, String>(),
//                new HashMap<String, Integer>());
//
//        TSV.Source in = new TSV.Source(file, Files.DEFAULT_CHARSET);
//        while (in.canRead()) {
//            int id = in.readInt();
//            String s = in.readString();
//            in.endOfRecord();
//
//            if (nextId.get() <= id)
//                nextId.set(id + 1);
//
//            map.put(id, s);
//        }
//
//        Enumerator<String> instance;
//        instance = new BiMapEnumerator<String>(map, nextId);
//
//        instance = new BackoffEnumerator<String>(100, instance);
//
//        assert instance.indexOf(FilterCommand.FILTERED_STRING) == 0;
//        return instance;
//
//    }

//    public static Enumerator<String> newMemoryStringEnumerator() {
//
//        ForwardingBiMap<Integer, String> map = ForwardingBiMap.<Integer, String>create(
//                new HashMap<Integer, String>(),
//                new HashMap<String, Integer>());
//
//
//        Enumerator<String> instance;
//
//        instance = new BiMapEnumerator<String>(map, new AtomicInteger(0));
//
//        instance.indexOf(FilterCommand.FILTERED_STRING);
//
////        instance = new BackoffEnumerator<String>(100, instance);
//
//        assert instance.indexOf(FilterCommand.FILTERED_STRING) == 0;
//        return instance;
//    }
//
//    @SuppressWarnings("unchecked")
//    public static Enumerator<String> openMemoryBasedStringEnumerator(File file) throws IOException {
//
//        LOG.info("Loading string index: " + file);
//
//        AtomicInteger nextId = new AtomicInteger(0);
//
//        ForwardingBiMap<Integer, String> map = ForwardingBiMap.<Integer, String>create(
//                new HashMap<Integer, String>(),
//                new HashMap<String, Integer>());
//
//        TSV.Source in = new TSV.Source(file, Files.DEFAULT_CHARSET);
//        while (in.canRead()) {
//            int id = in.readInt();
//            String s = in.readString();
//            in.endOfRecord();
//
//            if (nextId.get() <= id)
//                nextId.set(id + 1);
//
//            map.put(id, s);
//        }
//
//        Enumerator<String> instance;
//        instance = new BiMapEnumerator<String>(map, nextId);
//
//        assert instance.indexOf(FilterCommand.FILTERED_STRING) == 0;
//        return instance;
//
//    }
//
//    public static void saveMemoryBasedStringEnumerator(Enumerator<String> strEnum,
//                                                       File file) throws IOException {
//        LOG.info("Saving string index: " + file);
//
//        TSV.Sink out = new TSV.Sink(file, Files.DEFAULT_CHARSET);
//
//        for (Entry<Integer, String> e : strEnum) {
//            out.writeInt(e.getKey());
//            out.writeString(e.getValue());
//            out.endOfRecord();
//        }
//        out.flush();
//        out.close();
//    }
//
//    public static void closeMemoryBasedStringEnumerator(Enumerator<String> strEnum,
//                                                        File file) throws IOException {
//        // not a sausage
//    }

}
