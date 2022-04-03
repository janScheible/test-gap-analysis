package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.parser.TestClassSourceJavaParser.parseJavaTestSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.assertj.core.api.AbstractAssert;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.xml.XMLFormatter;

import com.scheible.testgapanalysis.jacoco.JaCoCoHelper;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;
import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
public abstract class AbstractIntegrationTest {

	public static class CoverageResultAssert extends AbstractAssert<CoverageResultAssert, CoverageResult> {

		private CoverageResultAssert(final CoverageResult actual) {
			super(actual, CoverageResultAssert.class);
		}

		public static CoverageResultAssert assertThat(final CoverageResult actual) {
			return new CoverageResultAssert(actual);
		}

		public CoverageResultAssert isUnambiguouslyResolved() {
			if (!actual.getUnresolvedMethods().isEmpty() || !actual.getAmbiguousCoverage().isEmpty()) {
				failWithMessage(
						"Expected unambiguously resolved coverage result but unresolved methods is %s and "
								+ "ambiguous coverage is %s",
						actual.getUnresolvedMethods(), actual.getAmbiguousCoverage());
			}

			return this;
		}
	}

	protected CoverageResult resolve(final Class<?> testClass, final MethodType... filterTypes) throws Exception {
		final String testClassName = testClass.getName();

		final IRuntime runtime = new LoggerRuntime();
		final RuntimeData data = new RuntimeData();
		runtime.startup(data);

		final ExecutionDataStore executionData = new ExecutionDataStore();
		final SessionInfoStore sessionInfos = new SessionInfoStore();
		data.collect(executionData, sessionInfos, false);
		runtime.shutdown();

		// together with the original class definition we can calculate coverage information
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		try (InputStream original = getTargetClass(testClassName)) {
			analyzer.analyzeClass(original, testClassName);
		}

		final Set<ParsedMethod> parsedMethods = parseJavaTestSource(testClass, filterTypes);

		final String xmlOutput = getCoverageReportXml(sessionInfos, executionData, coverageBuilder);
		final Set<MethodWithCoverageInfo> methodCoverage = JaCoCoHelper.getMethodCoverage(xmlOutput);

		return CoverageResolver.with(parsedMethods, methodCoverage).resolve(parsedMethods);
	}

	private String getCoverageReportXml(final SessionInfoStore sessionInfos, final ExecutionDataStore executionData,
			final CoverageBuilder coverageBuilder) throws IOException {
		final XMLFormatter xmlFormatter = new XMLFormatter();
		final ByteArrayOutputStream xmlOutput = new ByteArrayOutputStream();

		final IReportVisitor visitor = xmlFormatter.createVisitor(xmlOutput);
		visitor.visitInfo(sessionInfos.getInfos(), executionData.getContents());
		visitor.visitBundle(coverageBuilder.getBundle("test-class"), null);
		visitor.visitEnd();

		return new String(xmlOutput.toByteArray(), StandardCharsets.UTF_8);
	}

	private InputStream getTargetClass(final String name) {
		final String resourceName = '/' + name.replace('.', '/') + ".class";
		return getClass().getResourceAsStream(resourceName);
	}

}
