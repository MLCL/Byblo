/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.tasks;

import com.google.common.annotations.Beta;
import java.util.Comparator;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @author hamish
 */
@Beta
public class ObjectExternalSortTask<T> extends AbstractTask {

    private final ObjectSortTask<T> sortTaskDeligate;

    private int maxChunkSize = 1000000;

    public ObjectExternalSortTask(Source<T> source, Sink<T> sink,
                            Comparator<T> comparator) {
        sortTaskDeligate = new ObjectSortTask<T>(source, sink, comparator);
    }

    public ObjectExternalSortTask(Source<T> source, Sink<T> sink) {
        sortTaskDeligate = new ObjectSortTask<T>(source, sink);
    }

    public ObjectExternalSortTask() {
        sortTaskDeligate = new ObjectSortTask<T>();
    }

    @Override
    protected void initialiseTask() throws Exception {
        sortTaskDeligate.initialiseTask();
    }

    @Override
    protected void runTask() throws Exception {

        Source<Chunk<T>> chunker = Chunker.newInstance(
                getSource(), maxChunkSize);

        while (chunker.hasNext()) {
            submitSortTask(asStore(chunker.read()));
        }
    }

    protected void submitSortTask(
            final Store<T> from) {
        submitSortTask(from, newTemporaryStore());
    }

    protected void submitSortTask(
            final Store<T> from, final Store<T> to) {
        ObjectSortTask<T> sortTask = new ObjectSortTask<T>();
        submit(new ForwardingTask(sortTask) {

            @Override
            public void run() {
                setSource(from.openSource());
                setSink(to.openSink());
                setComparator(ObjectExternalSortTask.this.getComparator());

                super.run();
            }
        });



    }

    protected void submitMergeTask(
            Store<T> from1, Store<T> from2) {
        submitMergeTask(from1, from2, newTemporaryStore());
    }

    protected void submitMergeTask(
            Store<T> from1, Store<T> from2, Store<T> to) {
//        submit(new Runnable() {
//
//            @Override
//            public void run() {
//            }
//        });
    }

    protected void submit(Task task) {
    }

    protected Store<T> newTemporaryStore() {
        return null;
    }

    protected Store<T> asStore(Source<T> src) {
        return null;
    }

    protected Store<T> asStore(Sink<T> src) {
        return null;
    }

    @Override
    protected void finaliseTask() throws Exception {
        sortTaskDeligate.finaliseTask();
    }

    public final void setSource(Source<T> source) {
        sortTaskDeligate.setSource(source);
    }

    public final void setSink(Sink<T> sink) {
        sortTaskDeligate.setSink(sink);
    }

    public final Source<T> getSource() {
        return sortTaskDeligate.getSource();
    }

    public final Sink<T> getSink() {
        return sortTaskDeligate.getSink();
    }

    public final void setComparator(Comparator<T> comparator) {
        sortTaskDeligate.setComparator(comparator);
    }

    public final Comparator<T> getComparator() {
        return sortTaskDeligate.getComparator();
    }

    protected interface Resource {

        void aquire();

        /**
         * Notify the resource it no longer required.
         *
         * This may lead to data being deleted or memory freed if nothing else
         * still requires the resource.
         */
        void release();
    }

    protected interface Store<T> extends Resource {

        Source<T> openSource();

        Sink<T> openSink();

        boolean isReadable();

        boolean isWriteable();
    }

    protected class ForwardingTask implements Task {

        private final Task inner;

        public ForwardingTask(Task inner) {
            this.inner = inner;
        }

        public Task getInner() {
            return inner;
        }

        @Override
        public void run() {
            inner.run();
        }

        @Override
        public Exception getException() {
            return inner.getException();
        }

        @Override
        public boolean isExceptionCaught() {
            return inner.isExceptionCaught();
        }

        @Override
        public void throwException() throws Exception {
            inner.throwException();
        }

        @Override
        public String getProperty(String key) {
            return inner.getProperty(key);
        }

        @Override
        public void setProperty(String key, String value) {
            inner.setProperty(key, value);
        }
    }
}
