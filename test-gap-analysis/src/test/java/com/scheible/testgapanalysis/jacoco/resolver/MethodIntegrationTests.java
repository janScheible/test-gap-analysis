package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.CoverageResolutionAssert.assertThat;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.METHOD;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class MethodIntegrationTests extends AbstractIntegrationTest {

	public static class SimpleMethod {

		public void doIt() {
			"".trim();
		}
	}

	@Test
	public void testSimpleMethod() throws Exception {
		assertThat(resolve(SimpleMethod.class, METHOD)).isUnambiguouslyResolved();
	}

	public static class MultiLineArgumentsMethod {

		public void doIt(String value, //
				boolean flag) {
			"".trim();
		}
	}

	@Test
	public void testMultiLineArgumentsMethod() throws Exception {
		assertThat(resolve(MultiLineArgumentsMethod.class, METHOD)).isUnambiguouslyResolved();
	}
}
