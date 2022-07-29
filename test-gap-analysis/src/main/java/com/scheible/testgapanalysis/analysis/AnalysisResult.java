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

	public AnalysisResult(Map<ParsedMethod, MethodWithCoverageInfo> coveredMethods,
			Map<ParsedMethod, MethodWithCoverageInfo> uncoveredMethods, Set<ParsedMethod> emptyMethods,
			Set<ParsedMethod> unresolvableMethods,
			Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguouslyResolvedCoverage) {
		this.coveredMethods = Collections.unmodifiableMap(new HashMap<>(coveredMethods));
		this.uncoveredMethods = Collections.unmodifiableMap(new HashMap<>(uncoveredMethods));

		this.emptyMethods = Collections.unmodifiableSet(new HashSet<>(emptyMethods));
		this.unresolvableMethods = Collections.unmodifiableSet(new HashSet<>(unresolvableMethods));
		this.ambiguouslyResolvedCoverage = Collections.unmodifiableMap(new HashMap<>(ambiguouslyResolvedCoverage));
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getCoveredMethods() {
		return this.coveredMethods;
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getUncoveredMethods() {
		return this.uncoveredMethods;
	}

	public Set<ParsedMethod> getEmptyMethods() {
		return this.emptyMethods;
	}

	public Set<ParsedMethod> getUnresolvableMethods() {
		return this.unresolvableMethods;
	}

	public Map<MethodWithCoverageInfo, Set<ParsedMethod>> getAmbiguouslyResolvedCoverage() {
		return this.ambiguouslyResolvedCoverage;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof AnalysisResult) {
			AnalysisResult other = (AnalysisResult) obj;
			return Objects.equals(this.coveredMethods, other.coveredMethods)
					&& Objects.equals(this.uncoveredMethods, other.uncoveredMethods)
					&& Objects.equals(this.emptyMethods, other.emptyMethods)
					&& Objects.equals(this.unresolvableMethods, other.unresolvableMethods)
					&& Objects.equals(this.ambiguouslyResolvedCoverage, other.ambiguouslyResolvedCoverage);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.coveredMethods, this.uncoveredMethods, this.emptyMethods, this.unresolvableMethods,
				this.ambiguouslyResolvedCoverage);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("coveredMethods", this.coveredMethods)
				.append("uncoveredMethods", this.uncoveredMethods).append("emptyMethods", this.emptyMethods)
				.append("unresolvableMethods", this.unresolvableMethods)
				.append("ambiguouslyResolvedCoverage", this.ambiguouslyResolvedCoverage).build();
	}
}
