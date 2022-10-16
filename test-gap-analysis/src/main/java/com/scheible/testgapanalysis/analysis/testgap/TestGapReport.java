package com.scheible.testgapanalysis.analysis.testgap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.scheible.testgapanalysis.analysis.testgap.TestGapReportBuilder.BuilderImpl;
import com.scheible.testgapanalysis.analysis.testgap.TestGapReportBuilder.WorkDirStep;
import com.scheible.testgapanalysis.common.EqualsUtils;
import com.scheible.testgapanalysis.common.ToStringBuilder;

/**
 *
 * @author sj
 */
public class TestGapReport {

	private final String workDir;

	private final String previousState;
	private final Optional<String> currentState;
	private final Boolean compareWithWorkingCopyChanges;

	private final Set<String> jaCoCoReportFiles;
	private final int jaCoCoCoverageCount;

	private final Set<NewOrChangedFile> newOrChangedFiles;

	private final int coveredMethodsCount;
	private final int uncoveredMethodsCount;
	private final double testGap;
	private final int emptyMethodsCount;

	private final int unresolvableMethodsCount;
	private final int ambiguouslyResolvedCount;

	private final Set<TestGapMethod> coveredMethods;
	private final Set<TestGapMethod> uncoveredMethods;

	private final Set<TestGapMethod> emptyMethods;
	private final Set<TestGapMethod> unresolvableMethods;
	private final Map<CoverageReportMethod, Set<TestGapMethod>> ambiguouslyResolvedCoverage;

	TestGapReport(BuilderImpl builder) {
		this.workDir = builder.workDir;

		this.previousState = builder.previousState;
		this.currentState = builder.currentState;
		this.compareWithWorkingCopyChanges = builder.currentState.isPresent() ? null : Boolean.TRUE;

		this.jaCoCoReportFiles = Collections.unmodifiableSet(new HashSet<>(builder.jaCoCoReportFiles));
		this.jaCoCoCoverageCount = builder.jaCoCoCoverageCount;

		this.newOrChangedFiles = Collections.unmodifiableSet(new HashSet<>(builder.newOrChangedFiles));

		this.coveredMethodsCount = builder.coveredMethods.size();
		this.uncoveredMethodsCount = builder.uncoveredMethods.size();
		this.testGap = 1.0 - (this.coveredMethodsCount + this.uncoveredMethodsCount > 0
				? (double) this.coveredMethodsCount / (this.coveredMethodsCount + this.uncoveredMethodsCount)
				: 1.0);
		this.emptyMethodsCount = builder.emptyMethods.size();

		this.unresolvableMethodsCount = builder.unresolvableMethods.size();
		this.ambiguouslyResolvedCount = builder.ambiguouslyResolvedCoverage.size();

		this.coveredMethods = Collections.unmodifiableSet(new HashSet<>(builder.coveredMethods));
		this.uncoveredMethods = Collections.unmodifiableSet(new HashSet<>(builder.uncoveredMethods));

		this.emptyMethods = Collections.unmodifiableSet(new HashSet<>(builder.emptyMethods));
		this.unresolvableMethods = Collections.unmodifiableSet(new HashSet<>(builder.unresolvableMethods));
		this.ambiguouslyResolvedCoverage = Collections
				.unmodifiableMap(new HashMap<>(builder.ambiguouslyResolvedCoverage));
	}

	public static WorkDirStep builder() {
		return new BuilderImpl();
	}

	public String getWorkDir() {
		return this.workDir;
	}

	public String getPreviousState() {
		return this.previousState;
	}

	public Optional<String> getCurrentState() {
		return this.currentState;
	}

	public Boolean getCompareWithWorkingCopyChanges() {
		return this.compareWithWorkingCopyChanges;
	}

	public Set<String> getJaCoCoReportFiles() {
		return this.jaCoCoReportFiles;
	}

	public int getJaCoCoCoverageCount() {
		return this.jaCoCoCoverageCount;
	}

	public Set<NewOrChangedFile> getNewOrChangedFiles() {
		return this.newOrChangedFiles;
	}

