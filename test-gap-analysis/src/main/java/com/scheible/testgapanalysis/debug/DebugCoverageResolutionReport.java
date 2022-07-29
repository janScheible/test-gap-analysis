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

	DebugCoverageResolutionReport(BuilderImpl builder) {
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
		return this.coverageInfoCount;
	}

	public Set<String> getJaCoCoReportFiles() {
		return this.jaCoCoReportFiles;
	}

	public int getJavaFileCount() {
		return this.javaFileCount;
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getResolved() {
		return this.resolved;
	}

	public Set<ParsedMethod> getEmpty() {
		return this.empty;
	}

	public Set<ParsedMethod> getUnresolved() {
		return this.unresolved;
	}

	public Map<MethodWithCoverageInfo, Set<ParsedMethod>> getAmbiguousCoverage() {
		return this.ambiguousCoverage;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof DebugCoverageResolutionReport) {
			DebugCoverageResolutionReport other = (DebugCoverageResolutionReport) obj;
			return this.coverageInfoCount == other.coverageInfoCount
					&& Objects.equals(this.jaCoCoReportFiles, other.jaCoCoReportFiles)
					&& this.javaFileCount == other.javaFileCount && Objects.equals(this.resolved, other.resolved)
					&& Objects.equals(this.empty, other.empty) && Objects.equals(this.unresolved, other.unresolved)
					&& Objects.equals(this.ambiguousCoverage, other.ambiguousCoverage);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.coverageInfoCount, this.jaCoCoReportFiles, this.javaFileCount, this.resolved,
				this.empty, this.unresolved, this.ambiguousCoverage);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("coverageInfoCount", this.coverageInfoCount)
				.append("jaCoCoReportFiles", this.jaCoCoReportFiles).append("javaFileCount", this.javaFileCount)
				.append("resolved", this.resolved).append("empty", this.empty).append("unresolved", this.unresolved)
				.append("ambiguousCoverage", this.ambiguousCoverage).build();
	}
}
