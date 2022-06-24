package com.scheible.testgapanalysis.debug;

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
public class DebugCoverageResolutionReport {

	private final int coverageInfoCount;
	private final Set<String> jaCoCoReportFiles;
	private final int javaFileCount;

	private final Map<ParsedMethod, MethodWithCoverageInfo> resolved;
	private final Set<ParsedMethod> empty;

	private final Set<ParsedMethod> unresolved;
	private final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguousCoverage;

	public DebugCoverageResolutionReport(final int coverageInfoCount, final Set<String> jaCoCoReportFiles,
			final int javaFileCount, final Map<ParsedMethod, MethodWithCoverageInfo> resolved,
			final Set<ParsedMethod> empty, final Set<ParsedMethod> unresolved,
			final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguousCoverage) {
		this.coverageInfoCount = coverageInfoCount;
		this.jaCoCoReportFiles = unmodifiableSet(jaCoCoReportFiles);
		this.javaFileCount = javaFileCount;

		this.resolved = unmodifiableMap(resolved);
		this.empty = unmodifiableSet(empty);

		this.unresolved = unmodifiableSet(unresolved);
		this.ambiguousCoverage = unmodifiableMap(ambiguousCoverage);
	}

	public int getCoverageInfoCount() {
		return coverageInfoCount;
	}

	public Set<String> getJaCoCoReportFiles() {
		return jaCoCoReportFiles;
	}

	public int getJavaFileCount() {
		return javaFileCount;
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getResolved() {
		return resolved;
	}

	public Set<ParsedMethod> getEmpty() {
		return empty;
	}

	public Set<ParsedMethod> getUnresolved() {
		return unresolved;
	}

	public Map<MethodWithCoverageInfo, Set<ParsedMethod>> getAmbiguousCoverage() {
		return ambiguousCoverage;
	}
}
