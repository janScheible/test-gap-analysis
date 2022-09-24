package com.scheible.testgapanalysis.parser;

import static java.util.Collections.emptyList;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.javaparser.Position;
import com.github.javaparser.Range;

/**
 *
 * @author sj
 */
public class MaskUtilsTest {

	private static final String TEST_CLASS = "" //
			+ "class TestClass {\n" + "	\n" //
			+ "  void test() {\n" //
			+ "    // comment\n" //
			+ "    doIt();\n" //
			+ "    other(() -> \":-)\");\n" //
			+ "  }\n" //
			+ "}";

	private static final Range TEST_METHOD_RANGE = new Range(new Position(3, 1), new Position(7, 3));

	@Test
	public void testNoMasks() {
		assertThat(MaskUtils.apply(TEST_CLASS, TEST_METHOD_RANGE, emptyList())).startsWith("void test()")
				.endsWith("}\n");
	}

	@Test
	public void testSingleMask() {
		assertThat(MaskUtils.apply(TEST_CLASS, TEST_METHOD_RANGE,
				Arrays.asList(new Range(new Position(3, 8), new Position(3, 11))), true)).startsWith("void ####()")
						.endsWith("}\n");
	}

	@Test
	public void testTwoOverlappingMasks() {
		assertThat(MaskUtils.apply(TEST_CLASS, TEST_METHOD_RANGE,
				Arrays.asList(new Range(new Position(3, 8), new Position(3, 10)),
						new Range(new Position(3, 9), new Position(3, 11))),
				true)).startsWith("void ####()").endsWith("}\n");
	}

	@Test
	public void testMultilineMask() {
		assertThat(MaskUtils.apply(TEST_CLASS, TEST_METHOD_RANGE,
				Arrays.asList(new Range(new Position(3, 8), new Position(4, 5))), true))
						.startsWith("void ########\n" + "#####/ comment").endsWith("}\n");
	}

}