	public int getCoveredMethodsCount() {
		return this.coveredMethodsCount;
	}

	public int getUncoveredMethodsCount() {
		return this.uncoveredMethodsCount;
	}

	public double getTestGap() {
		return this.testGap;
	}

	public int getEmptyMethodsCount() {
		return this.emptyMethodsCount;
	}

	public int getUnresolvableMethodsCount() {
		return this.unresolvableMethodsCount;
	}

	public int getAmbiguouslyResolvedCount() {
		return this.ambiguouslyResolvedCount;
	}

	public Set<TestGapMethod> getCoveredMethods() {
		return this.coveredMethods;
	}

	public Set<TestGapMethod> getUncoveredMethods() {
		return this.uncoveredMethods;
	}

	public Set<TestGapMethod> getEmptyMethods() {
		return this.emptyMethods;
	}

	public Set<TestGapMethod> getUnresolvableMethods() {
		return this.unresolvableMethods;
	}

	public Map<CoverageReportMethod, Set<TestGapMethod>> getAmbiguouslyResolvedCoverage() {
		return this.ambiguouslyResolvedCoverage;
	}

	@Override
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof TestGapReport) {
			TestGapReport other = (TestGapReport) obj;
			return Objects.equals(this.workDir, other.workDir)
					&& Objects.equals(this.previousState, other.previousState)
					&& Objects.equals(this.currentState, other.currentState)
					&& Objects.equals(this.compareWithWorkingCopyChanges, other.compareWithWorkingCopyChanges)
					&& Objects.equals(this.jaCoCoReportFiles, other.jaCoCoReportFiles)
					&& this.jaCoCoCoverageCount == other.jaCoCoCoverageCount
					&& Objects.equals(this.newOrChangedFiles, other.newOrChangedFiles)
					&& this.coveredMethodsCount == other.coveredMethodsCount
					&& this.uncoveredMethodsCount == other.uncoveredMethodsCount
					&& EqualsUtils.equals(this.testGap, other.testGap)
					&& this.emptyMethodsCount == other.emptyMethodsCount
					&& this.unresolvableMethodsCount == other.unresolvableMethodsCount
					&& this.ambiguouslyResolvedCount == other.ambiguouslyResolvedCount
					&& Objects.equals(this.coveredMethods, other.coveredMethods)
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
		return Objects.hash(this.workDir, this.previousState, this.currentState, this.compareWithWorkingCopyChanges,
				this.jaCoCoReportFiles, this.jaCoCoCoverageCount, this.newOrChangedFiles, this.coveredMethodsCount,
				this.uncoveredMethodsCount, this.testGap, this.emptyMethodsCount, this.unresolvableMethodsCount,
				this.ambiguouslyResolvedCount, this.coveredMethods, this.uncoveredMethods, this.emptyMethods,
				this.unresolvableMethods, this.ambiguouslyResolvedCoverage);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("workDir", this.workDir)
				.append("previousState", this.previousState).append("currentState", this.currentState)
				.append("compareWithWorkingCopyChanges", this.compareWithWorkingCopyChanges)
				.append("jaCoCoReportFiles", this.jaCoCoReportFiles)
				.append("jaCoCoCoverageCount", this.jaCoCoCoverageCount)
				.append("newOrChangedFiles", this.newOrChangedFiles)
				.append("coveredMethodsCount", this.coveredMethodsCount)
				.append("uncoveredMethodsCount", this.uncoveredMethodsCount).append("testGap", this.testGap)
				.append("emptyMethodsCount", this.emptyMethodsCount)
				.append("unresolvableMethodsCount", this.unresolvableMethodsCount)
				.append("ambiguouslyResolvedCount", this.ambiguouslyResolvedCount)
				.append("coveredMethods", this.coveredMethods).append("uncoveredMethods", this.uncoveredMethods)
				.append("emptyMethods", this.emptyMethods).append("unresolvableMethods", this.unresolvableMethods)
				.append("ambiguouslyResolvedCoverage", this.ambiguouslyResolvedCoverage).build();
	}
}
