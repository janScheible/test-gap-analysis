package com.scheible.testgapanalysis.debug;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.scheible.testgapanalysis.common.ToStringBuilder;
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

	DebugCoverageResolutionReport(final BuilderImpl builder) {
		this.coverageInfoCount = builder.coverageInfoCount;
		this.jaCoCoReportFiles = Collections.unmodifiableSet(new HashSet<>(builder.jaCoCoReportFiles));
		this.javaFileCount = builder.javaFileCount;

		this.resolved = Collections.unmodifiableMap(new HashMap<>(builder.resolved));
		this.empty = Collections.unmodifiableSet(new HashSet<>(builder.empty));

		this.unresolved = Collections.unmodifiableSet(new HashSet<>(builder.unresolved));
		this.ambiguousCoverage = Collections.unmodifiableMap(new HashMap<>(builder.ambiguousCoverage));
	}

	public static CoverageInfoCountStep builder() {
		return new BuilderImpl();
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

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof DebugCoverageResolutionReport) {
			final DebugCoverageResolutionReport other = (DebugCoverageResolutionReport) obj;
			return coverageInfoCount == other.coverageInfoCount
					&& Objects.equals(jaCoCoReportFiles, other.jaCoCoReportFiles)
					&& javaFileCount == other.javaFileCount && Objects.equals(resolved, other.resolved)
					&& Objects.equals(empty, other.empty) && Objects.equals(unresolved, other.unresolved)
					&& Objects.equals(ambiguousCoverage, other.ambiguousCoverage);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(coverageInfoCount, jaCoCoReportFiles, javaFileCount, resolved, empty, unresolved,
				ambiguousCoverage);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("coverageInfoCount", coverageInfoCount)
				.append("jaCoCoReportFiles", jaCoCoReportFiles).append("javaFileCount", javaFileCount)
				.append("resolved", resolved).append("empty", empty).append("unresolved", unresolved)
				.append("ambiguousCoverage", ambiguousCoverage).build();
	}
}
