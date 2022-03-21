package com.scheible.testgapanalysis.jacoco.resolver;

import static java.util.Collections.emptySet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.util.Maps.newHashMap;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.util.Sets;
import org.junit.Test;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class CoverageResultTest {

	final ParsedMethod secondMethod = mock(ParsedMethod.class);
	final ParsedMethod firstMethod = mock(ParsedMethod.class);

	final MethodWithCoverageInfo coverageInfo = mock(MethodWithCoverageInfo.class);

	@Test
	public void testSingleResult() {
		final CoverageResult result = new CoverageResult(newHashMap(firstMethod, coverageInfo), emptySet());

		assertThat(result.getResolvedMethods()).containsOnly(entry(firstMethod, coverageInfo));
		assertThat(result.getAmbiguousCoverage()).isEmpty();
		assertThat(result.getUnresolvedMethods()).isEmpty();
	}

	@Test
	public void testFindAmbiguouslyResolvedSingleResult() {
		final Map<ParsedMethod, MethodWithCoverageInfo> resolved = new HashMap<>();
		resolved.put(firstMethod, coverageInfo);
		resolved.put(secondMethod, coverageInfo);

		final CoverageResult result = new CoverageResult(resolved, emptySet());

		assertThat(result.getResolvedMethods()).isEmpty();
		assertThat(result.getAmbiguousCoverage())
				.containsOnly(entry(coverageInfo, Sets.newLinkedHashSet(firstMethod, secondMethod)));
		assertThat(result.getUnresolvedMethods()).containsOnly(firstMethod, secondMethod);
	}

	@Test
	public void testFindAmbiguouslyResolvedWithAddedResults() {
		final CoverageResult result = new CoverageResult(newHashMap(firstMethod, coverageInfo), emptySet());

		result.add(new CoverageResult(newHashMap(secondMethod, coverageInfo), emptySet()));

		assertThat(result.getResolvedMethods()).isEmpty();
		assertThat(result.getAmbiguousCoverage())
				.containsOnly(entry(coverageInfo, Sets.newLinkedHashSet(firstMethod, secondMethod)));
		assertThat(result.getUnresolvedMethods()).containsOnly(firstMethod, secondMethod);
	}
}
