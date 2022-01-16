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

	private final Set<MethodWithCoverageInfo> coverageInfo;
	private final Set<String> jaCoCoReportFiles;
	private final int javaFileCount;
	private final Map<ParsedMethod, MethodWithCoverageInfo> resolved;
	private final Set<ParsedMethod> unresolved;

	public DebugCoverageResolutionReport(final Set<MethodWithCoverageInfo> coverageInfo,
			final Set<String> jaCoCoReportFiles, final int javaFileCount,
			final Map<ParsedMethod, MethodWithCoverageInfo> resolved, final Set<ParsedMethod> unresolved) {
		this.coverageInfo = coverageInfo;
		this.jaCoCoReportFiles = Collections.unmodifiableSet(jaCoCoReportFiles);
		this.javaFileCount = javaFileCount;
		this.resolved = Collections.unmodifiableMap(resolved);
		this.unresolved = Collections.unmodifiableSet(unresolved);
	}

	public Set<MethodWithCoverageInfo> getCoverageInfo() {
		return coverageInfo;
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
}