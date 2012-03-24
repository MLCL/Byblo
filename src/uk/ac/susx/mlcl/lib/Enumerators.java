/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib;

import com.google.common.base.Function;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author hiam20
 */
public class Enumerators {

    private Enumerators() {
    }

    private static final class InstanceHolder {
        private static final Enumerator<String> onTheFlyStringEnumerator 
                = new SimpleEnumerator<String>();

        private InstanceHolder() {
        }
    }
    
    
    public static Enumerator<String> newDefaultStringEnumerator() {
//        return InstanceHolder.onTheFlyStringEnumerator;
        return new SimpleEnumerator<String>();
    } 
//    public static Enumerator<String> newSimpleStringEnumerator() {
//        return new SimpleEnumerator<String>();
//    }

    public static void saveStringEnumerator(Enumerator<String> strEnum, File file) throws IOException {
//        Files.writeSerialized(strEnum, file, true);
        
        TSVSink out = new TSVSink(file, Files.DEFAULT_CHARSET);
        
        for(Object2IntMap.Entry<String> e : strEnum) {
            out.writeInt(e.getIntValue());
            out.writeString(e.getKey());
            out.endOfRecord();
        }
        out.flush();
        out.close();
    }

    @SuppressWarnings("unchecked")
    public static Enumerator<String> loadStringEnumerator(File file) throws IOException, ClassNotFoundException {
        
        ObjectList<String> indexToObj = new ObjectArrayList<String>();
        Object2IntMap<String> objToIndex = new Object2IntOpenHashMap<String>();
        AtomicInteger nextId = new AtomicInteger(0);
        
        TSVSource in = new TSVSource(file, Files.DEFAULT_CHARSET);
        while(in.hasNext()) {
            int id = in.readInt();
            String s = in.readString();
            in.endOfRecord();
            
            if(nextId.get() <= id)
                nextId.set(id+1);
            
            objToIndex.put(s, id);
            while(id < indexToObj.size())
                indexToObj.add(null);
            
            indexToObj.set(id, s);
        }
        
        return new SimpleEnumerator<String>(indexToObj, objToIndex, nextId);
        
//        return (Enumerator<String>) Files.readSerialized(file, true);
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
