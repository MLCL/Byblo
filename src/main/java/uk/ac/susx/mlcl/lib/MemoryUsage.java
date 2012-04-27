/*
 * Copyright (c) 2010-2012, MLCL, University of Sussex
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
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.lib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
import sun.misc.Unsafe;
import uk.ac.susx.mlcl.lib.collect.IdentityHashSet;

/**
 * Class to calculate the approximate memory usage of java objects.
 *
 * This implementation uses reflection inspect each object, descending into all
 * composite fields, and analyzing the associated classes.
 *
 * @author Hamish Morgan
 */
public class MemoryUsage {

    protected static final long OBJECT_REFERENCE_BITS;

    protected static final long OBJECT_OVERHEAD_BITS;

    protected static final long ARRAY_OVERHEAD_BITS;

    protected static final long ARRAY_REFERENCE_BITS;

    protected static final long ALIGNEDMENT_BITS;

    static {

        boolean java64bit;
        long objectOverheadBits;
        long objectReferenceBits;
        long arrayOverheadBits;
        long arrayReferenceBits;
        long alignmentBits;

        try {

            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            if (!unsafeField.isAccessible())
                unsafeField.setAccessible(true);
            Unsafe theUnsafe = (Unsafe) unsafeField.get(null);

            java64bit = theUnsafe.addressSize() == 8;

            objectReferenceBits = java64bit ? 64 : 32;

            final Object o = new Object() {

                int x = 1;

            };
            objectOverheadBits = Long.MAX_VALUE;
            for (Field f : o.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                objectOverheadBits = Math.min(
                        objectOverheadBits,
                        theUnsafe.objectFieldOffset(f) * 8);
            }

            arrayOverheadBits = theUnsafe.arrayBaseOffset(int[].class) * 8;
            arrayReferenceBits = theUnsafe.arrayIndexScale(Object[].class) * 8;
            
            alignmentBits = 64; //java64bit ? 64 : 32;

        } catch (Exception ex) {
            // Fallback to guessing from System properties

            final Properties props = System.getProperties();
            if (props.contains("sun.arch.data.model")) {
                java64bit = props.getProperty("sun.arch.data.model").contains(
                        "64");
            } else if (props.contains("java.vm.name")
                    && props.getProperty("java.vm.name").contains("64-Bit")) {
                java64bit = true;
            } else {
                final String osArch = props.getProperty("os.arch");
                java64bit = (osArch != null && osArch.contains("64"));
            }

            objectReferenceBits = java64bit ? 64 : 32;
            objectOverheadBits = 2 * objectReferenceBits;

            arrayOverheadBits = 3 * objectReferenceBits;
            arrayReferenceBits = 32;

            alignmentBits = 64; //java64bit ? 64 : 32;
        }

        OBJECT_REFERENCE_BITS = objectReferenceBits;
        OBJECT_OVERHEAD_BITS = objectOverheadBits;
        ARRAY_OVERHEAD_BITS = arrayOverheadBits;
        ARRAY_REFERENCE_BITS = arrayReferenceBits;
        ALIGNEDMENT_BITS = alignmentBits;
    }

    private final Set<Object> objectsSeen;

    private final Map<Class<?>, ClassInfo> classInfoCache;

    private final Deque<Object> queue;

    private long instanceSizeBits = 0;

    private long staticSizeBits = 0;

    /**
     * Construct a new instance of the MemoryProfiler
     */
    public MemoryUsage() {
        objectsSeen = new IdentityHashSet<Object>();
        classInfoCache = new IdentityHashMap<Class<?>, ClassInfo>();
        queue = new ArrayDeque<Object>();
    }

    /**
     * Return the summed memory footprint of all added objects, both static and
     * instance data, in bits.
     *
     * @return bits of both static and instance data seen
     */
    public long getSizeBits() {
        return instanceSizeBits + staticSizeBits;
    }

    /**
     * Return the summed memory footprint of all added objects, both static and
     * instance data, in bytes (rounding up to the nearest byte).
     *
     * @return bytes of both static and instance data seen
     */
    public long getSizeBytes() {
        return getSizeBits() / 8 + (getSizeBits() % 8 > 0 ? 1 : 0);
    }

    /**
     * Return the summed memory footprint of all static (non-instance) data
     * added objects, in bits.
     *
     * @return bits of instance data seen
     */
    public long getStaticSizeBits() {
        return staticSizeBits;
    }

