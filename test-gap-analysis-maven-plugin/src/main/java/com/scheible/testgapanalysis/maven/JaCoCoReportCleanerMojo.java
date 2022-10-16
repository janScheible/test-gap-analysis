package com.scheible.testgapanalysis.maven;

import java.io.File;
import java.util.stream.Collectors;

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
		if (this.buildDir.exists()) {
			getLog().info(String.format("Deleting all JaCoCo reports in: %s", this.buildDir));

			for (File file : findRelevantJaCoCoReportFiles().stream().sorted().collect(Collectors.toList())) {
				if (file.delete()) {
					getLog().info(String.format("Deleted %s.", file));
				} else {
					getLog().info(String.format("Failed to delete %s!", file));
				}
			}
		} else {
			getLog().debug(String.format("Skipping deletion of JaCoCo reports in '%s' because the "
					+ "directory does not exist.", this.buildDir));
		}
	}
}
