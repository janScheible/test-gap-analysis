package com.scheible.testgapanalysis;

import java.io.File;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class DebugCoverageResolutionTest {

	@Test
	public void testRun() {
		DebugCoverageResolution.run(new File(".").getAbsoluteFile());
	}
}
