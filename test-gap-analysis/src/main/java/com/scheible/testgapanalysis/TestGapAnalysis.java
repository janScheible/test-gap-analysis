package com.scheible.testgapanalysis;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheible.testgapanalysis.analysis.Analysis;
import com.scheible.testgapanalysis.common.Files2;
import com.scheible.testgapanalysis.git.RepositoryStatus;
import com.scheible.testgapanalysis.jacoco.JaCoCoHelper;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;

/**
 *
 * @author sj
 */
public class TestGapAnalysis {

	private static final Logger logger = LoggerFactory.getLogger(TestGapAnalysis.class);

	public static void run(final File workingDir, final Optional<String> referenceCommitHash) {
		logger.info("Comparing the {}", referenceCommitHash.map(h -> "repository head with commit " + h + ".")
				.orElseGet(() -> "working copy changes with the repository head."));

		final Set<File> jaCoCoFiles = JaCoCoHelper.findJaCoCoFiles(workingDir);
		final Set<MethodWithCoverageInfo> coverageInfo = jaCoCoFiles.stream()
				.flatMap(f -> JaCoCoHelper.getMethodCoverage(f).stream()).collect(Collectors.toSet());
		if (coverageInfo.isEmpty()) {
			logger.info("No coverage info available!");
			return;
		}
		logger.info("Found coverage info about {} methods in {}.", coverageInfo.size(),
				jaCoCoFiles.stream().map(Files2::toCanonical).collect(Collectors.toSet()));

		final RepositoryStatus status = referenceCommitHash
				.map(h -> RepositoryStatus.ofCommitComparedToHead(workingDir, h))
				.orElseGet(() -> RepositoryStatus.ofWorkingCopyChanges(workingDir));

		Analysis.perform(status, coverageInfo);
	}
}
