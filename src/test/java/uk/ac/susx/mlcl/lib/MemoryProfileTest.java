/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib;

import java.util.Calendar;
import org.junit.*;
import static org.junit.Assert.*;
import uk.ac.susx.mlcl.lib.collect.ArrayUtil;

/**
 *
 * @author hamish
 */
public class MemoryProfileTest {

    public MemoryProfileTest() {
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

    /**
     * The Java Calendar object is famous for it's bloat. This test checks it
     * size in various ways.
     *
     * @throws Exception
     */
    @Test
    public void testAddCalendarObject() throws Exception {

        Calendar cal = Calendar.getInstance();

        {
            MemoryProfile mp = new MemoryProfile();
            mp.add(cal);
            mp.build();
            System.out.printf("Calendar size: %d bits, %d bytes.%n",
                              mp.getSizeBits(), mp.getSizeBytes());
        }
        {
            MemoryProfile mp = new MemoryProfile();
            mp.setAccessableObeyed(true);
            mp.add(cal);
            mp.build();
            System.out.printf("Calendar (accessable) size: %d bits, %d bytes.%n",
                              mp.getSizeBits(), mp.getSizeBytes());
        }
        {
            MemoryProfile mp = new MemoryProfile();
            mp.setStaticFieldsIgnored(true);
            mp.add(cal);
            mp.build();
            System.out.printf("Calendar (non-static) size: %d bits, %d bytes.%n",
                              mp.getSizeBits(), mp.getSizeBytes());
        }
        {
            MemoryProfile mp = new MemoryProfile();
            mp.setAccessableObeyed(true);
            mp.setStaticFieldsIgnored(true);
            mp.add(cal);
            mp.build();
            System.out.printf(
                    "Calendar (accessable, non-static) size: %d bits, %d bytes.%n",
                    mp.getSizeBits(), mp.getSizeBytes());
        }
    }

    @Test
    public void testRepeatedAdds() throws Exception {
        Calendar cal = Calendar.getInstance();

        MemoryProfile mp = new MemoryProfile();
        mp.add(cal);
        mp.build();

        final long expectedSize = mp.getSizeBits();
        mp.add(cal);
        assertEquals(expectedSize, mp.getSizeBits());

    }

    @Test
    public void testRepeatedAdds2() throws Exception {
        Calendar cal = Calendar.getInstance();

        MemoryProfile mp = new MemoryProfile();
        mp.add(cal);
        mp.build();

        final long expectedSize = mp.getSizeBits();
        for (int i = 0; i < 10000; i++) {
            mp = new MemoryProfile();
            mp.add(cal);
            mp.build();
            assertEquals(expectedSize, mp.getSizeBits());
        }

    }
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
