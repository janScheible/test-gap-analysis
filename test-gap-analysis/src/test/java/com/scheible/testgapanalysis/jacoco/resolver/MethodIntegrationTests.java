package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.CoverageResolutionAssert.assertThat;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.METHOD;

import org.junit.jupiter.api.Test;

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

	public static class MultiLineParametersMethod {

		public void doIt(String value, //
				boolean flag) {
			"".trim();
		}
	}

	@Test
	public void testMultiLineParametersMethod() throws Exception {
		assertThat(resolve(MultiLineParametersMethod.class, METHOD)).isUnambiguouslyResolved();
	}
}
