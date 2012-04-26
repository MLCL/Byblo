/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib;

import static java.lang.Thread.yield;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author hamish
 */
public class MemoryProfilerTest {

    private static final int CHUNK_SIZE = 1024 * 1024 / 4;

    public MemoryProfilerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddRuntimeObject() throws Exception {

        MemoryProfiler mp = new MemoryProfiler();

        mp.add(Runtime.getRuntime());
        System.out.printf("Runtime size: %d bits, %d bytes.%n",
                          mp.getSizeBits(), mp.getSizeBytes());

    }

    @Test
    public void testAddThreadObject() throws Exception {

        MemoryProfiler mp = new MemoryProfiler();

        mp.add(Thread.currentThread());
        System.out.printf("Thread size: %d bits, %d bytes.%n",
                          mp.getSizeBits(), mp.getSizeBytes());
    }

    @Test
    public void testAddClassLoaderObject() throws Exception {

        MemoryProfiler mp = new MemoryProfiler();

        mp.add(Thread.currentThread().getContextClassLoader());
        System.out.printf("ClassLoader size: %d bits, %d bytes.%n",
                          mp.getSizeBits(), mp.getSizeBytes());
    }

    @Test
    public void testAddSelf() throws Exception {

        MemoryProfiler mp = new MemoryProfiler();
        mp.add(mp);
        System.out.printf("Self size: %d bits, %d bytes.%n",
                          mp.getSizeBits(), mp.getSizeBytes());
    }

    @Test
    public void testAddStringFooBar() throws Exception {

        MemoryProfiler mp = new MemoryProfiler();
        mp.add("foobar");
        mp.add(mp);
        System.out.printf("String \"foobar\" size: %d bits, %d bytes.%n",
                          mp.getSizeBits(), mp.getSizeBytes());
    }

    @Test
    @Ignore
    public void testAddDoubleObject() throws Exception {

        MemoryProfiler mp = new MemoryProfiler();

        mp.add(new Double(Math.PI));
        System.out.printf("1 Double size: %d bits, %d bytes.%n",
                          mp.getSizeBits(), mp.getSizeBytes());

        mp.add(new Double(Math.E));
        System.out.printf("2 Doubles size: %d bits, %d bytes.%n",
                          mp.getSizeBits(), mp.getSizeBytes());

        mp.add(new Double(0));
        System.out.printf("3 Doubles size: %d bits, %d bytes.%n",
                          mp.getSizeBits(), mp.getSizeBytes());
    }

    @Test
    @Ignore
    public void testStringObject() throws Exception {

        MemoryProfiler mp = new MemoryProfiler();

        String str = "foo";

        long staticExpected = 0;

        //  string object contains the following static fields:

//    private static final long serialVersionUID = -6849794470754667710L;
        staticExpected += MemoryProfiler.Primitives.LONG.getSizeBits();
//    private static final ObjectStreamField[] serialPersistentFields =
//        new ObjectStreamField[0];
        staticExpected += MemoryProfiler.OBJECT_REFERENCE_BITS;
        staticExpected += MemoryProfiler.ARRAY_OVERHEAD_BITS;

//    public static final Comparator<String> CASE_INSENSITIVE_ORDER
//                                         = new String.CaseInsensitiveComparator();
//  private static class CaseInsensitiveComparator
//                         implements Comparator<String>, java.io.Serializable {
//	// use serialVersionUID from JDK 1.2.2 for interoperability
//	private static final long serialVersionUID = 8575799808933029326L;
//...
//    }
//  
        staticExpected += MemoryProfiler.OBJECT_REFERENCE_BITS;
        staticExpected += MemoryProfiler.OBJECT_OVERHEAD_BITS;
        staticExpected += MemoryProfiler.Primitives.LONG.getSizeBits();

        if (staticExpected % MemoryProfiler.ALIGNEDMENT_BITS != 0)
            staticExpected += MemoryProfiler.ALIGNEDMENT_BITS
                    - (staticExpected % MemoryProfiler.ALIGNEDMENT_BITS);

        //  string object contains the following instance fields:


        long instanceExpected = 0;
        instanceExpected += MemoryProfiler.OBJECT_OVERHEAD_BITS;
//    private final char value[];
        instanceExpected += MemoryProfiler.OBJECT_REFERENCE_BITS;
        instanceExpected += MemoryProfiler.ARRAY_OVERHEAD_BITS;
        instanceExpected += MemoryProfiler.Primitives.CHAR.getSizeBits()
                * str.length();
//    private final int offset;
        instanceExpected += MemoryProfiler.Primitives.INT.getSizeBits();
//    private final int count;
        instanceExpected += MemoryProfiler.Primitives.INT.getSizeBits();
//    private int hash; // Default to 0
        instanceExpected += MemoryProfiler.Primitives.INT.getSizeBits();

        if (instanceExpected % MemoryProfiler.ALIGNEDMENT_BITS != 0)
            instanceExpected += MemoryProfiler.ALIGNEDMENT_BITS
                    - (instanceExpected % MemoryProfiler.ALIGNEDMENT_BITS);

        mp.add(str);

        System.out.printf(
                "String \"%s\" size: static = %d, instanec = %d, actual = %d%n",
                str, staticExpected, instanceExpected,
                mp.getSizeBits());;
        assertEquals(staticExpected + instanceExpected, mp.getSizeBits());


        // Add another string of the same size
        mp.add("bar");
        instanceExpected *= 2;
        assertEquals(staticExpected + instanceExpected, mp.getSizeBits());

        // Add another string of the same size
        mp.add("abc");
        mp.add("xyz");
        instanceExpected *= 2;
        assertEquals(staticExpected + instanceExpected, mp.getSizeBits());



    }

