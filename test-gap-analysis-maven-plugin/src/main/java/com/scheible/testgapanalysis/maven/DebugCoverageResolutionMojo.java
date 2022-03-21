package com.scheible.testgapanalysis.maven;

import com.scheible.testgapanalysis.DebugCoverageResolution;
import com.scheible.testgapanalysis.DebugCoverageResolutionReport;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author sj
 */
@Mojo(name = "debug-coverage-resolution", threadSafe = true, requiresProject = false)
public class DebugCoverageResolutionMojo extends AbstractTestGapMojo {

	@Parameter(defaultValue = "${project.build.sourceDirectory}")
	private File sourceDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final DebugCoverageResolutionReport report = DebugCoverageResolution.run(baseDir, sourceDir,
				findRelevantJaCoCoReportFiles());

		logReport(report);
	}

	private void logReport(final DebugCoverageResolutionReport report) {
		if (report.getCoverageInfoCount() == 0) {
			getLog().info("No coverage info available!");
		} else {
			getLog().info(String.format("Found coverage info about %d methods in %s.", report.getCoverageInfoCount(),
					report.getJaCoCoReportFiles()));
		}

		getLog().info(String.format("Found %d Java files with %d methods in '%s'.", report.getJavaFileCount(),
				report.getResolved().size() + report.getUnresolved().size(), sourceDir));

		getLog().info("Resolved methods:");
		report.getResolved().entrySet()
				.forEach(e -> getLog().info(String.format(" - %s -> %s", e.getKey(), e.getValue())));

		if (!report.getAmbiguousCoverage().isEmpty()) {
			getLog().info("Ambiguously resolved methods:");
			report.getAmbiguousCoverage().entrySet()
					.forEach(e -> getLog().info(String.format(" - %s -> %s", e.getKey(), e.getValue())));
		}

		if (!report.getUnresolved().isEmpty()) {
			getLog().info("Unresolvable methods (no coverage information available):");
			report.getUnresolved()
					.forEach(u -> getLog().info(String.format(" - %s", u)));
		}
	}
}
