/*
 * Copyright (c) 2010-2012, University of Sussex
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *  
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 *  * Neither the name of the University of Sussex nor the names of its 
 *    contributors may be used to endorse or promote products derived from this 
 *    software without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.enumerators;

import com.google.common.collect.BiMap;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kotek.jdbm.DB;
import net.kotek.jdbm.DBMaker;
import uk.ac.susx.mlcl.byblo.commands.FilterCommand;
import uk.ac.susx.mlcl.lib.collect.ForwardingBiMap;

/**
 *
 * @author hiam20
 */
public class JDBCStringEnumerator extends BiMapEnumerator<String> {

    private static final long serialVersionUID = 1L;

    private static final String COLLECTION_FORWARDS = "forwards";

    private static final String COLLECTION_BACKWARDS = "backwards";

    private static final String COLLECTION_PROPERTIES = "properties";

    private static final String COLLECTION_NEXT_ID = "__next_id__";

    private static final int TRANSACTION_LIMIT = 1000;

    private File file;

    private DB db;

    private long modCount = 0;

    public JDBCStringEnumerator(DB db, File file) {
        this.file = null;
        this.db = db;
    }

    public JDBCStringEnumerator(DB db, File file, BiMap<Integer, String> map, AtomicInteger nextId) {
        super(map, nextId);
        this.file = file;
        this.db = db;
    }

    @Override
    protected synchronized void put(int id, String obj) {
        super.put(id, obj);
        ++modCount;
        if (modCount > TRANSACTION_LIMIT) {
            save();
            modCount = 0;
        }
    }

    
    public static synchronized Enumerator<String> newInstance(File file) {
        return load(file);
    }

    public static synchronized Enumerator<String> load(File file) {

        final boolean anonymous;
        if (file == null) {
            anonymous = true;
            try {
                file = File.createTempFile("jdbc-", ".tmp");
            } catch (IOException ex) {
                Logger.getLogger(JDBCStringEnumerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            anonymous = false;
        }

        DBMaker maker = DBMaker.openFile(file.toString());
        if (anonymous)
            maker.deleteFilesAfterClose();
        maker.closeOnExit();
        DB db = maker.make();

        Set<String> collections = db.getCollections().keySet();

        Map<Integer, String> forwards;
        Map<String, Integer> backwards;
        Map<String, String> props;
        AtomicInteger nextId;

        if (collections.contains(COLLECTION_FORWARDS)) {
            // collection exists            
            assert collections.contains(COLLECTION_BACKWARDS);
            assert collections.contains(COLLECTION_PROPERTIES);
            
            forwards = db.<Integer, String>getHashMap(COLLECTION_FORWARDS);
            backwards = db.<String, Integer>getHashMap(COLLECTION_BACKWARDS);
            props = db.<String, String>getHashMap(COLLECTION_PROPERTIES);
            
            assert backwards.containsKey(FilterCommand.FILTERED_STRING);
            assert forwards.containsKey(FilterCommand.FILTERED_ID);
            assert backwards.get(FilterCommand.FILTERED_STRING) == FilterCommand.FILTERED_ID;
            assert forwards.get(FilterCommand.FILTERED_ID).equals(FilterCommand.FILTERED_STRING);
            
            assert props.containsKey(COLLECTION_NEXT_ID);
            nextId = new AtomicInteger(Integer.valueOf(props.get(COLLECTION_NEXT_ID)));
            
        } else {
            
            forwards = db.<Integer, String>createHashMap(COLLECTION_FORWARDS);
            backwards = db.<String, Integer>createHashMap(COLLECTION_BACKWARDS);
            props = db.<String, String>createHashMap(COLLECTION_PROPERTIES);
            forwards.put(FilterCommand.FILTERED_ID, FilterCommand.FILTERED_STRING);
            backwards.put(FilterCommand.FILTERED_STRING, FilterCommand.FILTERED_ID);
            nextId = new AtomicInteger(FilterCommand.FILTERED_ID+1);
            props.put(COLLECTION_NEXT_ID, Integer.toString(0));
            db.commit();
        }


        ForwardingBiMap<Integer, String> map = ForwardingBiMap.<Integer, String>create(
                forwards, backwards);
        JDBCStringEnumerator instance = new JDBCStringEnumerator(
                db, file, map, nextId);
        
        instance.indexOf(FilterCommand.FILTERED_STRING);

        assert instance.indexOf(FilterCommand.FILTERED_STRING)
                == FilterCommand.FILTERED_ID;
        return instance;
    }

    public synchronized void save() {
        if(file == null) {
            Logger.getLogger(JDBCStringEnumerator.class.getName()).log(Level.WARNING,
                "Attempt made to save an enumerator with no attached file.");
            return;
        }

        db.<String, String>getHashMap(COLLECTION_PROPERTIES).put(
                COLLECTION_NEXT_ID, Integer.toString(getNextId()));
        db.commit();
    }

    public synchronized void close() {
        if(db == null || db.isClosed()) {
            Logger.getLogger(JDBCStringEnumerator.class.getName()).log(Level.WARNING,
                "Attempt made to close an enumerator that was not open.");
            return;
        }

        save();
        db.close();
    }

}