    /**
     * The Java Calendar object is famous for it's bloat. This test checks it
     * size in various ways.
     *
     * @throws Exception
     */
    @Test
    public void testAddCalendarObject() throws Exception {

        Calendar cal = Calendar.getInstance();

        MemoryProfiler mp = new MemoryProfiler();
        mp.add(cal);
        System.out.printf("Calendar size: %d bits, %d bytes.%n",
                          mp.getSizeBits(), mp.getSizeBytes());
    }

    @Test
    @Ignore
    public void testRepeatedAdds() throws Exception {
        Calendar cal = Calendar.getInstance();

        MemoryProfiler mp = new MemoryProfiler();
        mp.add(cal);

        System.out.println("\n");

        final long expectedSize = mp.getSizeBits();
        mp.add(cal);
        assertEquals(expectedSize, mp.getSizeBits());

    }

    @Test
    @Ignore
    public void testRepeatedAdds2() throws Exception {
        Calendar cal = Calendar.getInstance();

        MemoryProfiler mp = new MemoryProfiler();
        mp.add(cal);

        final long expectedSize = mp.getSizeBits();

        for (int i = 0; i < 10000; i++) {
            mp = new MemoryProfiler();
            mp.add(cal);
            assertEquals(expectedSize, mp.getSizeBits());
        }
    }

    interface Factory {

        Object newInstance();
    }

