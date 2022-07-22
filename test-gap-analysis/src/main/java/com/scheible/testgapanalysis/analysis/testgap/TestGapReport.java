package com.scheible.testgapanalysis.analysis.testgap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.scheible.testgapanalysis.analysis.testgap.TestGapReportBuilder.BuilderImpl;
import com.scheible.testgapanalysis.analysis.testgap.TestGapReportBuilder.WorkDirStep;

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
}
