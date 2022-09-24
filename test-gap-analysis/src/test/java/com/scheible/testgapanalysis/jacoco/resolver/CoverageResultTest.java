package com.scheible.testgapanalysis.jacoco.resolver;

import static java.util.Collections.emptySet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.util.Maps.newHashMap;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class CoverageResultTest {

	private final ParsedMethod secondMethod = mock(ParsedMethod.class);
	private final ParsedMethod firstMethod = mock(ParsedMethod.class);

	private final MethodWithCoverageInfo coverageInfo = mock(MethodWithCoverageInfo.class);

	@Test
	public void testSingleResult() {
		CoverageResult result = new CoverageResult(newHashMap(this.firstMethod, this.coverageInfo), emptySet());

		assertThat(result.getResolvedMethods()).containsOnly(entry(this.firstMethod, this.coverageInfo));
		assertThat(result.getAmbiguousCoverage()).isEmpty();
		assertThat(result.getUnresolvedMethods()).isEmpty();
	}

	@Test
	public void testFindAmbiguouslyResolvedSingleResult() {
		Map<ParsedMethod, MethodWithCoverageInfo> resolved = new HashMap<>();
		resolved.put(this.firstMethod, this.coverageInfo);
		resolved.put(this.secondMethod, this.coverageInfo);

		CoverageResult result = new CoverageResult(resolved, emptySet());

		assertThat(result.getResolvedMethods()).isEmpty();
		assertThat(result.getAmbiguousCoverage())
				.containsOnly(entry(this.coverageInfo, Sets.newLinkedHashSet(this.firstMethod, this.secondMethod)));
		assertThat(result.getUnresolvedMethods()).containsOnly(this.firstMethod, this.secondMethod);
	}

	@Test
	public void testFindAmbiguouslyResolvedWithAddedResults() {
		CoverageResult result = new CoverageResult(newHashMap(this.firstMethod, this.coverageInfo), emptySet());

		result.add(new CoverageResult(newHashMap(this.secondMethod, this.coverageInfo), emptySet()));

		assertThat(result.getResolvedMethods()).isEmpty();
		assertThat(result.getAmbiguousCoverage())
				.containsOnly(entry(this.coverageInfo, Sets.newLinkedHashSet(this.firstMethod, this.secondMethod)));
		assertThat(result.getUnresolvedMethods()).containsOnly(this.firstMethod, this.secondMethod);
	}
}
