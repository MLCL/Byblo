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
package uk.ac.susx.mlcl.testing;

/**
 * Marker interface denoting tests that are likely to take a long time. These
 * can then be disabled in situations where testing shouldn't take too long, for
 * example when a user compiles the software.
 * <p/>
 * Tests can be disabled in a number of ways:
 * <p/>
 * <ul>
 * <li>Using maven we can configure surefire to exclude annotated tests:
 * <p/>
 * <pre>
 * <plugin>
 *     &lt;artifactId&gt;maven-surefire-plugin&lt;/artifactId&gt;
 *     &lt;version&gt;2.11&lt;/version&gt;
 *     &lt;configuration&gt;
 *         &lt;excludedGroups&gt;uk.ac.susx.mlcl.SlowTestCategory&lt;/excludedGroups&gt;
 *     &lt;/configuration&gt;
 * </plugin>
 * </pre>
 * <p/>
 * </li>
 * <li>Or invoking maven from the command line:
 * <p/>
 * <pre>
 * $ mvn surefire:test -DexcludedGroups=uk.ac.susx.mlcl.SlowTestCategory
 * </pre>
 * <p/>
 * </li>
 * <p/>
 * <li>Finally, one can create a test suite that excludes specific categories:
 * <p/>
 * <pre>
 * &#064;RunWith(Categories.class)
 * &#064;ExcludeCategory(SlowTestCategory.class)
 * &#064;SuiteClasses({ Class1Test.class, Class2Test.class })
 * public class UnitTestSuite {
 * }
 * </pre>
 * <p/>
 * </li>
 * </ul>
 * <p/>
 * <p/>
 * What defines a "slow" test, rather depends on the test. Generally we should
 * think slowness as ratio between the expectation of test failure with the time
 * it takes to run.
 */
public interface SlowTestCategory {

}
