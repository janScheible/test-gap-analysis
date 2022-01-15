package com.scheible.testgapanalysis.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 *
 * @author sj
 */
@Mojo(name = "clean-jacoco-reports", threadSafe = true, requiresProject = false)
public class JaCoCoReportCleanerMojo extends AbstractTestGapMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(String.format("Deleting all JaCoCo reports in: %s", buildDir));

		for (final File file : findRelevantJaCoCoReportFiles()) {
			if (file.delete()) {
				getLog().info(String.format("Deleted %s.", file));
			} else {
				getLog().info(String.format("Failed to delete %s!", file));
			}
		}
	}
}
