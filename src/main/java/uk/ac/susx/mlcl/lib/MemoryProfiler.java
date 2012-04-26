/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 *
 *
 * TODO: Word alignment in object structure is not accounted for.
 *
 * TODO: 32/64bit architecture is not detected.
 *
 * TODO: Print memory usage tree
 *
 * @author hamish
 */
public class MemoryProfiler {

    private static final boolean JAVA_64BIT = false;

    protected static final long OBJECT_REFERENCE_BITS = 48; //JAVA_64BIT ? 64 : 32;
    protected static final long OBJECT_REFERENCE_COMPRESSED_BITS = 32;

    protected static final long OBJECT_OVERHEAD_BITS = 32 * 2;

    protected static final long ARRAY_OVERHEAD_BITS = 32 * 3;

    protected static final long ALIGNEDMENT_BITS = 64;

    private long instanceSizeBits = 0;

    private long staticSizeBits = 0;

    private Set<Object> objectSeen = new IdentityHashSet<Object>();

    private Map<Class<?>, ClassInfo> classInfoCache =
            new IdentityHashMap<Class<?>, ClassInfo>();

    public static class ClassInfo {

        /**
         * Store count of instance fields that are primitives or references.
         */
        long primitivesSize = 0;

        /**
         * Cache the field because it takes an rather a long time to call
         * Class.getDeclatedFields()
         */
        Field[] fields = null;

    }

    public MemoryProfiler() {
    }

    /**
     * Return the summed memory footprint of all added objects, in bits.
     *
     * @return
     */
    public long getSizeBits() {
        return instanceSizeBits + staticSizeBits;
    }

    public MemoryProfiler clear() {
        objectSeen.clear();
        classInfoCache.clear();
        return this;
    }

    /**
     * Return the summed memory footprint of all added objects, in bytes,
     * rounding up to the nearest byte.
     *
     * @return
     */
    public long getSizeBytes() {
        return instanceSizeBits / 8 + (instanceSizeBits % 8 > 0 ? 1 : 0);
    }

    public MemoryProfiler add(Object value) throws IllegalAccessException {
        sizeOf(value);
        return this;
    }

    private long align(long bits) {
        return bits + (bits % ALIGNEDMENT_BITS == 0 ? 0 : ALIGNEDMENT_BITS - (bits % ALIGNEDMENT_BITS));
    }

    private ClassInfo shallowClassInfo(Class<?> type) throws IllegalAccessException {
        assert type != null;

        ClassInfo info = classInfoCache.get(type);
        long size = 0;
        if (info == null) {
            info = new ClassInfo();
            classInfoCache.put(type, info);

            info.fields = type.getDeclaredFields();
            for (Field field : info.fields) {
                field.setAccessible(true);
//                if(field.isSynthetic())
//                    System.out.println("synthetic: " + field);
//                Modifier.
//                if(Modifier.isNative(field.getModifiers()) ){
//                    System.out.println("native: " + field);
//                }
                
            }
            
            for (Field field : info.fields) {

                if (Modifier.isStatic(field.getModifiers())) {

                    if (field.getType().isPrimitive()) {

                        size += Primitives.sizeOf(field.getType());

                    } else {

                        size += OBJECT_REFERENCE_BITS;
                        Object fieldInst = field.get(null);
                        sizeOf(fieldInst);
                    }

                } else if (field.getType().isPrimitive()) {

                    info.primitivesSize += Primitives.sizeOf(field.getType());

                } else {

                    info.primitivesSize += OBJECT_REFERENCE_BITS;
                }
            }
        }
        staticSizeBits += align(size);
        return info;
    }

    private void sizeOf(Object instance) throws IllegalAccessException {
        if (instance == null || objectSeen.contains(instance)) {
            return;
        }

        objectSeen.add(instance);

        long size = 0;
        Class<?> type = instance.getClass();
        if (type.isArray()) {

            size += ARRAY_OVERHEAD_BITS;
            Class<?> compType = type.getComponentType();
            if (compType.isPrimitive()) {

                size += Primitives.sizeOf(compType)
                        * Array.getLength(instance);

            } else {

                size += OBJECT_REFERENCE_COMPRESSED_BITS
                        * Array.getLength(instance);
                for (Object value : ((Object[]) instance))
                    sizeOf(value);
            }

        } else {


            size += OBJECT_OVERHEAD_BITS;

            do {

                ClassInfo info = shallowClassInfo(type);
                size += info.primitivesSize;

                for (Field field : info.fields) {

                    if (Modifier.isStatic(field.getModifiers())
                            || field.getType().isPrimitive())
                        continue;

                    sizeOf(field.get(instance));
                }

            } while ((type = type.getSuperclass()) != null);

        }
        instanceSizeBits += align(size);

    }

    protected enum Primitives {

        INT(Integer.SIZE, int.class),
        LONG(Long.SIZE, long.class),
        SHORT(Short.SIZE, short.class),
        CHAR(Character.SIZE, char.class),
        BYTE(Byte.SIZE, byte.class),
        FLOAT(Float.SIZE, float.class),
        DOUBLE(Double.SIZE, double.class),
        BOOLEAN(8, boolean.class),
        VOID(0, void.class);

        private static final Map<Class<?>, Primitives> classLookupTable;

        static {
            classLookupTable = new HashMap<Class<?>, Primitives>(9);
            for (Primitives prim : Primitives.values())
                classLookupTable.put(prim.getType(), prim);
        }

        private final long sizeBits;

        private final Class<?> type;

        private Primitives(long bits, Class<?> type) {
            this.sizeBits = bits;
            this.type = type;
        }

        public long getSizeBits() {
            return sizeBits;
        }

        public Class<?> getType() {
            return type;
        }

        public static Primitives valueOf(Class<?> type) {
            return classLookupTable.get(type);
        }

        public static long sizeOf(Class<?> type) {
            return classLookupTable.get(type).getSizeBits();
        }
    }

    private static class IdentityHashSet<T>
            extends AbstractSet<T>
            implements Set<T> {

        private IdentityHashMap<T, T> map;

        private IdentityHashSet() {
            map = new IdentityHashMap<T, T>();
        }

        @Override
        public boolean contains(Object o) {
            return map.containsKey(o);
        }

        @Override
        public boolean add(T e) {
            map.put(e, e);
            return true;
        }

        @Override
        public Iterator<T> iterator() {
            return map.keySet().iterator();
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public void clear() {
            map.clear();
        }
    }
}
