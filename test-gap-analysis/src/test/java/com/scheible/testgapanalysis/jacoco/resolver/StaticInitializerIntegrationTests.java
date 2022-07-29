package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.CoverageResolutionAssert.assertThat;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.STATIC_INITIALIZER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;

/**
 *
 * @author sj
 */
public class StaticInitializerIntegrationTests extends AbstractIntegrationTest {

	public static class SingleStaticInitializer {

		static {
			"".trim();
		}
	}

	@Test
	public void testSingleStaticInitializer() throws Exception {
		assertThat(resolve(SingleStaticInitializer.class, STATIC_INITIALIZER)).isUnambiguouslyResolved();
	}

	public static class MultipleStaticInitializer {

		static {
			"".trim();
		}

		static {
			"".trim();
		}
	}

	@Test
	public void testMultipleStaticInitializer() throws Exception {
		CoverageResolution resolution = resolve(MultipleStaticInitializer.class, STATIC_INITIALIZER);

		assertThat(resolution).isUnambiguouslyResolved();

		List<MethodWithCoverageInfo> staticInitializerCoverage = resolution.getMethodCoverage().stream()
				.filter(MethodWithCoverageInfo::isStaticInitializer).collect(Collectors.toList());
		assertThat(staticInitializerCoverage).hasSize(1).first().matches(mwci -> mwci.isStaticInitializer());

		assertThat(resolution.getResult().getResolvedMethods()).isEqualTo(coverageResult( //
				resolved(resolution.getParsedMethods().get(0), staticInitializerCoverage.get(0)),
				resolved(resolution.getParsedMethods().get(1), staticInitializerCoverage.get(0))));
	}
}
