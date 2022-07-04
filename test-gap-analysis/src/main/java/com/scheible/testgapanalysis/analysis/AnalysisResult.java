package com.scheible.testgapanalysis.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

	private final Set<ParsedMethod> emptyMethods;
	private final Set<ParsedMethod> unresolvableMethods;
	private final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguouslyResolvedCoverage;

	public AnalysisResult(final Map<ParsedMethod, MethodWithCoverageInfo> coveredMethods,
			final Map<ParsedMethod, MethodWithCoverageInfo> uncoveredMethods, final Set<ParsedMethod> emptyMethods,
			final Set<ParsedMethod> unresolvableMethods,
			final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguouslyResolvedCoverage) {
		this.coveredMethods = Collections.unmodifiableMap(new HashMap<>(coveredMethods));
		this.uncoveredMethods = Collections.unmodifiableMap(new HashMap<>(uncoveredMethods));

		this.emptyMethods = Collections.unmodifiableSet(new HashSet<>(emptyMethods));
		this.unresolvableMethods = Collections.unmodifiableSet(new HashSet<>(unresolvableMethods));
		this.ambiguouslyResolvedCoverage = Collections.unmodifiableMap(new HashMap<>(ambiguouslyResolvedCoverage));
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getCoveredMethods() {
		return coveredMethods;
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getUncoveredMethods() {
		return uncoveredMethods;
	}

	public Set<ParsedMethod> getEmptyMethods() {
		return emptyMethods;
	}

	public Set<ParsedMethod> getUnresolvableMethods() {
		return unresolvableMethods;
	}

	public Map<MethodWithCoverageInfo, Set<ParsedMethod>> getAmbiguouslyResolvedCoverage() {
		return ambiguouslyResolvedCoverage;
	}
}
