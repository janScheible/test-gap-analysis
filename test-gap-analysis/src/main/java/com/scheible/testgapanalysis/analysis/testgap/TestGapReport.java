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

	public static class NewOrChangedFile {

		public enum State {
			NEW, CHANGED
		};

		private final String repositoryPath;
		private final boolean skipped;
		private final State state;

		public NewOrChangedFile(final String name, final boolean skipped, final State state) {
			this.repositoryPath = name;
			this.skipped = skipped;
			this.state = state;
		}

		public String getName() {
			return repositoryPath;
		}

		public boolean isSkipped() {
			return skipped;
		}

		public State getState() {
			return state;
		}

		@Override
		public String toString() {
			return String.format("[%s%s] %s", skipped ? "skipped, " : "", state == State.CHANGED ? "changed" : "new",
					repositoryPath);
		}
	}

	public static class TestGapMethod {

		private final String topLevelTypeFqn;
		private final String description;
		private final int sourceLine;
		private final int sourceColumn;

		private final String coveredMethodName;
		private final Integer coveredMethodLine;

		private TestGapMethod(final String topLevelTypeFqn, final String description, final int sourceLine,
				final int sourceColumn, final String coveredMethodName, final Integer coveredMethodLine) {
			this.topLevelTypeFqn = topLevelTypeFqn;
			this.description = description;
			this.sourceLine = sourceLine;
			this.sourceColumn = sourceColumn;

			this.coveredMethodName = coveredMethodName;
			this.coveredMethodLine = coveredMethodLine;
		}

		public TestGapMethod(final String topLevelTypeFqn, final String description, final int sourceLine,
				final int sourceColumn, final String coveredMethodName, final int coveredMethodLine) {
			this(topLevelTypeFqn, description, sourceLine, sourceColumn, coveredMethodName,
					(Integer) coveredMethodLine);
		}

		public TestGapMethod(final String topLevelTypeFqn, final String description, final int sourceLine,
				final int sourceColumn) {
			this(topLevelTypeFqn, description, sourceLine, sourceColumn, null, null);
		}

		public String getTopLevelTypeFqn() {
			return topLevelTypeFqn;
		}

		public String getDescription() {
			return description;
		}

		public int getSourceLine() {
			return sourceLine;
		}

		public int getSourceColumn() {
			return sourceColumn;
		}

		public Optional<String> getCoveredMethodName() {
			return Optional.ofNullable(coveredMethodName);
		}

		public Optional<Integer> getCoveredMethodLine() {
			return Optional.ofNullable(coveredMethodLine);
		}

		@Override
		public String toString() {
			final String coverageInfo = coveredMethodName != null && coveredMethodLine != null
					? String.format(" resolved to '%s' with line %d", coveredMethodName, coveredMethodLine)
					: "";
			return String.format("%s%s at %d:%d%s", topLevelTypeFqn, description, sourceLine, sourceColumn,
					coverageInfo);
		}
	}

	public static class CoverageReportMethod {

		private final String coveredMethodName;
		private final int coveredMethodLine;

		public CoverageReportMethod(final String coveredMethodName, final int coveredMethodLine) {
			this.coveredMethodName = coveredMethodName;
			this.coveredMethodLine = coveredMethodLine;
		}

		public String getCoveredMethodName() {
			return coveredMethodName;
		}

		public int getCoveredMethodLine() {
			return coveredMethodLine;
		}

		@Override
		public String toString() {
			return String.format("'%s' at line %d", coveredMethodName, coveredMethodLine);
		}
	}

	private final String workDir;

	private final String oldCommitHash;
	private final String newCommitHash;
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

	public static WorkDirStep builder() {
		return new BuilderImpl();
	}

	TestGapReport(final BuilderImpl builder) {
		this.workDir = builder.workDir;

		this.oldCommitHash = builder.oldCommitHash;
		this.newCommitHash = builder.newCommitHash.orElse(null);
		compareWithWorkingCopyChanges = builder.newCommitHash.isPresent() ? null : Boolean.TRUE;

		this.jaCoCoReportFiles = Collections.unmodifiableSet(new HashSet<>(builder.jaCoCoReportFiles));
		this.jaCoCoCoverageCount = builder.jaCoCoCoverageCount;

		this.newOrChangedFiles = Collections.unmodifiableSet(new HashSet<>(builder.newOrChangedFiles));
		consideredNewOrChangedFilesCount = (int) builder.newOrChangedFiles.stream().filter(f -> !f.isSkipped()).count();

		coveredMethodsCount = builder.coveredMethods.size();
		this.coveredMethods = Collections.unmodifiableSet(new HashSet<>(builder.coveredMethods));
		uncoveredMethodsCount = builder.uncoveredMethods.size();
		this.uncoveredMethods = Collections.unmodifiableSet(new HashSet<>(builder.uncoveredMethods));
		coverageRatio = coveredMethodsCount + uncoveredMethodsCount > 0
				? (double) coveredMethodsCount / (coveredMethodsCount + uncoveredMethodsCount)
				: 1.0;
		emptyMethodsCount = builder.emptyMethods.size();
		this.emptyMethods = Collections.unmodifiableSet(new HashSet<>(builder.emptyMethods));
		unresolvableMethodsCount = builder.unresolvableMethods.size();
		this.unresolvableMethods = Collections.unmodifiableSet(new HashSet<>(builder.unresolvableMethods));
		ambiguouslyResolvedCount = builder.ambiguouslyResolvedCoverage.size();
		this.ambiguouslyResolvedCoverage = Collections
				.unmodifiableMap(new HashMap<>(builder.ambiguouslyResolvedCoverage));
	}

	public String getWorkDir() {
		return workDir;
	}

	public String getOldCommitHash() {
		return oldCommitHash;
	}

	public Optional<String> getNewCommitHash() {
		return Optional.ofNullable(newCommitHash);
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

	public int getEmptyMethodsCount() {
		return emptyMethodsCount;
	}

	public Set<TestGapMethod> getEmptyMethods() {
		return emptyMethods;
	}

	public int getUncoveredMethodsCount() {
		return uncoveredMethodsCount;
	}

	public int getUnresolvableMethodsCount() {
		return unresolvableMethodsCount;
	}

	public double getCoverageRatio() {
		return coverageRatio;
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

	public Set<TestGapMethod> getUnresolvableMethods() {
		return unresolvableMethods;
	}

	public Map<CoverageReportMethod, Set<TestGapMethod>> getAmbiguouslyResolvedCoverage() {
		return ambiguouslyResolvedCoverage;
	}
}
