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

	private final String oldCommitHash;
	private final Optional<String> newCommitHash;
	private final Boolean compareWithWorkingCopyChanges;

	private final Set<String> jaCoCoReportFiles;
	private final int jaCoCoCoverageCount;

	private final Set<NewOrChangedFile> newOrChangedFiles;
	private final int consideredNewOrChangedFilesCount;

	private final int coveredMethodsCount;
	private final int uncoveredMethodsCount;
	private final double coverageRatio;
	private final int emptyMethodsCount;

	private final int unresolvableMethodsCount;
	private final int ambiguouslyResolvedCount;

	private final Set<TestGapMethod> coveredMethods;
	private final Set<TestGapMethod> uncoveredMethods;

	private final Set<TestGapMethod> emptyMethods;
	private final Set<TestGapMethod> unresolvableMethods;
	private final Map<CoverageReportMethod, Set<TestGapMethod>> ambiguouslyResolvedCoverage;

	TestGapReport(final BuilderImpl builder) {
		this.workDir = builder.workDir;

		this.oldCommitHash = builder.oldCommitHash;
		this.newCommitHash = builder.newCommitHash;
		compareWithWorkingCopyChanges = builder.newCommitHash.isPresent() ? null : Boolean.TRUE;

		this.jaCoCoReportFiles = Collections.unmodifiableSet(new HashSet<>(builder.jaCoCoReportFiles));
		this.jaCoCoCoverageCount = builder.jaCoCoCoverageCount;

		this.newOrChangedFiles = Collections.unmodifiableSet(new HashSet<>(builder.newOrChangedFiles));
		consideredNewOrChangedFilesCount = (int) builder.newOrChangedFiles.stream().filter(f -> !f.isSkipped()).count();

		coveredMethodsCount = builder.coveredMethods.size();
		uncoveredMethodsCount = builder.uncoveredMethods.size();
		coverageRatio = coveredMethodsCount + uncoveredMethodsCount > 0
				? (double) coveredMethodsCount / (coveredMethodsCount + uncoveredMethodsCount)
				: 1.0;
		emptyMethodsCount = builder.emptyMethods.size();

		unresolvableMethodsCount = builder.unresolvableMethods.size();
		ambiguouslyResolvedCount = builder.ambiguouslyResolvedCoverage.size();

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
		return workDir;
	}

	public String getOldCommitHash() {
		return oldCommitHash;
	}

	public Optional<String> getNewCommitHash() {
		return newCommitHash;
	}

	public Boolean getCompareWithWorkingCopyChanges() {
		return compareWithWorkingCopyChanges;
	}

	public Set<String> getJaCoCoReportFiles() {
		return jaCoCoReportFiles;
	}

	public int getJaCoCoCoverageCount() {
		return jaCoCoCoverageCount;
	}

	public Set<NewOrChangedFile> getNewOrChangedFiles() {
		return newOrChangedFiles;
	}

	public int getConsideredNewOrChangedFilesCount() {
		return consideredNewOrChangedFilesCount;
	}

	public int getCoveredMethodsCount() {
		return coveredMethodsCount;
	}

	public int getUncoveredMethodsCount() {
		return uncoveredMethodsCount;
	}

	public double getCoverageRatio() {
		return coverageRatio;
	}

	public int getEmptyMethodsCount() {
		return emptyMethodsCount;
	}

	public int getUnresolvableMethodsCount() {
		return unresolvableMethodsCount;
	}

	public int getAmbiguouslyResolvedCount() {
		return ambiguouslyResolvedCount;
	}

	public Set<TestGapMethod> getCoveredMethods() {
		return coveredMethods;
	}

	public Set<TestGapMethod> getUncoveredMethods() {
		return uncoveredMethods;
	}

	public Set<TestGapMethod> getEmptyMethods() {
		return emptyMethods;
	}

	public Set<TestGapMethod> getUnresolvableMethods() {
		return unresolvableMethods;
	}

	public Map<CoverageReportMethod, Set<TestGapMethod>> getAmbiguouslyResolvedCoverage() {
		return ambiguouslyResolvedCoverage;
	}

	@Override
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof TestGapReport) {
			final TestGapReport other = (TestGapReport) obj;
			return Objects.equals(workDir, other.workDir) && Objects.equals(oldCommitHash, other.oldCommitHash)
					&& Objects.equals(newCommitHash, other.newCommitHash)
					&& Objects.equals(compareWithWorkingCopyChanges, other.compareWithWorkingCopyChanges)
					&& Objects.equals(jaCoCoReportFiles, other.jaCoCoReportFiles)
					&& jaCoCoCoverageCount == other.jaCoCoCoverageCount
					&& Objects.equals(newOrChangedFiles, other.newOrChangedFiles)
					&& consideredNewOrChangedFilesCount == other.consideredNewOrChangedFilesCount
					&& coveredMethodsCount == other.coveredMethodsCount
					&& uncoveredMethodsCount == other.uncoveredMethodsCount
					&& EqualsUtils.equals(coverageRatio, other.coverageRatio)
					&& emptyMethodsCount == other.emptyMethodsCount
					&& unresolvableMethodsCount == other.unresolvableMethodsCount
					&& ambiguouslyResolvedCount == other.ambiguouslyResolvedCount
					&& Objects.equals(coveredMethods, other.coveredMethods)
					&& Objects.equals(uncoveredMethods, other.uncoveredMethods)
					&& Objects.equals(emptyMethods, other.emptyMethods)
					&& Objects.equals(unresolvableMethods, other.unresolvableMethods)
					&& Objects.equals(ambiguouslyResolvedCoverage, other.ambiguouslyResolvedCoverage);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(workDir, oldCommitHash, newCommitHash, compareWithWorkingCopyChanges, jaCoCoReportFiles,
				jaCoCoCoverageCount, newOrChangedFiles, consideredNewOrChangedFilesCount, coveredMethodsCount,
				uncoveredMethodsCount, coverageRatio, emptyMethodsCount, unresolvableMethodsCount,
				ambiguouslyResolvedCount, coveredMethods, uncoveredMethods, emptyMethods, unresolvableMethods,
				ambiguouslyResolvedCoverage);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("workDir", workDir).append("oldCommitHash", oldCommitHash)
				.append("newCommitHash", newCommitHash)
				.append("compareWithWorkingCopyChanges", compareWithWorkingCopyChanges)
				.append("jaCoCoReportFiles", jaCoCoReportFiles).append("jaCoCoCoverageCount", jaCoCoCoverageCount)
				.append("newOrChangedFiles", newOrChangedFiles)
				.append("consideredNewOrChangedFilesCount", consideredNewOrChangedFilesCount)
				.append("coveredMethodsCount", coveredMethodsCount)
				.append("uncoveredMethodsCount", uncoveredMethodsCount).append("coverageRatio", coverageRatio)
				.append("emptyMethodsCount", emptyMethodsCount)
				.append("unresolvableMethodsCount", unresolvableMethodsCount)
				.append("ambiguouslyResolvedCount", ambiguouslyResolvedCount).append("coveredMethods", coveredMethods)
				.append("uncoveredMethods", uncoveredMethods).append("emptyMethods", emptyMethods)
				.append("unresolvableMethods", unresolvableMethods)
				.append("ambiguouslyResolvedCoverage", ambiguouslyResolvedCoverage).build();
	}
}
