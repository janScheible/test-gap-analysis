package com.scheible.testgapanalysis.debug;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.scheible.testgapanalysis.jacoco.JaCoCoReportParser;
import com.scheible.testgapanalysis.parser.JavaParser;

/**
 *
 * @author sj
 */
public class DebugCoverageResolutionTest {

	@Test
	public void testRun() {
		// There are no (real) JaCoCo reports available at this time --> use the testing ones and just make sure that
		// it reads the methods correctly.
		DebugCoverageResolution debugCoverageResolution = new DebugCoverageResolution(new JavaParser(),
				new JaCoCoReportParser());
		assertThat(debugCoverageResolution.run(new File("."), new File("./src/main/java"),
				JaCoCoReportParser.findJaCoCoReportFiles(new File("./target/test-classes")))).isNotNull();
	}
}
