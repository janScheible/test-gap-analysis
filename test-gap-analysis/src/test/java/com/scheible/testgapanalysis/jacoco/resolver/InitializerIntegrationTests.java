package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.coverageResult;
import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.resolved;
import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.CoverageResolutionAssert.assertThat;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.INITIALIZER;
import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Map;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class InitializerIntegrationTests extends AbstractIntegrationTest {

	public static class SingleInitializerWithSingleConstructor {

		{
			"".trim();
		}
	}

	@Test
	public void testSingleInitializerWithSingleConstructor() throws Exception {
		CoverageResolution resolution = resolve(SingleInitializerWithSingleConstructor.class, INITIALIZER);

		assertThat(resolution).isUnambiguouslyResolved();
	}

	public static class MultipleInitializerWithMultipleConstructorsWithAtLeastOneCovered implements Serializable {

		private static final long serialVersionUID = 1L;

		{
			"".trim();
		}

		{
			"".trim();
		}

		// this one is covered because class is instantied with no-args constructor
		public MultipleInitializerWithMultipleConstructorsWithAtLeastOneCovered() {
			"".trim();
		}

		public MultipleInitializerWithMultipleConstructorsWithAtLeastOneCovered(String ggg) {
			"".trim();
		}
	}

	@Test
	public void testMultipleInitializerWithMultipleConstructorsWithAtLeastOneCovered() throws Exception {
		CoverageResolution resolution = resolve(MultipleInitializerWithMultipleConstructorsWithAtLeastOneCovered.class,
				Serializable.class, instance -> {
				}, INITIALIZER);

		assertThat(resolution).isUnambiguouslyResolved();

		assertThat(resolution.getCoveredMethods()).hasSize(1).first().matches(mwci -> mwci.isConstructor());

		assertThat(resolution.getResult().getResolvedMethods()).isEqualTo(coverageResult( //
				resolved(resolution.getParsedMethods().get(0), resolution.getCoveredMethods().get(0)),
				resolved(resolution.getParsedMethods().get(1), resolution.getCoveredMethods().get(0))));
	}

	public static class MultipleInitializerWithMutlipleNotCoveredConstructors {

		{
			"".trim();
		}

		{
			"".trim();
		}

		public MultipleInitializerWithMutlipleNotCoveredConstructors() {
			"".trim();
		}

		public MultipleInitializerWithMutlipleNotCoveredConstructors(String ggg) {
			"".trim();
		}
	}

	@Test
	public void testMultipleInitializerWithMutlipleNotCoveredConstructors() throws Exception {
		CoverageResolution resolution = resolve(MultipleInitializerWithMutlipleNotCoveredConstructors.class,
				INITIALIZER);

		assertThat(resolution).isUnambiguouslyResolved();

		// we don't know if the initalizers are resolved to the first or second constructor
		assertThat(resolution.getResult().getResolvedMethods())
				.is(anyOf(new Condition<Map<ParsedMethod, MethodWithCoverageInfo>>() {
					@Override
					public boolean matches(Map<ParsedMethod, MethodWithCoverageInfo> actual) {
						return actual.equals(coverageResult( //
								resolved(resolution.getParsedMethods().get(0), resolution.getMethodCoverage().get(0)),
								resolved(resolution.getParsedMethods().get(1), resolution.getMethodCoverage().get(0))));
					}

				}, new Condition<Map<ParsedMethod, MethodWithCoverageInfo>>() {
					@Override
					public boolean matches(Map<ParsedMethod, MethodWithCoverageInfo> actual) {
						return actual.equals(coverageResult( //
								resolved(resolution.getParsedMethods().get(0), resolution.getMethodCoverage().get(1)),
								resolved(resolution.getParsedMethods().get(1), resolution.getMethodCoverage().get(1))));
					}
				}));
	}
}
