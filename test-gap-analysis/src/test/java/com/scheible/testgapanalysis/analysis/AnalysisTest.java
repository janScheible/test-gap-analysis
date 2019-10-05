package com.scheible.testgapanalysis.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.scheible.testgapanalysis.git.RepositoryStatus;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;

/**
 *
 * @author sj
 */
public class AnalysisTest {

	@Test
	public void testSampleScenario() {
		final RepositoryStatus repositoryStatus = mock(RepositoryStatus.class);

		doReturn(new HashSet<>(Arrays.asList("Added.java"))).when(repositoryStatus).getAddedFiles();
		doReturn(new HashSet<>(Arrays.asList("Changed.java"))).when(repositoryStatus).getChangedFiles();

		doAnswer((Answer<Map<String, String>>) (InvocationOnMock iom) -> {
			final Map<String, String> fileContentMapping = new HashMap<>();
			fileContentMapping.put("Changed.java",
					"package test; public class Changed { private void doAction() { /* :-( */ }}");
			return fileContentMapping;
		}).when(repositoryStatus).getOldContents(any());

		doAnswer((Answer<Map<String, String>>) (InvocationOnMock iom) -> {
			final Map<String, String> fileContentMapping = new HashMap<>();
			fileContentMapping.put("Added.java", "package test; public class Added { private void doIt() {}}");
			fileContentMapping.put("Changed.java",
					"package test; public class Changed { private void doAction() { /* :-) */ }}");
			return fileContentMapping;
		}).when(repositoryStatus).getNewContents(any());

		final Set<MethodWithCoverageInfo> coverageInfo = new HashSet<>(
				Arrays.asList(new MethodWithCoverageInfo("test.Changed", "covered", 1)));

		final Optional<AnalysisResult> result = Analysis.perform(repositoryStatus, coverageInfo);
		assertThat(result).isPresent().get()
				.satisfies(ar -> assertThat(ar.getUncoveredNewOrChangedMethods()).hasSize(2));
		assertThat(result.get().getUncoveredNewOrChangedMethods().stream()
				.map(pm -> pm.getTypeFullyQualifiedName() + "#" + pm.getMethodName()))
						.containsExactly("test.Added#doIt", "test.Changed#doAction");
	}
}
