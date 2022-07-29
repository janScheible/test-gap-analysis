package com.scheible.testgapanalysis.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.scheible.testgapanalysis.git.RepositoryStatus;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.JavaParser;

/**
 *
 * @author sj
 */
public class AnalysisTest {

	@Test
	public void testSampleScenario() {
		RepositoryStatus repositoryStatus = mock(RepositoryStatus.class);

		doReturn(new HashSet<>(Arrays.asList("Added.java"))).when(repositoryStatus).getAddedFiles();
		doReturn(new HashSet<>(Arrays.asList("Changed.java"))).when(repositoryStatus).getChangedFiles();

		doAnswer((Answer<Map<String, String>>) (InvocationOnMock iom) -> {
			Map<String, String> fileContentMapping = new HashMap<>();
			fileContentMapping.put("Changed.java",
					"package test; public class Changed { private void doAction() { \"\".trim(); }}");
			return fileContentMapping;
		}).when(repositoryStatus).getOldContents();

		doAnswer((Answer<Map<String, String>>) (InvocationOnMock iom) -> {
			Map<String, String> fileContentMapping = new HashMap<>();
			fileContentMapping.put("Added.java", "package test; public class Added { private void doIt() {}}");
			fileContentMapping.put("Changed.java",
					"package test; public class Changed { private void doAction() { \"\".size(); }}");
			return fileContentMapping;
		}).when(repositoryStatus).getNewContents();

		Set<MethodWithCoverageInfo> coverageInfo = new HashSet<>(
				Arrays.asList(new MethodWithCoverageInfo("test.Added", "doIt", "", 1, 1),
						new MethodWithCoverageInfo("test.Changed", "doAction", "", 1, 0)));

		Analysis analysis = new Analysis(new JavaParser());
		AnalysisResult result = analysis.perform(repositoryStatus, coverageInfo);
		assertThat(
				result.getUncoveredMethods().keySet().stream().map(pm -> pm.getTopLevelTypeFqn() + "#" + pm.getName()))
						.containsOnly("test.Changed#doAction");
	}
}
