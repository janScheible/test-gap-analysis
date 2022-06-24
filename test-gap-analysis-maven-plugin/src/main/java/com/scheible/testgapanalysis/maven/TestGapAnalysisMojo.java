package com.scheible.testgapanalysis.maven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scheible.testgapanalysis.analysis.testgap.TestGapAnalysis;
import com.scheible.testgapanalysis.analysis.testgap.TestGapReport;
import com.scheible.testgapanalysis.analysis.Analysis;
import com.scheible.testgapanalysis.git.GitDiffer;
import com.scheible.testgapanalysis.jacoco.JaCoCoReportParser;
import com.scheible.testgapanalysis.parser.JavaParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author sj
 */
@Mojo(name = "perform", threadSafe = true, requiresProject = false)
public class TestGapAnalysisMojo extends AbstractTestGapMojo {

	@Parameter(property = "test-gap-analysis.reference-commit-hash")
	private String referenceCommitHash;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (buildDir.exists()) {
			final TestGapAnalysis testGapAnalysis = new TestGapAnalysis(new Analysis(new JavaParser()),
					new JaCoCoReportParser(), new GitDiffer());
			final TestGapReport report = testGapAnalysis.run(baseDir, findRelevantJaCoCoReportFiles(),
					Optional.ofNullable(referenceCommitHash));

			logReport(report);
			writeJsonReport(report);
		} else {
			getLog().debug(String.format("Skipping test gap analysis because the '%s' directory does not exist and "
					+ "therefore no JaCoCo coverage reports are available.", buildDir));
		}
	}

	private void logReport(final TestGapReport report) {
		getLog().info(String.format("Performing test gap analysis in '%s'.", report.getWorkDir()));

		if (report.getJaCoCoCoverageCount() == 0) {
			getLog().info("No coverage info available!");
		} else {
			getLog().info(String.format("Found coverage info about %d methods in %s.", report.getJaCoCoCoverageCount(),
					report.getJaCoCoReportFiles()));
		}

		final String oldCommitHash = report.getOldCommitHash().substring(0, 7);
		getLog().info(String.format("Comparing the %s", report.getNewCommitHash()
				.map(newCommitHash -> "repository head (" + newCommitHash.substring(0, 7)
				+ ") with reference commit " + oldCommitHash + ".")
				.orElseGet(() -> "working copy changes with the repository head (" + oldCommitHash + ").")));

		if (report.getNewOrChangedFiles().isEmpty()) {
			getLog().info("No new or changed files!");
		} else {
			getLog().info(String.format("Found %d new or changed Java files (%d non-test Java files are considered):",
					report.getNewOrChangedFiles().size(), report.getConsideredNewOrChangedFilesCount()));
			report.getNewOrChangedFiles().stream().sorted((a, b) -> a.getName().compareTo(b.getName()))
					.map(Object::toString).forEach(newOrChangedFile -> getLog().info(" - " + newOrChangedFile));
		}

		getLog().info("Method blacklist (excluded from coverage check): all getter and setter");

		if (!report.getCoveredMethods().isEmpty()) {
			getLog().info("Covered methods:");
			report.getCoveredMethods().stream().map(Object::toString).sorted()
					.forEach(coveredMethod -> getLog().info(" - " + coveredMethod));
		}

		if (!report.getUncoveredMethods().isEmpty()) {
			getLog().info("Uncovered methods:");
			report.getUncoveredMethods().stream().map(Object::toString).sorted()
					.forEach(uncoveredMethod -> getLog().info(" - " + uncoveredMethod));
		}

		if (!report.getEmptyMethods().isEmpty()) {
			getLog().info("Empty methods (no coverage information available):");
			report.getEmptyMethods().stream().map(Object::toString).sorted()
					.forEach(emptyMethod -> getLog().info(" - " + emptyMethod));
		}

		if (!report.getAmbiguouslyResolvedCoverage().isEmpty()) {
			getLog().info("Ambiguously resolved methods (multiple methods were resolved to a single coverage information):");
			report.getAmbiguouslyResolvedCoverage().entrySet().stream()
					.forEach(e -> getLog().info(String.format(" - %s -> %s", e.getKey(), e.getValue())));
		}

		if (!report.getUnresolvableMethods().isEmpty()) {
			getLog().info("Unresolvable methods (coverage information couldn't be found):");
			report.getUnresolvableMethods().stream().map(Object::toString).sorted()
					.forEach(unresolvableMethod -> getLog().info(" - " + unresolvableMethod));
		}
	}

	private void writeJsonReport(final TestGapReport report) throws MojoExecutionException {
		final File reportFile = new File(buildDir, "test-gap-report.json");
		final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

		try {
			Files.write(reportFile.toPath(), gson.toJson(report).getBytes());
		} catch (IOException ex) {
			throw new MojoExecutionException("Can't write test gap report to '" + reportFile + "'!", ex);
		}
	}
}
