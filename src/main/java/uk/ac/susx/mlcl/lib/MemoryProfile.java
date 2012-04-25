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
 * TODO: Byte alignment is not accounted for.
 *
 * TODO: 64bit architecture is not detected.
 *
 * TODO: Print memory usage tree
 *
 * @author hamish
 */
public class MemoryProfile {

    private static final boolean JAVA_64BIT = true;

    private static final long REFERENCE_BITS = JAVA_64BIT ? 64 : 32;

    private static final long OBJECT_HEADER_BITS = REFERENCE_BITS * 2;

    private static final long ARRAY_HEADER_BITS = REFERENCE_BITS * 3;

    private long sizeBits = 0;

    private boolean staticFieldsIgnored = false;

    private boolean accessableObeyed = false;

    /**
     * Whether to use depth-First search strategy when inspecting object.
     * Otherwise use BFS strategy. DFS will probably use less memory.
     */
    private boolean dfs = false;

    private Deque<Object> queue = new ArrayDeque<Object>();

    private Map<Object, Object> seen = new IdentityHashMap<Object, Object>();

    public MemoryProfile() {
    }

    public boolean isAccessableObeyed() {
        return accessableObeyed;
    }

    public MemoryProfile setAccessableObeyed(boolean accessableObeyed) {
        this.accessableObeyed = accessableObeyed;
        return this;
    }

    public boolean isStaticFieldIgnored() {
        return staticFieldsIgnored;
    }

    public MemoryProfile setStaticFieldsIgnored(boolean staticFieldsIgnored) {
        this.staticFieldsIgnored = staticFieldsIgnored;
        return this;
    }

    /**
     * Return the summed memory footprint of all added objects, in bits.
     *
     * @return
     */
    public long getSizeBits() {
        return sizeBits;
    }

    public MemoryProfile clear() {
        queue.clear();
        seen.clear();
        return this;
    }

    /**
     * Return the summed memory footprint of all added objects, in bytes,
     * rounding up to the nearest byte.
     *
     * @return
     */
    public long getSizeBytes() {
        return sizeBits / 8 + (sizeBits % 8 > 0 ? 1 : 0);
    }

    public MemoryProfile add(Object value) {
        if (value != null && !seen.containsKey(value))
            queue.offerLast(value);
        return this;
    }

    public MemoryProfile build() throws IllegalAccessException {
        while (!queue.isEmpty()) {
            final Object instance = dfs
                    ? queue.pollLast()
                    : queue.pollFirst();

            if (seen.containsKey(instance))
                continue;
            seen.put(instance, instance);


            if (instance.getClass().isArray()) {

                sizeOfArray(instance);

            } else {

                processNonArray(instance);
            }
        }
        return this;
    }

    private void sizeOfArray(Object array) {
        assert array.getClass().isArray();
        Class<?> compType = array.getClass().getComponentType();

        if (compType.isPrimitive()) {
            sizeBits += ARRAY_HEADER_BITS;
            sizeBits += Primitives.valueOf(compType).getSizeBits()
                    * Array.getLength(array);
        } else {
            sizeBits += ARRAY_HEADER_BITS;
            sizeBits += REFERENCE_BITS * Array.getLength(array);
            for (Object value : ((Object[]) array))
                add(value);
        }

    }

    IdentityHashMap<Class<?>, Long> primitiveFieldsCache =
            new IdentityHashMap<Class<?>, Long>();

    private void processNonArray(Object instance) throws IllegalAccessException {
        Class<?> type = instance.getClass();
        assert !type.isArray();

        long complexSizeBits = OBJECT_HEADER_BITS;

        do {


            final Field[] fields = isAccessableObeyed()
                    ? type.getFields()
                    : type.getDeclaredFields();


            for (Field field : fields) {

                if (isStaticFieldIgnored() && Modifier.isStatic(
                        field.getModifiers()))
                    continue;

                if (field.getType().isPrimitive())
                    continue;

                final boolean accessible = field.isAccessible();
                if (!accessible && !isAccessableObeyed())
                    field.setAccessible(true);

                if (field.getType().isPrimitive())
                    continue;

                complexSizeBits += REFERENCE_BITS;
                add(field.get(instance));


                if (!accessible && !isAccessableObeyed())
                    field.setAccessible(accessible);
            }

            // process primitive information

            if (primitiveFieldsCache.containsKey(type)) {
                complexSizeBits += primitiveFieldsCache.get(type);
            } else {
                long primitiveSizeBits = 0;
                for (Field field : fields) {

                    if (isStaticFieldIgnored() && Modifier.isStatic(
                            field.getModifiers()))
                        continue;

                    final boolean accessible = field.isAccessible();
                    if (!accessible && !isAccessableObeyed())
                        field.setAccessible(true);

                    if (field.getType().isPrimitive()) {

                        primitiveSizeBits += Primitives.valueOf(field.getType()).
                                getSizeBits();

                    }
                    if (!accessible && !isAccessableObeyed())
                        field.setAccessible(accessible);

                }
                complexSizeBits += primitiveSizeBits;
                primitiveFieldsCache.put(type, primitiveSizeBits);
            }
        } while ((type = type.getSuperclass()) != null);

        this.sizeBits += complexSizeBits;

    }
//
//    private void process(Class<?> clazz, Object instance) throws IllegalAccessException {
//
//        boolean simpleObject = true;
//
//        long objSizeBits = OBJECT_HEADER_BITS;
//
//        final Field[] fields = isAccessableObeyed()
//                ? clazz.getFields()
//                : clazz.getDeclaredFields();
//
//        for (Field field : fields) {
//
//            if (isStaticFieldIgnored() && Modifier.isStatic(field.getModifiers()))
//                continue;
//
//            final boolean accessible = field.isAccessible();
//            if (!accessible && !isAccessableObeyed())
//                field.setAccessible(true);
//
//            if (field.getType().isPrimitive()) {
//
//                objSizeBits += PRIMITIVE_SIZE_BITS.get(field.getType());
//
//            } else {
//
//                objSizeBits += REFERENCE_BITS;
//                add(field.get(instance));
//                simpleObject = false;
//
//            }
//            if (!accessible && !isAccessableObeyed())
//                field.setAccessible(accessible);
//
//        }
//
//        System.out.println(simpleObject);
//
//        this.sizeBits += objSizeBits;
//    }

    private enum Primitives {

        INT(Integer.SIZE, int.class),
        LONG(Long.SIZE, long.class),
        SHORT(Short.SIZE, short.class),
        CHAR(Character.SIZE, char.class),
        BYTE(Byte.SIZE, byte.class),
        FLOAT(Float.SIZE, float.class),
        DOUBLE(Double.SIZE, double.class),
        BOOLEAN(1, boolean.class),
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
    }
}
