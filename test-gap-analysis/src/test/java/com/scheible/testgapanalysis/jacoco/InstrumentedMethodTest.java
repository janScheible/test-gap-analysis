package com.scheible.testgapanalysis.jacoco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 *
 * @author sj
 */
public class InstrumentedMethodTest {

	@Test
	public void testMerge() {
		assertThat(InstrumentedMethod.merge(Arrays.asList(new InstrumentedMethod("className", "name", "desc", 42, 1),
				new InstrumentedMethod("className", "name", "desc", 42, 2))))
						.isEqualTo(new InstrumentedMethod("className", "name", "desc", 42, 3));
	}

	@Test
	public void testMergeEmptySet() {
		assertThatThrownBy(() -> InstrumentedMethod.merge(Collections.emptySet()))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Can't merge a empty set");
	}

	@Test
	public void testMergeWithDifferentMethods() {
		assertThatThrownBy(() -> InstrumentedMethod.merge(Arrays.asList( //
				new InstrumentedMethod("className", "firstName", "desc", 42, 1),
				new InstrumentedMethod("className", "secondName", "desc", 42, 2))))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("can't be merged because they refer to different methods");
	}

	@Test
	public void testSimpleName() {
		assertThat(withClassName("CoverageTestClass").getSimpleClassName()).isEqualTo("CoverageTestClass");
		assertThat(withClassName("testgapanalysis/_test/CoverageTestClass$InnerStatic").getSimpleClassName())
				.isEqualTo("CoverageTestClass$InnerStatic");
	}

	@Test
	public void testEnclosingSimpleName() {
		assertThat(withClassName("testgapanalysis/_test/CoverageTestClass").getEnclosingSimpleName())
				.isEqualTo("CoverageTestClass");
		assertThat(withClassName("testgapanalysis/_test/CoverageTestClass$InnerStatic").getEnclosingSimpleName())
				.isEqualTo("InnerStatic");
		assertThat(withClassName("testgapanalysis/_test/CoverageTestClass$Foo$InnerStatic").getEnclosingSimpleName())
				.isEqualTo("InnerStatic");
	}

	private static InstrumentedMethod withClassName(String className) {
		return new InstrumentedMethod(className, "name", "", 0, 0);
	}
}
