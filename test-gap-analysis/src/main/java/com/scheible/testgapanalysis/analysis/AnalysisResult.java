package com.scheible.testgapanalysis.analysis;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.util.Map;
import java.util.Set;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class AnalysisResult {

	private final Map<ParsedMethod, MethodWithCoverageInfo> coveredMethods;
	private final Map<ParsedMethod, MethodWithCoverageInfo> uncoveredMethods;

	private final Set<ParsedMethod> unresolvableMethods;
	private final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguouslyResolvedCoverage;

	public AnalysisResult(final Map<ParsedMethod, MethodWithCoverageInfo> coveredMethods,
			final Map<ParsedMethod, MethodWithCoverageInfo> uncoveredMethods,
			final Set<ParsedMethod> unresolvableMethods,
			final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguouslyResolvedCoverage) {
		this.coveredMethods = unmodifiableMap(coveredMethods);
		this.uncoveredMethods = unmodifiableMap(uncoveredMethods);

		this.unresolvableMethods = unmodifiableSet(unresolvableMethods);
		this.ambiguouslyResolvedCoverage = unmodifiableMap(ambiguouslyResolvedCoverage);
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getCoveredMethods() {
		return coveredMethods;
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getUncoveredMethods() {
		return uncoveredMethods;
	}

	public Set<ParsedMethod> getUnresolvableMethods() {
		return unresolvableMethods;
	}

	public Map<MethodWithCoverageInfo, Set<ParsedMethod>> getAmbiguouslyResolvedCoverage() {
		return ambiguouslyResolvedCoverage;
	}
}
