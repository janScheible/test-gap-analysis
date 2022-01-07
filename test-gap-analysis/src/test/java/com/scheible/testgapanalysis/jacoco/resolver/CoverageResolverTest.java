package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.JaCoCoHelper.getMethodCoverage;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Test;

import com.scheible.testgapanalysis.common.Files2;
import com.scheible.testgapanalysis.jacoco.JaCoCoHelperTest;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.JavaParserHelper;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class CoverageResolverTest {

	@Test
	public void testCoverageTestClassResolvement() {
		final Set<ParsedMethod> methods = JavaParserHelper
				.getMethods(Files2.readUtf8(JavaParserHelper.class, "CoverageTestClass.java"));
		final Set<MethodWithCoverageInfo> coverage = getMethodCoverage(
				Files2.readUtf8(JaCoCoHelperTest.class, "jacoco.xml"));

		final CoverageResolver resolver = CoverageResolver.with(methods, coverage);
		final CoverageResult result = resolver.resolve(methods);

		assertThat(result.getUnresolved()).isEmpty();
	}
}
