package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.METHOD;
import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat(resolve(SimpleMethod.class, METHOD).getUnresolvedMethods()).isEmpty();
	}

	public static class MultiLineArgumentsMethod {

		public void doIt(final String value, //
				final boolean flag) {
			"".trim();
		}
	}

	@Test
	public void testMultiLineArgumentsMethod() throws Exception {
		assertThat(resolve(MultiLineArgumentsMethod.class, METHOD).getUnresolvedMethods()).isEmpty();
	}
}
