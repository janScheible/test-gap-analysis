package com.scheible.testgapanalysis.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import com.scheible.testgapanalysis.git.FileChange;
import com.scheible.testgapanalysis.git.GitChangeSet;
import com.scheible.testgapanalysis.git.GitRepoState;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.JavaParser;

/**
 *
 * @author sj
 */
public class AnalysisTest {

	@Test
	public void testSampleScenario() {
		GitChangeSet changeSet = new GitChangeSet(new GitRepoState("HEAD^1"), GitRepoState.WORKING_TREE, Sets.set(
				new FileChange("Added.java", Optional.empty(),
						Optional.of("package test; public class Added { private void doIt() {}}")),
				new FileChange("Changed.java",
						Optional.of("package test; public class Changed { private void done() { \"\".trim(); }}"),
						Optional.of("package test; public class Changed { private void done() { \"\".size(); }}"))));

		Set<MethodWithCoverageInfo> coverageInfo = new HashSet<>(
				Arrays.asList(new MethodWithCoverageInfo("test.Added", "doIt", "", 1, 1),
						new MethodWithCoverageInfo("test.Changed", "done", "", 1, 0)));

		Analysis analysis = new Analysis(new JavaParser());
		AnalysisResult result = analysis.perform(changeSet, coverageInfo);
		assertThat(
				result.getUncoveredMethods().keySet().stream().map(pm -> pm.getTopLevelTypeFqn() + "#" + pm.getName()))
						.containsOnly("test.Changed#done");
	}
}
