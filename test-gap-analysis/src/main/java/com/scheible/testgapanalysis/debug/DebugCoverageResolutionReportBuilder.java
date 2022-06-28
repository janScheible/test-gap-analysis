package com.scheible.testgapanalysis.debug;

import java.util.Map;
import java.util.Set;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public abstract class DebugCoverageResolutionReportBuilder {

	private DebugCoverageResolutionReportBuilder() {
	}

	public interface CoverageInfoCountStep {

		JaCoCoReportFilesStep setCoverageInfoCount(final int coverageInfoCount);
	}

	public interface JaCoCoReportFilesStep {

		JavaFileCountStep setJaCoCoReportFiles(final Set<String> jaCoCoReportFiles);
	}

	public interface JavaFileCountStep {

		ResolvedStep setJavaFileCount(final int javaFileCount);
	}

	public interface ResolvedStep {

		EmptyStep setResolved(final Map<ParsedMethod, MethodWithCoverageInfo> resolved);
	}

	public interface EmptyStep {

		UnresolvedStep setEmpty(final Set<ParsedMethod> empty);
	}

	public interface UnresolvedStep {

		AmbiguousCoverageStep setUnresolved(final Set<ParsedMethod> unresolved);
	}

	public interface AmbiguousCoverageStep {

		BuildStep setAmbiguousCoverage(final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguousCoverage);
	}

	public interface BuildStep {

		DebugCoverageResolutionReport build();
	}

	static class BuilderImpl
			implements
				CoverageInfoCountStep,
				JaCoCoReportFilesStep,
				JavaFileCountStep,
				ResolvedStep,
				EmptyStep,
				UnresolvedStep,
				AmbiguousCoverageStep,
				BuildStep {

		int coverageInfoCount;
		Set<String> jaCoCoReportFiles;
		int javaFileCount;
		Map<ParsedMethod, MethodWithCoverageInfo> resolved;
		Set<ParsedMethod> empty;
		Set<ParsedMethod> unresolved;
		Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguousCoverage;

		BuilderImpl() {
		}

		@Override
		public JaCoCoReportFilesStep setCoverageInfoCount(final int coverageInfoCount) {
			this.coverageInfoCount = coverageInfoCount;
			return this;
		}

		@Override
		public JavaFileCountStep setJaCoCoReportFiles(final Set<String> jaCoCoReportFiles) {
			this.jaCoCoReportFiles = jaCoCoReportFiles;
			return this;
		}

		@Override
		public ResolvedStep setJavaFileCount(final int javaFileCount) {
			this.javaFileCount = javaFileCount;
			return this;
		}

		@Override
		public EmptyStep setResolved(final Map<ParsedMethod, MethodWithCoverageInfo> resolved) {
			this.resolved = resolved;
			return this;
		}

		@Override
		public UnresolvedStep setEmpty(final Set<ParsedMethod> empty) {
			this.empty = empty;
			return this;
		}

		@Override
		public AmbiguousCoverageStep setUnresolved(final Set<ParsedMethod> unresolved) {
			this.unresolved = unresolved;
			return this;
		}

		@Override
		public BuildStep setAmbiguousCoverage(final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguousCoverage) {
			this.ambiguousCoverage = ambiguousCoverage;
			return this;
		}

		@Override
		public DebugCoverageResolutionReport build() {
			return new DebugCoverageResolutionReport(this);
		}
	}
}
