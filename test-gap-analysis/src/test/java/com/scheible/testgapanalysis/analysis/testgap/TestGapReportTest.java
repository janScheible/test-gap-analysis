package com.scheible.testgapanalysis.analysis.testgap;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 *
 * @author sj
 */
public class TestGapReportTest {

	@Test
	public void testEmptyReportTestGap() {
		TestGapReport report = TestGapReport.builder().setWorkDir(".").setPreviousState("asbc")
				.setCurrentState(Optional.empty()).setJaCoCoReportFiles(emptySet()).setJaCoCoCoverageCount(0)
				.setNewOrChangedFiles(emptySet()).setCoveredMethods(emptySet()).setUncoveredMethods(emptySet())
				.setEmptyMethods(emptySet()).setUnresolvableMethods(emptySet())
				.setAmbiguouslyResolvedCoverage(emptyMap()).build();
		assertThat(report.getTestGap()).isNotNaN().isEqualTo(0.0d, offset(0.01));
	}
}
