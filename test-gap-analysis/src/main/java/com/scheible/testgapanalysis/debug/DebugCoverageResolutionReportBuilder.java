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

		JaCoCoReportFilesStep setCoverageInfoCount(int coverageInfoCount);
	}

	public interface JaCoCoReportFilesStep {

		JavaFileCountStep setJaCoCoReportFiles(Set<String> jaCoCoReportFiles);
	}

	public interface JavaFileCountStep {

		ResolvedStep setJavaFileCount(int javaFileCount);
	}

	public interface ResolvedStep {

		EmptyStep setResolved(Map<ParsedMethod, MethodWithCoverageInfo> resolved);
	}

	public interface EmptyStep {

		UnresolvedStep setEmpty(Set<ParsedMethod> empty);
	}

	public interface UnresolvedStep {

		AmbiguousCoverageStep setUnresolved(Set<ParsedMethod> unresolved);
	}

	public interface AmbiguousCoverageStep {

		BuildStep setAmbiguousCoverage(Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguousCoverage);
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
		public JaCoCoReportFilesStep setCoverageInfoCount(int coverageInfoCount) {
			this.coverageInfoCount = coverageInfoCount;
			return this;
		}

		@Override
		public JavaFileCountStep setJaCoCoReportFiles(Set<String> jaCoCoReportFiles) {
			this.jaCoCoReportFiles = jaCoCoReportFiles;
			return this;
		}

		@Override
		public ResolvedStep setJavaFileCount(int javaFileCount) {
			this.javaFileCount = javaFileCount;
			return this;
		}

		@Override
		public EmptyStep setResolved(Map<ParsedMethod, MethodWithCoverageInfo> resolved) {
			this.resolved = resolved;
			return this;
		}

		@Override
		public UnresolvedStep setEmpty(Set<ParsedMethod> empty) {
			this.empty = empty;
			return this;
		}

		@Override
		public AmbiguousCoverageStep setUnresolved(Set<ParsedMethod> unresolved) {
			this.unresolved = unresolved;
			return this;
		}

		@Override
		public BuildStep setAmbiguousCoverage(Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguousCoverage) {
			this.ambiguousCoverage = ambiguousCoverage;
			return this;
		}

		@Override
		public DebugCoverageResolutionReport build() {
			return new DebugCoverageResolutionReport(this);
		}
	}
}
