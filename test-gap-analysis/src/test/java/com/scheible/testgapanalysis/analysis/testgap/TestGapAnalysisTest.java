package com.scheible.testgapanalysis.analysis.testgap;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.scheible.testgapanalysis.analysis.Analysis;
import com.scheible.testgapanalysis.git.GitRepoChangeScanner;
import com.scheible.testgapanalysis.jacoco.JaCoCoReportParser;
import com.scheible.testgapanalysis.parser.JavaParser;

/**
 *
 * @author sj
 */
public class TestGapAnalysisTest {

	@Test
	public void testRunTestGapAnalysisWithReferenceCommit() {
		// There are no (real) JaCoCo reports available at this time --> use the testing ones and just make sure that
		// it reads the methods correctly.
		TestGapAnalysis testGapAnalysis = createTestGapAnalysis();
		TestGapReport report = testGapAnalysis.run(new File("."), new File("./src/main/java"),
				JaCoCoReportParser.findJaCoCoReportFiles(new File("./target/test-classes")),
				Optional.of("756a25318e23bebace82f8317f3a57e43204901a"), Optional.empty(), Optional.empty());
		assertThat(report).isNotNull();
	}

	@Test
	public void testRunTestGapAnalysisWithWorkingCopyComparison() {
		// There are no (real) JaCoCo reports available at this time --> use the testing ones and just make sure that
		// it reads the methods correctly.
		TestGapAnalysis testGapAnalysis = createTestGapAnalysis();
		TestGapReport report = testGapAnalysis.run(new File("."), new File("./src/main/java"),
				JaCoCoReportParser.findJaCoCoReportFiles(new File("./target/test-classes")), Optional.empty(),
				Optional.empty(), Optional.empty());
		assertThat(report).isNotNull();
	}

	private static TestGapAnalysis createTestGapAnalysis() {
		return new TestGapAnalysis(new Analysis(new JavaParser()), new JaCoCoReportParser(),
				new GitRepoChangeScanner());
	}
}
