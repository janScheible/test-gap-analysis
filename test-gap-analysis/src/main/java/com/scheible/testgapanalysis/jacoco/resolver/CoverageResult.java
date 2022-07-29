package com.scheible.testgapanalysis.jacoco.resolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis.common.ToStringBuilder;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class CoverageResult {

	private final Map<ParsedMethod, MethodWithCoverageInfo> resolvedMethods = new HashMap<>();
	private final Set<ParsedMethod> emptyMethods = new HashSet<>();
	private final Set<ParsedMethod> unresolvedMethods = new HashSet<>();
	private final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguousCoverage = new HashMap<>();

	public CoverageResult() {
	}

	public CoverageResult(final Map<ParsedMethod, MethodWithCoverageInfo> resolved,
			final Set<ParsedMethod> unresolved) {
		add(resolved, unresolved);
	}

	public static CoverageResult ofEmptyMethods(final Set<ParsedMethod> allMethods) {
		final CoverageResult result = new CoverageResult();
		allMethods.stream().filter(ParsedMethod::isEmpty).forEach(result.emptyMethods::add);
		return result;
	}

	private void add(final Map<ParsedMethod, MethodWithCoverageInfo> resolved, final Set<ParsedMethod> unresolved) {
		this.resolvedMethods.putAll(resolved);
		this.unresolvedMethods.addAll(unresolved);

		for (final Entry<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguouslyResolvedCoverage : findAmbiguouslyResolvedCoverage(
				this.resolvedMethods).entrySet()) {
			ambiguouslyResolvedCoverage.getValue().stream().forEach(this.resolvedMethods::remove);

			this.unresolvedMethods.addAll(ambiguouslyResolvedCoverage.getValue());

			this.ambiguousCoverage.computeIfAbsent(ambiguouslyResolvedCoverage.getKey(), key -> new HashSet<>())
					.addAll(ambiguouslyResolvedCoverage.getValue());
		}
	}

	void add(final CoverageResult result) {
		add(result.resolvedMethods, result.unresolvedMethods);
	}

	/**
	 * Creates a map of all methods that are ambiguously resolved (excluding (static) initializers). The reason
	 * that (static) initalizers are ignored is their special handling. See {@code CoverageResolver} for details.
	 */
	private static Map<MethodWithCoverageInfo, Set<ParsedMethod>> findAmbiguouslyResolvedCoverage(
			final Map<ParsedMethod, MethodWithCoverageInfo> resolved) {
		return resolved.entrySet().stream()
				.filter(e -> !(e.getKey().isInitializer() || e.getKey().isStaticInitializer()))
				.collect(Collectors.groupingBy(Entry::getValue)).entrySet().stream()
				.filter(e -> e.getValue().size() > 1).collect(Collectors.toMap(Entry::getKey,
						e -> e.getValue().stream().map(Entry::getKey).collect(Collectors.toSet())));
	}

	public boolean contains(final ParsedMethod method) {
		return this.emptyMethods.contains(method) || this.resolvedMethods.containsKey(method)
				|| this.unresolvedMethods.contains(method)
				|| this.ambiguousCoverage.entrySet().stream().anyMatch(e -> e.getValue().contains(method));
	}

	public boolean isEmpty() {
		return this.emptyMethods.isEmpty() && this.resolvedMethods.isEmpty() && this.unresolvedMethods.isEmpty()
				&& this.ambiguousCoverage.isEmpty();
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getResolvedMethods() {
		return Collections.unmodifiableMap(this.resolvedMethods);
	}

	public Set<ParsedMethod> getEmptyMethods() {
		return Collections.unmodifiableSet(this.emptyMethods);
	}

	public Set<ParsedMethod> getUnresolvedMethods() {
		return Collections.unmodifiableSet(this.unresolvedMethods);
	}

	public Map<MethodWithCoverageInfo, Set<ParsedMethod>> getAmbiguousCoverage() {
		return Collections.unmodifiableMap(this.ambiguousCoverage);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof CoverageResult) {
			final CoverageResult other = (CoverageResult) obj;
			return Objects.equals(this.resolvedMethods, other.resolvedMethods)
					&& Objects.equals(this.emptyMethods, other.emptyMethods)
					&& Objects.equals(this.unresolvedMethods, other.unresolvedMethods)
					&& Objects.equals(this.ambiguousCoverage, other.ambiguousCoverage);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.resolvedMethods, this.emptyMethods, this.unresolvedMethods, this.ambiguousCoverage);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("resolvedMethods", this.resolvedMethods)
				.append("emptyMethods", this.emptyMethods).append("unresolvedMethods", this.unresolvedMethods)
				.append("ambiguousCoverage", this.ambiguousCoverage).build();
	}
}
