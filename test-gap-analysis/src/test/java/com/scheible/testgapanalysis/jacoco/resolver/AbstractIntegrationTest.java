package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.parser.TestClassSourceJavaParser.parseJavaTestSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
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

	public static class CoverageResolutionAssert extends AbstractAssert<CoverageResolutionAssert, CoverageResolution> {

		private CoverageResolutionAssert(final CoverageResolution actual) {
			super(actual, CoverageResolutionAssert.class);
		}

		public static CoverageResolutionAssert assertThat(final CoverageResolution actual) {
			return new CoverageResolutionAssert(actual);
		}

		public CoverageResolutionAssert isUnambiguouslyResolved() {
			if (actual.result.isEmpty()) {
				failWithMessage("Expected some unambiguously resolved coverage result but was empty!");
			} else if (!actual.result.getUnresolvedMethods().isEmpty()
					|| !actual.result.getAmbiguousCoverage().isEmpty()) {
				failWithMessage(
						"Expected unambiguously resolved coverage result but unresolved methods is %s and "
								+ "ambiguous coverage is %s",
						actual.result.getUnresolvedMethods(), actual.result.getAmbiguousCoverage());
			}

			return this;
		}

		public CoverageResolutionAssert hasUnresolvedOnly(final ParsedMethod... expectedUnreseolved) {
			if (!actual.result.getResolvedMethods().isEmpty() || !actual.result.getAmbiguousCoverage().isEmpty()) {
				failWithMessage(
						"Expected only unresolved parsed methods but resolved methods is %s and "
								+ "ambiguous coverage is %s",
						actual.result.getResolvedMethods(), actual.result.getAmbiguousCoverage());
			}

			try {
				Assertions.assertThat(actual.result.getUnresolvedMethods()).containsOnly(expectedUnreseolved);
			} catch (final AssertionError ae) {
				failWithMessage("Expected only the following unresolved parsed methods: %s", ae.getMessage());
			}

			return this;
		}

		public CoverageResolutionAssert isEmpty() {
			if (!actual.result.getAmbiguousCoverage().isEmpty()) {
				failWithMessage("Expected empty coverage result but ambiguous coverage is %s.",
						actual.result.getAmbiguousCoverage());
			} else if (!actual.result.getResolvedMethods().isEmpty()) {
				failWithMessage("Expected empty coverage result but resolved methods is %s.",
						actual.result.getResolvedMethods());
			} else if (!actual.result.getUnresolvedMethods().isEmpty()) {
				failWithMessage("Expected empty coverage result but unresolved methods is %s.",
						actual.result.getUnresolvedMethods());
			}

			return this;
		}
	}

	/**
	 * Wrapper class that holds the raw data as well the result. Raw data lists are sorted by line number.
	 */
	public static class CoverageResolution {

		private final List<ParsedMethod> parsedMethods;
		private final List<MethodWithCoverageInfo> methodCoverage;
		private final CoverageResult result;

		private CoverageResolution(final Set<ParsedMethod> parsedMethods,
				final Set<MethodWithCoverageInfo> methodCoverage, final CoverageResult result) {
			this.parsedMethods = parsedMethods.stream().sorted(Comparator.comparing(ParsedMethod::getCodeColumn))
					.collect(Collectors.toList());
			this.methodCoverage = methodCoverage.stream().sorted(Comparator.comparing(MethodWithCoverageInfo::getLine))
					.collect(Collectors.toList());
			this.result = result;
		}

		public List<ParsedMethod> getParsedMethods() {
			return parsedMethods;
		}

		public List<MethodWithCoverageInfo> getMethodCoverage() {
			return methodCoverage;
		}

		public List<MethodWithCoverageInfo> getCoveredMethods() {
			return methodCoverage.stream().filter(mwci -> mwci.getCoveredInstructionCount() > 0)
					.collect(Collectors.toList());
		}

		public CoverageResult getResult() {
			return result;
		}
	}

	/**
	 * Parses the test class and resolves it with the JaCoCo report.
	 */
	protected CoverageResolution resolve(final Class<?> testClass, final MethodType... filterTypes) throws Exception {
		return resolve(testClass, null, null, filterTypes);
	}

	/**
	 * Parses the test class and resolves it with the JaCoCo report. The test class has to implement the
	 * {@code testInterface} and can be used in {@code execution}. This allows the test code to have parts of the
	 * class covered.
	 */
	protected <T> CoverageResolution resolve(final Class<? extends T> testClass, final Class<T> testInterface,
			Consumer<T> execution, final MethodType... filterTypes) throws Exception {
		final String testClassName = testClass.getName();

		final IRuntime runtime = new LoggerRuntime();

		final Instrumenter instrumenter = new Instrumenter(runtime);
		final Map<String, byte[]> instrumentedClasses = new ConcurrentHashMap<>();

		final ClassLoader instrumentedClassLoader = new ClassLoader() {
			@Override
			public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
				if (name.startsWith(testClassName)) {
					final byte[] instrumented = instrumentedClasses.computeIfAbsent(name, key -> {
						try (InputStream original = getTargetClass(name)) {
							return instrumenter.instrument(original, name);
						} catch (final IOException ex) {
							throw new UncheckedIOException(ex);
						}
					});

					return defineClass(name, instrumented, 0, instrumented.length);
				} else {
					return super.loadClass(name, resolve);
				}
			}
		};

		final RuntimeData data = new RuntimeData();
		runtime.startup(data);

		if (execution != null) {
			Class<?> instrumentedClass = instrumentedClassLoader.loadClass(testClassName);
			T instance = testInterface.cast(instrumentedClass.getConstructor().newInstance());
			execution.accept(instance);
		}

		final ExecutionDataStore executionData = new ExecutionDataStore();
		final SessionInfoStore sessionInfos = new SessionInfoStore();
		data.collect(executionData, sessionInfos, false);
		runtime.shutdown();

		// together with the original class definition we can calculate coverage information
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

		final Set<String> originalClassNames = Stream
				.concat(instrumentedClasses.keySet().stream(), Stream.of(testClassName)).collect(Collectors.toSet());
		for (final String originalClassName : originalClassNames) {
			try (InputStream original = getTargetClass(originalClassName)) {
				analyzer.analyzeClass(original, originalClassName);
			}
		}

		final Set<ParsedMethod> parsedMethods = parseJavaTestSource(testClass, filterTypes);

		final String xmlOutput = getCoverageReportXml(sessionInfos, executionData, coverageBuilder);
		final Set<MethodWithCoverageInfo> methodCoverage = JaCoCoHelper.getMethodCoverage(xmlOutput);

		return new CoverageResolution(parsedMethods, methodCoverage,
				CoverageResolver.with(methodCoverage).resolve(parsedMethods));
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

	protected static Entry<ParsedMethod, MethodWithCoverageInfo> resolved(final ParsedMethod method,
			MethodWithCoverageInfo coverage) {
		return new SimpleImmutableEntry<>(method, coverage);
	}

	@SafeVarargs
	protected static final Map<ParsedMethod, MethodWithCoverageInfo> coverageResult(
			final Entry<ParsedMethod, MethodWithCoverageInfo>... entries) {
		final Map<ParsedMethod, MethodWithCoverageInfo> result = new HashMap<>();

		for (final Entry<ParsedMethod, MethodWithCoverageInfo> entry : entries) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
}