    /**
     * Return the summed memory footprint of all static (non-instance) data
     * added objects, in Bytes (rounding up to the nearest byte).
     *
     * @return bytes of static data seen
     */
    public long getStaticSizeBytes() {
        return getStaticSizeBits() / 8 + (getStaticSizeBits() % 8 > 0 ? 1 : 0);
    }

    /**
     * Return the summed memory footprint of all instance (non-static) data
     * added objects, in bits.
     *
     * @return bits of instance data seen
     */
    public long getInstanceSizeBits() {
        return instanceSizeBits;
    }

    /**
     * Return the summed memory footprint of all instance (non-static) data
     * added objects, in Bytes (rounding up to the nearest byte).
     *
     * @return bytes of instance data seen
     */
    public long getInstanceSizeBytes() {
        return getInstanceSizeBits() / 8 + (getInstanceSizeBits() % 8 > 0 ? 1 : 0);
    }

    /**
     * Return the number of object instances that have been seen by the
     * MemoryProfiler. This will include all objects added, and their
     * non-primitives fields with both static and instance values.
     *
     * @return number of unique instances seen.
     */
    public int getNumObjectsSeen() {
        return objectsSeen.size();
    }

    /**
     * Return the number of class types that have been seen by the
     * MemoryProfiler. This will include the class of all objects added, their
     * super-types, and the classes of all fields.
     *
     * @return number of unique classes seen.
     */
    public int getNumClassesSeen() {
        return classInfoCache.size();
    }

    /**
     * Return a set containing all the unique classes that have been seen by
     * this MemoryUsage object.
     *
     * @return all seen classes
     */
    public Set<Class<?>> getSeenClasses() {
        return classInfoCache.keySet();
    }

    /**
     * Return a set containing all the unique objects that have been seen by
     * this MemoryUsage object.
     *
     * @return all seen classes
     */
    public Set<Object> getSeenObjects() {
        return new IdentityHashSet<Object>(objectsSeen);
    }

    /**
     * Produce an information string detailing the number of classes and objects
     * seen, with their estimated memory consumption.
     *
     * @return
     */
    public String getInfoString() {
        return MessageFormat.format(
                "Seen {0} classes and {1} objects; consuming {2} "
                + "(of which {3} static, {4} instance).",
                getNumClassesSeen(), getNumObjectsSeen(),
                MiscUtil.humanReadableBytes(getSizeBytes()),
                MiscUtil.humanReadableBytes(getStaticSizeBytes()),
                MiscUtil.humanReadableBytes(getInstanceSizeBytes()));
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + getInfoString() + "}";
    }

    /**
     * Calculate the memory usage of the given object, accumulating the size
     * with any previously added objects.
     *
     * @param obj
     * @return self
     * @throws SecurityException if the field access restrictions request is
     */
    public MemoryUsage add(Object obj) throws SecurityException {
        Checks.checkNotNull("obj", obj);

        queue.addLast(obj);
        while (!queue.isEmpty()) {
            final Object o = queue.removeFirst();
            if (!objectsSeen.contains(o))
                visitObject(o);
        }
        return this;
    }

    /**
     * Reset the MemoryProfiler object, clearing the caches and setting all size
     * counters back to 0.
     *
     * @return self
     */
    public MemoryUsage clear() {
        assert queue.isEmpty();
        objectsSeen.clear();
        classInfoCache.clear();
        staticSizeBits = 0;
        instanceSizeBits = 0;
        return this;
    }

    /**
     *
     * @param type
     * @return
     * @throws SecurityException if the field access restrictions request is
     * denied.
     */
    private ClassInfo visitClass(Class<?> type) throws SecurityException {
        assert type != null;
        assert !classInfoCache.containsKey(type);


        final Field[] fields = type.getDeclaredFields();
        final List<Field> instanceFields =
                new ArrayList<Field>(fields.length);


        long primitiveInstanceSizeBits = 0;
        long _staticSizeBits = 0;


        primitiveInstanceSizeBits -= 32;

        try {

            for (Field field : fields) {

                if (Modifier.isStatic(field.getModifiers())) {

                    if (field.getType().isPrimitive()) {

                        _staticSizeBits += Primitive.sizeBits(field.getType());

                    } else {
                        _staticSizeBits += OBJECT_REFERENCE_BITS;
                        field.setAccessible(true);
                        Object fieldInst = field.get(null);
                        if (fieldInst != null && !objectsSeen.contains(fieldInst))
                            queue.addLast(fieldInst);
                    }

                } else if (field.getType().isPrimitive()) {

                    primitiveInstanceSizeBits += Primitive.sizeBits(field.
                            getType());

                } else {

                    instanceFields.add(field);
                    primitiveInstanceSizeBits += OBJECT_REFERENCE_BITS;
                    field.setAccessible(true);
                }
            }
        } catch (IllegalAccessException ex) {
            throw new AssertionError(
                    "Access restrictions should have been overridden.");
        }

        this.staticSizeBits += align(_staticSizeBits);

        final ClassInfo info = new ClassInfo(
                primitiveInstanceSizeBits,
                instanceFields.toArray(new Field[0]));
        classInfoCache.put(type, info);
        return info;
    }

