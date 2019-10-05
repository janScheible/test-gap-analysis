package com.scheible.testgapanalysis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import com.scheible.pocketsaw.impl.Pocketsaw;
import com.scheible.pocketsaw.impl.descriptor.annotation.FastClasspathScanner;

/**
 *
 * @author sj
 */
public class TestGapAnalysisSubModuleTest {

	private static Pocketsaw.AnalysisResult result;

	@BeforeClass
	public static void beforeClass() {
		result = Pocketsaw.analizeCurrentProject(FastClasspathScanner.create(TestGapAnalysisSubModuleTest.class));
	}

	@Test
	public void testNoDescriptorCycle() {
		assertThat(result.getAnyDescriptorCycle()).isEmpty();
	}

	@Test
	public void testNoCodeCycle() {
		assertThat(result.getAnyCodeCycle()).isEmpty();
	}

	@Test
	public void testNoIllegalCodeDependencies() {
		assertThat(result.getIllegalCodeDependencies()).isEmpty();
	}
}
