package com.scheible.testgapanalysis.analysis.testgap;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import java.util.Optional;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class TestGapReportTest {

	@Test
	public void testEmptyReportCoverageRatio() {
		final TestGapReport report = new TestGapReport(".", "asbc", Optional.empty(), emptySet(), 0, emptySet(),
				emptySet(), emptySet(), emptySet(), emptySet(), emptyMap());
		assertThat(report.getCoverageRatio()).isNotNaN().isEqualTo(1.0d, offset(0.01));
	}
}
