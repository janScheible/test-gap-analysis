package com.scheible.testgapanalysis.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
public class ParsedMethodTest {

	@Test
	public void testTopLevelSimpleName() {
		assertThat(withTopLevelTypeFqn("CoverageTestClass").getTopLevelSimpleName()).isEqualTo("CoverageTestClass");
		assertThat(withTopLevelTypeFqn("foo.CoverageTestClass").getTopLevelSimpleName()).isEqualTo("CoverageTestClass");
	}

	@Test
	public void testEnclosingSimpleName() {
		assertThat(withTopLevelTypeFqn("foo.CoverageTestClass").getEnclosingSimpleName())
				.isEqualTo("CoverageTestClass");
		assertThat(withTopLevelTypeFqn("bar.CoverageTestClass", "InnerClass").getEnclosingSimpleName())
				.isEqualTo("InnerClass");
	}

	private static ParsedMethod withTopLevelTypeFqn(final String topLevelTypeFqn, final String... scope) {
		return new ParsedMethod(MethodType.CONSTRUCTOR, topLevelTypeFqn, Arrays.asList(scope), "", "",
				Arrays.asList(42), 0, false, 0);
	}
}