    @Test
    public void compareMemoryGrowth_LongArr() throws Exception {
        System.out.println("compareMemoryGrowth_LongArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                return new long[CHUNK_SIZE / 8];
            }
        });
    }

    @Test
    public void compareMemoryGrowth_IntArr() throws Exception {
        System.out.println("compareMemoryGrowth_IntArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                return new int[CHUNK_SIZE / 4];
            }
        });
    }

    @Test
    public void compareMemoryGrowth_DoubleArr() throws Exception {
        System.out.println("compareMemoryGrowth_DoubleArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                return new double[CHUNK_SIZE / 8];
            }
        });
    }

    @Test
    public void compareMemoryGrowth_FloatArr() throws Exception {
        System.out.println("compareMemoryGrowth_FloatArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                return new float[CHUNK_SIZE / 4];
            }
        });
    }

    @Test
    public void compareMemoryGrowth_ShortArr() throws Exception {
        System.out.println("compareMemoryGrowth_ShortArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                return new short[CHUNK_SIZE / 2];
            }
        });
    }

    @Test
    public void compareMemoryGrowth_CharArr() throws Exception {
        System.out.println("compareMemoryGrowth_CharArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                return new char[CHUNK_SIZE / 2];
            }
        });
    }

    @Test
    public void compareMemoryGrowth_ByteArr() throws Exception {
        System.out.println("compareMemoryGrowth_ByteArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                return new byte[CHUNK_SIZE];
            }
        });
    }

    @Test
    public void compareMemoryGrowth_booleanArr() throws Exception {
        System.out.println("compareMemoryGrowth_booleanArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                return new boolean[CHUNK_SIZE];
            }
        });
    }

    @Test
    public void compareMemoryGrowth_booleanArr2d() throws Exception {
        System.out.println("compareMemoryGrowth_booleanArr2d");
        final int size = (int) Math.floor(Math.sqrt(CHUNK_SIZE));
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                return new boolean[size][size];
            }
        });
    }

    @Test
    public void compareMemoryGrowth_Container() throws Exception {
        System.out.println("compareMemoryGrowth_Container");
        class Containder {

            int a = 1;

            long b = 1;

            float c = 1;

            double d = 1;

            boolean e = true;

            short f = 1;

            char g = 1;

            byte h = (byte) 1;

        

        }
        final int size = (int) new MemoryProfiler().add(new Containder()).
                getSizeBytes();
        System.out.println(CHUNK_SIZE / size);
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                Containder[] c = new Containder[CHUNK_SIZE / size];
                for (int i = 0; i < c.length; i++)
                    c[i] = new Containder();
                return c;
            }
        });
    }
    
    @Test
    public void compareMemoryGrowth_Container2() throws Exception {
        System.out.println("compareMemoryGrowth_Container2");
        class Containder {

            Object j;

        }
        final int size = (int) new MemoryProfiler().add(new Containder()).
                getSizeBytes();
        System.out.println(CHUNK_SIZE / size);
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                Containder[] c = new Containder[CHUNK_SIZE / size];
                for (int i = 0; i < c.length; i++)
                    c[i] = new Containder();
                return c;
            }
        });
    }

    @Test
    public void compareMemoryGrowth_BooleanArr() throws Exception {
        System.out.println("compareMemoryGrowth_BooleanArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                Boolean[] b = new Boolean[CHUNK_SIZE / 4];
                for (int i = 0; i < b.length; i++)
                    b[i] = i % 2 == 0 ? true : false;
                return b;
            }
        });
    }

    @Test
    public void compareMemoryGrowth_ClassArr() throws Exception {
        System.out.println("compareMemoryGrowth_ClassArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                Class[] b = new Class[CHUNK_SIZE / 4];
                for (int i = 0; i < b.length; i++)
                    b[i] = this.getClass();
                return b;
            }
        });
    }

    @Test
    public void compareMemoryGrowth_String() throws Exception {
        System.out.println("compareMemoryGrowth_String");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                StringBuilder sb = new StringBuilder();
                sb.append("x");
                int end = (int) Math.floor(
                        Math.log(CHUNK_SIZE / 2) / Math.log(2));
                for (int i = 0; i < end; i++)
                    sb.append(sb);
                return sb.toString();
            }
        });
    }

    @Test
    public void compareMemoryGrowth_StringBuilder() throws Exception {
        System.out.println("compareMemoryGrowth_StringBuilder");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                StringBuilder sb = new StringBuilder();
                sb.append("x");
                int end = (int) Math.floor(
                        Math.log(CHUNK_SIZE / 2) / Math.log(2));
                for (int i = 0; i < end; i++)
                    sb.append(sb);
                return sb;
            }
        });
    }

    enum Blah {

        X, Y, Z;

    }

    @Test
    public void compareMemoryGrowth_EnumArr() throws Exception {
        System.out.println("compareMemoryGrowth_EnumArr");

        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                Blah[] b = new Blah[CHUNK_SIZE / 4];
                for (int i = 0; i < b.length; i++)
                    b[i] = Blah.values()[i % 3];
                return b;
            }
        });
    }

    @Test
    public void compareMemoryGrowth_EnumSetArr() throws Exception {
        System.out.println("compareMemoryGrowth_EnumSetArr");

        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                EnumSet<Blah>[] b = new EnumSet[CHUNK_SIZE / 20];
                for (int i = 0; i < b.length; i++) {
                    b[i] = EnumSet.noneOf(Blah.class);
                    if (i % 2 == 0)
                        b[i].add(Blah.X);
                    if ((i / 2) % 2 == 0)
                        b[i].add(Blah.Y);
                    if ((i / 4) % 2 == 0)
                        b[i].add(Blah.Z);
                }
                return b;
            }
        });
    }

    @Test
    public void compareMemoryGrowth_ThreadArr() throws Exception {
        System.out.println("compareMemoryGrowth_ThreadArr");
        compareMemoryGrowth(new Factory() {

            public Object newInstance() {
                Thread[] b = new Thread[CHUNK_SIZE / 400];
                for (int i = 0; i < b.length; i++)
                    b[i] = new Thread();
                return b;
            }
        });
    }

    private void compareMemoryGrowth(Factory factory) throws Exception {

        gcUntilStable();
        final long startingMem = MiscUtil.usedMemory();

        System.out.println("Starting memory: " + MiscUtil.humanReadableBytes(
                startingMem));
        List<Object> list = new ArrayList<Object>();

        long expectedDiff = 0;
        for (int i = 0; i < 10; i++) {
            list.add(factory.newInstance());

            gcUntilStable();

            long usedSize = MiscUtil.usedMemory();

            MemoryProfiler mp = new MemoryProfiler();
            mp.add(list);

            long mpSize = mp.getSizeBytes() - expectedDiff;
            long diff = mpSize - usedSize;
//            if (i == 0)
//                expectedDiff = diff;

            double ratio = (double) usedSize / (double) mpSize;
            System.out.printf("%d: used=%s, profiler=%s, diff=%s, radio=%f%n", i,
                              MiscUtil.humanReadableBytes(usedSize),
                              MiscUtil.humanReadableBytes(mpSize),
                              MiscUtil.humanReadableBytes(diff),
                              ratio);

//            if (i != 0) {
//                assertEquals(ratio, 1.0, 0.01);
//            }
            expectedDiff += diff;
        }
    }

    private static void gcUntilStable() {

        long used = 0;
        int count = 0;
        do {
            used = MiscUtil.usedMemory();
            Runtime rt = Runtime.getRuntime();
            rt.runFinalization();
            rt.gc();
            ++count;
            yield();

        } while (used == MiscUtil.usedMemory());
//        System.out.println(count);

    }
