package com.scheible.testgapanalysis.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.scheible.testgapanalysis.common.ToStringBuilder;
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

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof AnalysisResult) {
			final AnalysisResult other = (AnalysisResult) obj;
			return Objects.equals(coveredMethods, other.coveredMethods)
					&& Objects.equals(uncoveredMethods, other.uncoveredMethods)
					&& Objects.equals(emptyMethods, other.emptyMethods)
					&& Objects.equals(unresolvableMethods, other.unresolvableMethods)
					&& Objects.equals(ambiguouslyResolvedCoverage, other.ambiguouslyResolvedCoverage);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(coveredMethods, uncoveredMethods, emptyMethods, unresolvableMethods,
				ambiguouslyResolvedCoverage);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("coveredMethods", coveredMethods)
				.append("uncoveredMethods", uncoveredMethods).append("emptyMethods", emptyMethods)
				.append("unresolvableMethods", unresolvableMethods)
				.append("ambiguouslyResolvedCoverage", ambiguouslyResolvedCoverage).build();
	}
}
