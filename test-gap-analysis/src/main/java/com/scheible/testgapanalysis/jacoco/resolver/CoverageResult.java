package com.scheible.testgapanalysis.jacoco.resolver;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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

	public Map<ParsedMethod, MethodWithCoverageInfo> getResolvedMethods() {
		return unmodifiableMap(resolvedMethods);
	}

	public Set<ParsedMethod> getEmptyMethods() {
		return unmodifiableSet(emptyMethods);
	}

	public Set<ParsedMethod> getUnresolvedMethods() {
		return unmodifiableSet(unresolvedMethods);
	}

	public Map<MethodWithCoverageInfo, Set<ParsedMethod>> getAmbiguousCoverage() {
		return unmodifiableMap(ambiguousCoverage);
	}

	public boolean contains(final ParsedMethod method) {
		return emptyMethods.contains(method) || resolvedMethods.containsKey(method)
				|| unresolvedMethods.contains(method)
				|| ambiguousCoverage.entrySet().stream().anyMatch(e -> e.getValue().contains(method));
	}

	public boolean isEmpty() {
		return emptyMethods.isEmpty() && resolvedMethods.isEmpty() && unresolvedMethods.isEmpty()
				&& ambiguousCoverage.isEmpty();
	}
}
