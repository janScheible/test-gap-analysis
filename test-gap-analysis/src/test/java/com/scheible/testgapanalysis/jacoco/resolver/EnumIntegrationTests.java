package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.CoverageResolutionAssert.assertThat;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.ENUM_CONSTRUCTOR;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class EnumIntegrationTests extends AbstractIntegrationTest {

	@Test
	public void testTopLevelEnum() throws Exception {
		assertThat(resolve(TopLevelEnum.class, ENUM_CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public static enum SimpleEnum {

		TEST;
	}

	@Test
	public void testSimpleEnum() throws Exception {
		assertThat(resolve(SimpleEnum.class)).isEmpty();
	}

	public static enum SimpleEnumWithNoArgsConstructor {

		TEST;

		private SimpleEnumWithNoArgsConstructor() {
			"".trim();
		}
	}

	@Test
	public void testSimpleEnumWithNoArgsConstructor() throws Exception {
		assertThat(resolve(SimpleEnumWithNoArgsConstructor.class, ENUM_CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public static enum EnumWithArgsConstructor {

		TEST(42);

		private final int value;

		private EnumWithArgsConstructor(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	@Test
	public void testEnumWithConstructor() throws Exception {
		assertThat(resolve(EnumWithArgsConstructor.class, ENUM_CONSTRUCTOR)).isUnambiguouslyResolved();
	}
}
