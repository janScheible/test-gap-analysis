package com.scheible.testgapanalysis.analysis;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class AnalysisResult {

	private final String oldCommitHash;
	private final Optional<String> newCommitHash;

	private final Set<String> newOrChangedFiles;

	private final Set<ParsedMethod> allNewOrChangedMethods;
	private final Map<ParsedMethod, MethodWithCoverageInfo> resolvedCoverage;
	private final Set<ParsedMethod> uncoveredMethods;
	private final Set<ParsedMethod> unresolvableMethods;

	public AnalysisResult(final String oldCommitHash, final Optional<String> newCommitHash,
			final Set<String> newOrChangedFiles, final Set<ParsedMethod> allNewOrChangedMethods,
			final Map<ParsedMethod, MethodWithCoverageInfo> resolvedCoverage, final Set<ParsedMethod> uncoveredMethods,
			final Set<ParsedMethod> unresolvableMethods) {
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;

		this.newOrChangedFiles = unmodifiableSet(newOrChangedFiles);

		this.allNewOrChangedMethods = unmodifiableSet(allNewOrChangedMethods);
		this.resolvedCoverage = unmodifiableMap(resolvedCoverage);
		this.uncoveredMethods = unmodifiableSet(uncoveredMethods);
		this.unresolvableMethods = unmodifiableSet(unresolvableMethods);
	}

	public String getOldCommitHash() {
		return oldCommitHash;
	}

	public Optional<String> getNewCommitHash() {
		return newCommitHash;
	}

	public Set<String> getNewOrChangedFiles() {
		return newOrChangedFiles;
	}

	public Set<ParsedMethod> getAllNewOrChangedMethods() {
		return allNewOrChangedMethods;
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getResolvedCoverage() {
		return resolvedCoverage;
	}

	public Set<ParsedMethod> getUncoveredMethods() {
		return uncoveredMethods;
	}

	public Set<ParsedMethod> getUnresolvableMethods() {
		return unresolvableMethods;
	}
}
