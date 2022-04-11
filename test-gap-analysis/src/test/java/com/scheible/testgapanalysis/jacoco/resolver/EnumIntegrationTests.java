package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.CoverageResolutionAssert.assertThat;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.CONSTRUCTOR;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class EnumIntegrationTests extends AbstractIntegrationTest {

	@Test
	public void testTopLevelEnum() throws Exception {
		assertThat(resolve(TopLevelEnum.class, CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public static enum SimpleEnum {

		TEST;
	}

	@Test
	public void testSimpleEnum() throws Exception {
		assertThat(resolve(SimpleEnum.class, CONSTRUCTOR)).isUnambiguouslyResolved();
	}
}
