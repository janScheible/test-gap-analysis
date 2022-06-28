package com.scheible.testgapanalysis.debug;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.util.Map;
import java.util.Set;

import com.scheible.testgapanalysis.debug.DebugCoverageResolutionReportBuilder.BuilderImpl;
import com.scheible.testgapanalysis.debug.DebugCoverageResolutionReportBuilder.CoverageInfoCountStep;
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

	public static CoverageInfoCountStep builder() {
		return new BuilderImpl();
	}

	DebugCoverageResolutionReport(final BuilderImpl builder) {
		this.coverageInfoCount = builder.coverageInfoCount;
		this.jaCoCoReportFiles = unmodifiableSet(builder.jaCoCoReportFiles);
		this.javaFileCount = builder.javaFileCount;

		this.resolved = unmodifiableMap(builder.resolved);
		this.empty = unmodifiableSet(builder.empty);

		this.unresolved = unmodifiableSet(builder.unresolved);
		this.ambiguousCoverage = unmodifiableMap(builder.ambiguousCoverage);
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
