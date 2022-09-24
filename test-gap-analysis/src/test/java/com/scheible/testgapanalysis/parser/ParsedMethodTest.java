package com.scheible.testgapanalysis.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

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

	private static ParsedMethod withTopLevelTypeFqn(String topLevelTypeFqn, String... scope) {
		return ParsedMethod.builder().setMethodType(MethodType.CONSTRUCTOR).setTopLevelTypeFqn(topLevelTypeFqn)
				.setScope(Arrays.asList(scope)).setName("").setRelevantCode("").setCodeLines(Arrays.asList(42))
				.setCodeColumn(0).setEmpty(false).setArgumentCount(0).build();
	}
}
