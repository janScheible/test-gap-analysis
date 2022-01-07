package com.scheible.testgapanalysis.jacoco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class MethodWithCoverageInfoTest {

	@Test
	public void testMerge() {
		assertThat(MethodWithCoverageInfo
				.merge(Arrays.asList(new MethodWithCoverageInfo("className", "name", "desc", 42, 1),
						new MethodWithCoverageInfo("className", "name", "desc", 42, 2))))
								.isEqualTo(new MethodWithCoverageInfo("className", "name", "desc", 42, 3));
	}

	@Test
	public void testMergeEmptySet() {
		assertThatThrownBy(() -> MethodWithCoverageInfo.merge(Collections.emptySet()))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Can't merge a empty set");
	}

	@Test
	public void testMergeWithDifferentMethods() {
		assertThatThrownBy(() -> MethodWithCoverageInfo.merge(Arrays.asList( //
				new MethodWithCoverageInfo("className", "firstName", "desc", 42, 1),
				new MethodWithCoverageInfo("className", "secondName", "desc", 42, 2))))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("can't be merged because the refer to different methods");
	}

	@Test
	public void testLevel() {
		assertThat(withClassName("testgapanalysis/_test/CoverageTestClass").getLevel()).isEqualTo(0);
		assertThat(withClassName("testgapanalysis/_test/CoverageTestClass$InnerStatic").getLevel()).isEqualTo(1);
	}

	private MethodWithCoverageInfo withClassName(final String className) {
		return new MethodWithCoverageInfo(className, "name", "", 0, 0);
	}
}