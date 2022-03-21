package com.scheible.testgapanalysis;

import java.util.Collections;
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
	private final Set<ParsedMethod> unresolved;

	private final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguousCoverage;

	public DebugCoverageResolutionReport(final int coverageInfoCount, final Set<String> jaCoCoReportFiles,
			final int javaFileCount, final Map<ParsedMethod, MethodWithCoverageInfo> resolved,
			final Set<ParsedMethod> unresolved,
			final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguousCoverage) {
		this.coverageInfoCount = coverageInfoCount;
		this.jaCoCoReportFiles = Collections.unmodifiableSet(jaCoCoReportFiles);
		this.javaFileCount = javaFileCount;

		this.resolved = Collections.unmodifiableMap(resolved);
		this.unresolved = Collections.unmodifiableSet(unresolved);

		this.ambiguousCoverage = Collections.unmodifiableMap(ambiguousCoverage);
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

	public Set<ParsedMethod> getUnresolved() {
		return unresolved;
	}

	public Map<MethodWithCoverageInfo, Set<ParsedMethod>> getAmbiguousCoverage() {
		return ambiguousCoverage;
	}
}
