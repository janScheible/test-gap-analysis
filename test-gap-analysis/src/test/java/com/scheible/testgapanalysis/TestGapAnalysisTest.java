package com.scheible.testgapanalysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Optional;

import org.junit.Test;

import com.scheible.testgapanalysis.jacoco.JaCoCoHelper;

/**
 *
 * @author sj
 */
public class TestGapAnalysisTest {

	@Test
	public void testRunTestGapAnalysisWithReferenceCommit() {
		// There are no (real) JaCoCo reports available at this time --> use the testing ones and just make sure that
		// it reads the methods correctly.
		final TestGapReport report = TestGapAnalysis.run(new File("."),
				JaCoCoHelper.findJaCoCoReportFiles(new File("./target/test-classes")),
				Optional.of("756a25318e23bebace82f8317f3a57e43204901a"));
		assertThat(report).isNotNull();
	}

	@Test
	public void testRunTestGapAnalysisWithWorkingCopyComparison() {
		// There are no (real) JaCoCo reports available at this time --> use the testing ones and just make sure that
		// it reads the methods correctly.
		final TestGapReport report = TestGapAnalysis.run(new File("."),
				JaCoCoHelper.findJaCoCoReportFiles(new File("./target/test-classes")), Optional.empty());
		assertThat(report).isNotNull();
	}
}
