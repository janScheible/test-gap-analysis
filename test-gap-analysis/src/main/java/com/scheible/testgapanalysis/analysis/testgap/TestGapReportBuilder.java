package com.scheible.testgapanalysis.analysis.testgap;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author sj
 */
public abstract class TestGapReportBuilder {

	private TestGapReportBuilder() {
	}

	public interface WorkDirStep {

		OldCommitHashStep setWorkDir(String workDir);
	}

	public interface OldCommitHashStep {

		NewCommitHashStep setOldCommitHash(String oldCommitHash);
	}

	public interface NewCommitHashStep {

		JaCoCoReportFilesStep setNewCommitHash(Optional<String> newCommitHash);
	}

	public interface JaCoCoReportFilesStep {

		JaCoCoCoverageCountStep setJaCoCoReportFiles(Set<String> jaCoCoReportFiles);
	}

	public interface JaCoCoCoverageCountStep {

		NewOrChangedFilesStep setJaCoCoCoverageCount(int jaCoCoCoverageCount);
	}

	public interface NewOrChangedFilesStep {

		CoveredMethodsStep setNewOrChangedFiles(Set<NewOrChangedFile> newOrChangedFiles);
	}

	public interface CoveredMethodsStep {

		UncoveredMethodsStep setCoveredMethods(Set<TestGapMethod> coveredMethods);
	}

	public interface UncoveredMethodsStep {

		EmptyMethodsStep setUncoveredMethods(Set<TestGapMethod> uncoveredMethods);
	}

	public interface EmptyMethodsStep {

		UnresolvableMethodsStep setEmptyMethods(Set<TestGapMethod> emptyMethods);
	}

	public interface UnresolvableMethodsStep {

		AmbiguouslyResolvedCoverageStep setUnresolvableMethods(Set<TestGapMethod> unresolvableMethods);
	}

	public interface AmbiguouslyResolvedCoverageStep {

		BuildStep setAmbiguouslyResolvedCoverage(
				Map<CoverageReportMethod, Set<TestGapMethod>> ambiguouslyResolvedCoverage);
	}

	public interface BuildStep {

		TestGapReport build();
	}

	static class BuilderImpl
			implements
				WorkDirStep,
				OldCommitHashStep,
				NewCommitHashStep,
				JaCoCoReportFilesStep,
				JaCoCoCoverageCountStep,
				NewOrChangedFilesStep,
				CoveredMethodsStep,
				UncoveredMethodsStep,
				EmptyMethodsStep,
				UnresolvableMethodsStep,
				AmbiguouslyResolvedCoverageStep,
				BuildStep {

		String workDir;
		String oldCommitHash;
		Optional<String> newCommitHash;
		Set<String> jaCoCoReportFiles;
		int jaCoCoCoverageCount;
		Set<NewOrChangedFile> newOrChangedFiles;
		Set<TestGapMethod> coveredMethods;
		Set<TestGapMethod> uncoveredMethods;
		Set<TestGapMethod> emptyMethods;
		Set<TestGapMethod> unresolvableMethods;
		Map<CoverageReportMethod, Set<TestGapMethod>> ambiguouslyResolvedCoverage;

		BuilderImpl() {
		}

		@Override
		public OldCommitHashStep setWorkDir(final String workDir) {
			this.workDir = workDir;
			return this;
		}

		@Override
		public NewCommitHashStep setOldCommitHash(final String oldCommitHash) {
			this.oldCommitHash = oldCommitHash;
			return this;
		}

		@Override
		public JaCoCoReportFilesStep setNewCommitHash(final Optional<String> newCommitHash) {
			this.newCommitHash = newCommitHash;
			return this;
		}

		@Override
		public JaCoCoCoverageCountStep setJaCoCoReportFiles(final Set<String> jaCoCoReportFiles) {
			this.jaCoCoReportFiles = jaCoCoReportFiles;
			return this;
		}

		@Override
		public NewOrChangedFilesStep setJaCoCoCoverageCount(final int jaCoCoCoverageCount) {
			this.jaCoCoCoverageCount = jaCoCoCoverageCount;
			return this;
		}

		@Override
		public CoveredMethodsStep setNewOrChangedFiles(final Set<NewOrChangedFile> newOrChangedFiles) {
			this.newOrChangedFiles = newOrChangedFiles;
			return this;
		}

		@Override
		public UncoveredMethodsStep setCoveredMethods(final Set<TestGapMethod> coveredMethods) {
			this.coveredMethods = coveredMethods;
			return this;
		}

		@Override
		public EmptyMethodsStep setUncoveredMethods(final Set<TestGapMethod> uncoveredMethods) {
			this.uncoveredMethods = uncoveredMethods;
			return this;
		}

		@Override
		public UnresolvableMethodsStep setEmptyMethods(final Set<TestGapMethod> emptyMethods) {
			this.emptyMethods = emptyMethods;
			return this;
		}

		@Override
		public AmbiguouslyResolvedCoverageStep setUnresolvableMethods(final Set<TestGapMethod> unresolvableMethods) {
			this.unresolvableMethods = unresolvableMethods;
			return this;
		}

		@Override
		public BuildStep setAmbiguouslyResolvedCoverage(
				final Map<CoverageReportMethod, Set<TestGapMethod>> ambiguouslyResolvedCoverage) {
			this.ambiguouslyResolvedCoverage = ambiguouslyResolvedCoverage;
			return this;
		}

		@Override
		public TestGapReport build() {
			return new TestGapReport(this);
		}
	}
}
