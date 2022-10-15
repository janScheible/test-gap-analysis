package com.scheible.testgapanalysis.maven;

import com.scheible.testgapanalysis.jacoco.JaCoCoReportParser;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author sj
 */
public abstract class AbstractTestGapMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "${project.basedir}") // where the POM.xml is
	protected File baseDir;
	
	@Parameter(defaultValue = "${project.build.sourceDirectory}") // ./src/main/java
	protected File sourceDir;	

	@Parameter(defaultValue = "${project.build.directory}") // ./target
	protected File buildDir;

	@Parameter(defaultValue = "${project.build.outputDirectory}") // ./taget/classes
	protected File outputDir;

	@Parameter(defaultValue = "${project.build.testOutputDirectory}")  // ./taget/test-classes
	protected File testOutputDir;

	/**
	 * Find all JaCoCo reports that are in the 'target' dir but not in 'classes' or 'test-classes'.
	 */
	protected Set<File> findRelevantJaCoCoReportFiles() {
		Set<File> result = new HashSet<>();

		Path outputDirAsPath = this.outputDir.toPath();
		Path testOutputDirAsPath = this.testOutputDir.toPath();

		JaCoCoReportParser.findJaCoCoReportFiles(this.buildDir).forEach(file -> {
			Path fileAsPath = file.toPath();
			if (!fileAsPath.startsWith(outputDirAsPath) && !fileAsPath.startsWith(testOutputDirAsPath)) {
				result.add(file);
			} else {
				getLog().debug(String.format("Skipped %s because it is in an output directory.", file));
			}
		});

		return result;
	}
}
