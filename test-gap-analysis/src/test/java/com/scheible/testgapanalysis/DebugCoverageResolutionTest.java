package com.scheible.testgapanalysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import com.scheible.testgapanalysis.jacoco.JaCoCoHelper;

/**
 *
 * @author sj
 */
public class DebugCoverageResolutionTest {

	@Test
	public void testRun() {
		// There are no (real) JaCoCo reports available at this time --> use the testing ones and just make sure that
		// it reads the methods correctly.
		assertThat(DebugCoverageResolution.run(new File("."), new File("./src/main/java"),
				JaCoCoHelper.findJaCoCoReportFiles(new File("./target/test-classes")))).isNotNull();
	}
}