    /**
     *
     * @param instance
     * @throws SecurityException if the field access restrictions request is
     * denied.
     */
    private void visitObject(Object instance) throws SecurityException {
        assert instance != null;
        assert !objectsSeen.contains(instance);

        objectsSeen.add(instance);

        long _instanceSizeBits = 0;
        Class<?> type = instance.getClass();
        if (type.isArray()) {

            _instanceSizeBits += ARRAY_OVERHEAD_BITS;
            final Class<?> compType = type.getComponentType();
            if (compType.isPrimitive()) {

                _instanceSizeBits += Primitive.sizeBits(compType)
                        * Array.getLength(instance);

            } else {

                _instanceSizeBits += ARRAY_REFERENCE_BITS
                        * Array.getLength(instance);
                for (Object value : ((Object[]) instance))
                    if (value != null && !objectsSeen.contains(value))
                        queue.addLast(value);
            }

        } else {


            _instanceSizeBits += OBJECT_OVERHEAD_BITS;

            try {
                do {

                    ClassInfo info = classInfoCache.get(type);
                    if (info == null)
                        info = visitClass(type);

                    _instanceSizeBits += info.primitiveInstanceSize;

                    for (Field field : info.compositeInstanceFields) {

                        final Object fieldInst = field.get(instance);
                        if (fieldInst != null)
                            queue.addLast(fieldInst);
                    }

                } while ((type = type.getSuperclass()) != null);
            } catch (IllegalAccessException ex) {
                throw new AssertionError(
                        "Access restrictions should have been overridden.");
            }

        }
        instanceSizeBits += align(_instanceSizeBits);

    }

    /**
     *
     * @param bits
     * @return
     */
    private long align(long bits) {
        return bits + (bits % ALIGNEDMENT_BITS == 0 ? 0
                       : ALIGNEDMENT_BITS - (bits % ALIGNEDMENT_BITS));
    }

    /**
     *
     */
    private static final class ClassInfo {

        /**
         * Total size of instance fields that are primitives or references.
         */
        final long primitiveInstanceSize;

        /**
         * Array of composite (non-primitive) instance (non-static) fields.
         */
        final Field[] compositeInstanceFields;

        private ClassInfo(final long primitiveInstanceSize,
                          final Field[] compositeInstanceFields) {
            this.primitiveInstanceSize = primitiveInstanceSize;
            this.compositeInstanceFields = compositeInstanceFields;
        }
    }

    /**
     *
     */
    protected enum Primitive {

        INT(Integer.SIZE, int.class),
        LONG(Long.SIZE, long.class),
        CHAR(Character.SIZE, char.class),
        FLOAT(Float.SIZE, float.class),
        DOUBLE(Double.SIZE, double.class),
        BYTE(Byte.SIZE, byte.class),
        BOOLEAN(Byte.SIZE, boolean.class),
        SHORT(Short.SIZE, short.class),
        VOID(0, void.class);

        private static final Map<Class<?>, Primitive> classLookupTable;

        static {
            classLookupTable = new IdentityHashMap<Class<?>, Primitive>(
                    Primitive.values().length);
            for (Primitive prim : Primitive.values())
                classLookupTable.put(prim.getType(), prim);
        }

        private final long sizeBits;

        private final Class<?> type;

        private Primitive(long bits, Class<?> type) {
            this.sizeBits = bits;
            this.type = type;
        }

        public long getSizeBits() {
            return sizeBits;
        }

        public Class<?> getType() {
            return type;
        }

        public static Primitive valueOf(Class<?> type) {
            return classLookupTable.get(type);
        }

        public static long sizeBits(Class<?> type) {
            return valueOf(type).getSizeBits();
        }
    }
}
