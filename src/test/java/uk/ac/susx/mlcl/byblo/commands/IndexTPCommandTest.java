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
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.byblo.commands;

import static uk.ac.susx.mlcl.TestConstants.DEFAULT_CHARSET;
import static uk.ac.susx.mlcl.TestConstants.TEST_FRUIT_INPUT;
import static uk.ac.susx.mlcl.TestConstants.TEST_OUTPUT_DIR;
import static uk.ac.susx.mlcl.TestConstants.assertSizeGT;
import static uk.ac.susx.mlcl.TestConstants.assertValidInputFiles;
import static uk.ac.susx.mlcl.TestConstants.assertValidJDBCInputFiles;
import static uk.ac.susx.mlcl.TestConstants.assertValidJDBCOutputFiles;
import static uk.ac.susx.mlcl.TestConstants.assertValidOutputFiles;
import static uk.ac.susx.mlcl.TestConstants.assertValidPlaintextInputFiles;
import static uk.ac.susx.mlcl.TestConstants.deleteIfExist;
import static uk.ac.susx.mlcl.TestConstants.deleteJDBCIfExist;
import static uk.ac.susx.mlcl.TestConstants.suffix;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.TestConstants.InfoProgressListener;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;

/**
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class IndexTPCommandTest extends
		AbstractCommandTest<IndexingCommands.IndexInstances> {

	@Override
	public Class<? extends IndexingCommands.IndexInstances> getImplementation() {
		return IndexingCommands.IndexInstances.class;
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testRunOnFruitAPI() throws Exception {
		System.out.println("Testing " + IndexTPCommandTest.class.getName()
				+ " on " + TEST_FRUIT_INPUT);

		final String name = TEST_FRUIT_INPUT.getName();
		final File out = new File(TEST_OUTPUT_DIR, name + ".indexed");
		final File idx1 = new File(TEST_OUTPUT_DIR, name + ".entry-index");
		final File idx2 = new File(TEST_OUTPUT_DIR, name + ".feature-index");

		deleteIfExist(out, idx1, idx2);

		indexTP(TEST_FRUIT_INPUT, out, idx1, idx2, EnumeratorType.Memory,
				false, false, true);

		// assertTrue(format(
		// "Output entries file \"{0}\" differs from expected file \"{1}\".",
		// out, TEST_FRUIT_INPUT_INDEXED),
		// TokenPairSource.equal(out, TEST_FRUIT_INPUT_INDEXED,
		// DEFAULT_CHARSET, false, false));

		// XXX: The files can be out of order
		// assertTrue("Output features file differs from test data file.",
		// Files.equal(idx1, TEST_FRUIT_ENTRY_INDEX));
		// assertTrue("Output entry/features file differs from test data file.",
		// Files.equal(idx2, TEST_FRUIT_FEATURE_INDEX));

		File out2 = suffix(out, ".unindexed");
		unindexTP(out, out2, idx1, idx2, EnumeratorType.Memory, false, false,
				true);

		// TokenPairSource.equal(out, out2, DEFAULT_CHARSET, false, false);

	}

	@Test
	public void testRunOnFruitAPI_skipboth_compact() throws Exception {
		testRunOnFruitAPI("compact-skipboth-", EnumeratorType.Memory, true,
				true, true);
	}

	@Test
	public void testRunOnFruitAPI_skipleft_compact() throws Exception {
		testRunOnFruitAPI("compact-skipleft-", EnumeratorType.Memory, true,
				false, true);
	}

	@Test
	public void testRunOnFruitAPI_skipright_compact() throws Exception {
		testRunOnFruitAPI("compact-skipright-", EnumeratorType.Memory, false,
				true, true);
	}

	@Test
	public void testRunOnFruitAPI_skipboth_verbose() throws Exception {
		testRunOnFruitAPI("verbose-skipboth-", EnumeratorType.Memory, true,
				true, false);
	}

	@Test
	public void testRunOnFruitAPI_skipleft_verbose() throws Exception {
		testRunOnFruitAPI("verbose-skipleft-", EnumeratorType.Memory, true,
				false, false);
	}

	@Test
	public void testRunOnFruitAPI_skipright_verbose() throws Exception {
		testRunOnFruitAPI("verbose-skipright-", EnumeratorType.Memory, false,
				true, false);
	}

	@Test
	public void testRunOnFruitAPI_skipboth_compact_JDBC() throws Exception {
		testRunOnFruitAPI("compact-skipboth-jdbc-", EnumeratorType.JDBC, true,
				true, true);
	}

	@Test
	public void testRunOnFruitAPI_skipleft_compact_JDBC() throws Exception {
		testRunOnFruitAPI("compact-skipleft-jdbc-", EnumeratorType.JDBC, true,
				false, true);
	}

	@Test
	public void testRunOnFruitAPI_skipright_compact_JDBC() throws Exception {
		testRunOnFruitAPI("compact-skipright-jdbc-", EnumeratorType.JDBC,
				false, true, true);
	}

	@Test
	public void testRunOnFruitAPI_skipboth_verbose_JDBC() throws Exception {
		testRunOnFruitAPI("verbose-skipboth-jdbc-", EnumeratorType.JDBC, true,
				true, false);
	}

	@Test
	public void testRunOnFruitAPI_skipleft_verbose_JDBC() throws Exception {
		testRunOnFruitAPI("verbose-skipleft-jdbc-", EnumeratorType.JDBC, true,
				false, false);
	}

	@Test
	public void testRunOnFruitAPI_skipright_verbose_JDBC() throws Exception {
		testRunOnFruitAPI("verbose-skipright-jdbc-", EnumeratorType.JDBC,
				false, true, false);
	}

	public void testRunOnFruitAPI(String prefix, EnumeratorType type,
			boolean skip1, boolean skip2, boolean compact) throws Exception {
		System.out.println("Testing " + IndexTPCommandTest.class.getName()
				+ " on " + TEST_FRUIT_INPUT);

		final String name = TEST_FRUIT_INPUT.getName();
		final File out = new File(TEST_OUTPUT_DIR, prefix + name + ".indexed");
		File out2 = suffix(out, ".unindexed");
		final File idx1 = new File(TEST_OUTPUT_DIR, name + ".entry-index");
		final File idx2 = new File(TEST_OUTPUT_DIR, name + ".feature-index");

		deleteIfExist(out, idx1, idx2);

		indexTP(TEST_FRUIT_INPUT, out, idx1, idx2, type, skip1, skip2, compact);

		unindexTP(out, out2, idx1, idx2, type, skip1, skip2, compact);

		// TokenPairSource.equal(out, out2, DEFAULT_CHARSET, skip1, skip2);

	}

	public static void indexTP(File from, File to, File index1, File index2,
			EnumeratorType type, boolean skip1, boolean skip2, boolean compact)
			throws Exception {
		assertValidPlaintextInputFiles(from);
		assertValidOutputFiles(to);
		if (type == EnumeratorType.JDBC)
			assertValidJDBCOutputFiles(index1, index2);
		else
			assertValidOutputFiles(index1, index2);

		IndexingCommands.IndexInstances unindex = new IndexingCommands.IndexInstances();
		unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
		unindex.getFilesDeligate().setSourceFile(from);
		unindex.getFilesDeligate().setDestinationFile(to);
		unindex.setIndexDeligate(new DoubleEnumeratingDeligate(type, true,
				true, index1, index2));
		unindex.runCommand();

		assertValidPlaintextInputFiles(to);

		if (type == EnumeratorType.JDBC)
			assertValidJDBCInputFiles(index1, index2);
		else
			assertValidInputFiles(index1, index2);
		assertSizeGT(from, to);
	}

	public static void unindexTP(File from, File to, File index1, File index2,
			EnumeratorType type, boolean skip1, boolean skip2, boolean compact)
			throws Exception {
		assertValidPlaintextInputFiles(from);
		if (type == EnumeratorType.JDBC)
			assertValidJDBCInputFiles(index1, index2);
		else
			assertValidInputFiles(index1, index2);
		assertValidOutputFiles(to);

		IndexingCommands.IndexInstances unindex = new IndexingCommands.IndexInstances();
		unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
		unindex.getFilesDeligate().setSourceFile(from);
		unindex.getFilesDeligate().setDestinationFile(to);

		unindex.setIndexDeligate(new DoubleEnumeratingDeligate(type, true,
				true, index1, index2));
		unindex.runCommand();

		assertValidPlaintextInputFiles(to);
		assertSizeGT(to, from);
	}

	@Test
	@Ignore(value = "Takes a rather a long time.")
	public void testEnumerateInstancesOnWorstCaseData() throws Exception {
		System.out.println("testEnumerateInstancesOnWorstCaseData()");

		// worst case is that you never see an entry of feature twice
		final int nEntries = 1 << 12;
		final int nFeaturesPerEntry = 1 << 12;

		final File inFile = new File(TEST_OUTPUT_DIR, String.format(
				"testEnumerateInstancesOnWorstCaseData-%dx%d-instances",
				nEntries, nFeaturesPerEntry));

		// Create the test data if necessary
		if (!inFile.exists())
			TestConstants.generateUniqueInstanceData(inFile, nEntries,
					nFeaturesPerEntry);

		File outFile = new File(TEST_OUTPUT_DIR, inFile.getName()
				+ "-enumerated");
		File entryIdx = new File(TEST_OUTPUT_DIR, inFile.getName()
				+ "-entry-index");
		File featureIdx = new File(TEST_OUTPUT_DIR, inFile.getName()
				+ "-feature-index");

		assertValidPlaintextInputFiles(inFile);
		assertValidOutputFiles(outFile);
		assertValidJDBCOutputFiles(entryIdx, featureIdx);

		deleteIfExist(outFile);
		deleteJDBCIfExist(entryIdx, featureIdx);

		final DoubleEnumeratingDeligate ded = new DoubleEnumeratingDeligate(
				EnumeratorType.JDBC, true, true, entryIdx, featureIdx);

		IndexingCommands.IndexInstances unindex = new IndexingCommands.IndexInstances();
		unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
		unindex.getFilesDeligate().setSourceFile(inFile);
		unindex.getFilesDeligate().setDestinationFile(outFile);
		unindex.setIndexDeligate(ded);

		unindex.addProgressListener(new InfoProgressListener());

		unindex.runCommand();

		ded.closeEnumerator();

		assertValidPlaintextInputFiles(outFile);
		assertValidJDBCInputFiles(entryIdx, featureIdx);
	}

}
