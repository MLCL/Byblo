package uk.ac.susx.mlcl.testing;

/**
 * Marker interface denoting tests that are likely to take a long time. These
 * can then be disabled in situations where testing shouldn't take too long, for
 * example when a user compiles the software.
 * <p />
 * Tests can be disabled in a number of ways:
 * 
 * <ul>
 * <li>Using maven we can configure surefire to exclude annotated tests:
 * 
 * <pre>
 * <plugin>
 *     &lt;artifactId&gt;maven-surefire-plugin&lt;/artifactId&gt;
 *     &lt;version&gt;2.11&lt;/version&gt;
 *     &lt;configuration&gt;
 *         &lt;excludedGroups&gt;uk.ac.susx.mlcl.SlowTestCategory&lt;/excludedGroups&gt;
 *     &lt;/configuration&gt;
 * </plugin>
 * </pre>
 * 
 * </li>
 * <li>Or invoking maver from the command line:
 * 
 * <pre>
 * $ mvn surefire:test -DexcludedGroups=uk.ac.susx.mlcl.SlowTestCategory
 * </pre>
 * 
 * </li>
 * 
 * <li>Finally, one can create a test suite that excludes specific categories:
 * 
 * <pre>
 * &#064;RunWith(Categories.class)
 * &#064;ExcludeCategory(SlowTestCategory.class)
 * &#064;SuiteClasses({ Class1Test.class, Class2Test.class })
 * public class UnitTestSuite {
 * }
 * </pre>
 * 
 * </li>
 * </ul>
 * 
 * 
 * What defines a "slow" test, rather depends on the test. Generally we should
 * think slowness as ratio between the expectation of test failure with the time
 * it takes to run.
 * 
 */
public interface SlowTestCategory {

}