//    
//    @Test
//    public void testSizeof() throws Exception {
//
////        System.out.printf("Primitive int %d %n", MiscUtil.sizeOf((int) 1));
////        System.out.printf("Primitive long %d %n", MiscUtil.sizeOf((long) 1));
////        System.out.printf("Primitive double %d %n", MiscUtil.sizeOf((double) 1));
////        System.out.printf("Primitive float %d %n", MiscUtil.sizeOf((float) 1));
////        System.out.printf("Primitive short %d %n", MiscUtil.sizeOf((short) 1));
////        System.out.printf("Primitive char %d %n", MiscUtil.sizeOf((char) 1));
////        System.out.printf("Primitive byte %d %n", MiscUtil.sizeOf((byte) 1));
////        System.out.printf("Primitive boolean %d %n", MiscUtil.sizeOf(true));
////
////        System.out.printf("Integer %d %n", MiscUtil.sizeOf((Integer) (int) 1));
////        System.out.printf("Long %d %n", MiscUtil.sizeOf((Long) (long) 1));
////        System.out.printf("Double %d %n", MiscUtil.sizeOf((Double) (double) 1));
////        System.out.printf("Float %d %n", MiscUtil.sizeOf((Float) (float) 1.0));
////        System.out.printf("Short %d %n", MiscUtil.sizeOf((Short) (short) 1));
////        System.out.printf("Character %d %n", MiscUtil.sizeOf((Character) (char) 1));
////        System.out.printf("Byte %d %n", MiscUtil.sizeOf((Byte) (byte) 1));
////        System.out.printf("Boolean %d %n", MiscUtil.sizeOf((Boolean) (boolean) true));
//
//        final int ARR_SIZE = 10000;
////
//        System.out.printf("Primitive int array  %d %n", MiscUtil.sizeOf(new int[ARR_SIZE]));
//        System.out.printf("Primitive long array  %d %n", MiscUtil.sizeOf(new long[ARR_SIZE]));
//        System.out.printf("Primitive double array  %d %n", MiscUtil.sizeOf(new double[ARR_SIZE]));
//        System.out.printf("Primitive float array  %d %n", MiscUtil.sizeOf(new float[ARR_SIZE]));
//        System.out.printf("Primitive short array  %d %n", MiscUtil.sizeOf(new short[ARR_SIZE]));
//        System.out.printf("Primitive char array  %d %n", MiscUtil.sizeOf(new char[ARR_SIZE]));
//        System.out.printf("Primitive byte array  %d %n", MiscUtil.sizeOf(new byte[ARR_SIZE]));
//        System.out.printf("Primitive boolean array %d %n", MiscUtil.sizeOf(new boolean[ARR_SIZE]));
////
////        System.out.printf("Empty Integer array  %d %n", MiscUtil.sizeOf(new Integer[ARR_SIZE]));
////        System.out.printf("Empty Long array  %d %n", MiscUtil.sizeOf(new Long[ARR_SIZE]));
////        System.out.printf("Empty Double array  %d %n", MiscUtil.sizeOf(new Double[ARR_SIZE]));
////        System.out.printf("Empty Float array  %d %n", MiscUtil.sizeOf(new Float[ARR_SIZE]));
////        System.out.printf("Empty Short array  %d %n", MiscUtil.sizeOf(new Short[ARR_SIZE]));
////        System.out.printf("Empty Character array  %d %n", MiscUtil.sizeOf(new Character[ARR_SIZE]));
////        System.out.printf("Empty Byte array  %d %n", MiscUtil.sizeOf(new Byte[ARR_SIZE]));
////        System.out.printf("Empty Boolean array %d %n", MiscUtil.sizeOf(new Boolean[ARR_SIZE]));
//
//        System.out.printf("Integer array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new int[ARR_SIZE])));
//        System.out.printf("Long array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new long[ARR_SIZE])));
//        System.out.printf("Double array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new double[ARR_SIZE])));
//        System.out.printf("Float array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new float[ARR_SIZE])));
//        System.out.printf("Short array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new short[ARR_SIZE])));
//        System.out.printf("Character array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new char[ARR_SIZE])));
//        System.out.printf("Byte array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new byte[ARR_SIZE])));
//        System.out.printf("Boolean array %d %n", MiscUtil.sizeOf(ArrayUtil.box(new boolean[ARR_SIZE])));
//
////
////        Calendar calendar = Calendar.getInstance();
////        System.out.printf("Calendar %d %n", MiscUtil.sizeOf(calendar));
//
//    }
//    
//    
//    /**
//     * Test of getSizeBits method, of class MemoryProfile.
//     */
//    @Test
//    public void testGetSizeBits() {
//        System.out.println("getSizeBits");
//        MemoryProfile instance = new MemoryProfile();
//        long expResult = 0L;
//        long result = instance.getSizeBits();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSizeBytes method, of class MemoryProfile.
//     */
//    @Test
//    public void testGetSizeBytes() {
//        System.out.println("getSizeBytes");
//        MemoryProfile instance = new MemoryProfile();
//        long expResult = 0L;
//        long result = instance.getSizeBytes();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of add method, of class MemoryProfile.
//     */
//    @Test
//    public void testAdd_int() {
//        System.out.println("add");
//        int value = 0;
//        MemoryProfile instance = new MemoryProfile();
//        MemoryProfile expResult = null;
//        MemoryProfile result = instance.add(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of add method, of class MemoryProfile.
//     */
//    @Test
//    public void testAdd_long() {
//        System.out.println("add");
//        long value = 0L;
//        MemoryProfile instance = new MemoryProfile();
//        MemoryProfile expResult = null;
//        MemoryProfile result = instance.add(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of add method, of class MemoryProfile.
//     */
//    @Test
//    public void testAdd_short() {
//        System.out.println("add");
//        short value = 0;
//        MemoryProfile instance = new MemoryProfile();
//        MemoryProfile expResult = null;
//        MemoryProfile result = instance.add(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of add method, of class MemoryProfile.
//     */
//    @Test
//    public void testAdd_char() {
//        System.out.println("add");
//        char value = ' ';
//        MemoryProfile instance = new MemoryProfile();
//        MemoryProfile expResult = null;
//        MemoryProfile result = instance.add(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of add method, of class MemoryProfile.
//     */
//    @Test
//    public void testAdd_byte() {
//        System.out.println("add");
//        byte value = 0;
//        MemoryProfile instance = new MemoryProfile();
//        MemoryProfile expResult = null;
//        MemoryProfile result = instance.add(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of add method, of class MemoryProfile.
//     */
//    @Test
//    public void testAdd_boolean() {
//        System.out.println("add");
//        boolean value = false;
//        MemoryProfile instance = new MemoryProfile();
//        MemoryProfile expResult = null;
//        MemoryProfile result = instance.add(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of add method, of class MemoryProfile.
//     */
//    @Test
//    public void testAdd_float() {
//        System.out.println("add");
//        float value = 0.0F;
//        MemoryProfile instance = new MemoryProfile();
//        MemoryProfile expResult = null;
//        MemoryProfile result = instance.add(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of add method, of class MemoryProfile.
//     */
//    @Test
//    public void testAdd_double() {
//        System.out.println("add");
//        double value = 0.0;
//        MemoryProfile instance = new MemoryProfile();
//        MemoryProfile expResult = null;
//        MemoryProfile result = instance.add(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of add method, of class MemoryProfile.
//     */
//    @Test
//    public void testAdd_Object() {
//        System.out.println("add");
//        Object value = null;
//        MemoryProfile instance = new MemoryProfile();
//        MemoryProfile expResult = null;
//        MemoryProfile result = instance.add(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addAll method, of class MemoryProfile.
//     */
//    @Test
//    public void testAddAll() {
//        System.out.println("addAll");
//        Object[] values = null;
//        MemoryProfile instance = new MemoryProfile();
//        MemoryProfile expResult = null;
//        MemoryProfile result = instance.addAll(values);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
