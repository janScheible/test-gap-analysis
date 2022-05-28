package com.scheible.testgapanalysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

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
		final DebugCoverageResolution debugCoverageResolution = new DebugCoverageResolution(new JavaParser(),
				new JaCoCoReportParser());
		DebugCoverageResolutionReport xxx;
		assertThat(xxx = debugCoverageResolution.run(new File("D:\\third-party\\commons-collections"),
				new File("D:\\third-party\\commons-collections\\src\\main\\java"),
				JaCoCoReportParser
						.findJaCoCoReportFiles(new File("D:\\third-party\\commons-collections\\target\\site"))))
								.isNotNull();
		"".trim();
	}
}
