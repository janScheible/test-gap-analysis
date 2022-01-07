package com.scheible.testgapanalysis.analysis;

import static java.util.Collections.unmodifiableSet;

import java.util.Set;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class AnalysisResult {

	private final Set<ParsedMethod> newOrChangedMethods;
	private final Set<MethodWithCoverageInfo> coverageInfo;
	private final Set<ParsedMethod> uncoveredNewOrChangedMethods;

	public AnalysisResult(final Set<ParsedMethod> newOrChangedMethods, final Set<MethodWithCoverageInfo> coverageInfo,
			final Set<ParsedMethod> uncoveredNewOrChangedMethods) {
		this.newOrChangedMethods = unmodifiableSet(newOrChangedMethods);
		this.coverageInfo = unmodifiableSet(coverageInfo);
		this.uncoveredNewOrChangedMethods = unmodifiableSet(uncoveredNewOrChangedMethods);
	}

	public Set<ParsedMethod> getNewOrChangedMethods() {
		return newOrChangedMethods;
	}

	public Set<MethodWithCoverageInfo> getCoverageInfo() {
		return coverageInfo;
	}

	public Set<ParsedMethod> getUncoveredNewOrChangedMethods() {
		return uncoveredNewOrChangedMethods;
	}
}
