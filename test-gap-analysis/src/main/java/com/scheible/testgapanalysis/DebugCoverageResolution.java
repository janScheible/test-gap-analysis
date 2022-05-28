package com.scheible.testgapanalysis;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.scheible.testgapanalysis.common.FilesUtils;
import com.scheible.testgapanalysis.jacoco.JaCoCoReportParser;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResolver;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResult;
import com.scheible.testgapanalysis.parser.JavaParser;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class DebugCoverageResolution {

	private static class ParseResult {

		private final Set<ParsedMethod> methods;
		private final int javaFileCount;

		private ParseResult(final Set<ParsedMethod> methods, final int javaFileCount) {
			this.methods = methods;
			this.javaFileCount = javaFileCount;
		}
	}

	private final JavaParser javaParser;
	private final JaCoCoReportParser jaCoCoReportParser;

	public DebugCoverageResolution(final JavaParser javaParser, final JaCoCoReportParser jaCoCoReportParser) {
		this.javaParser = javaParser;
		this.jaCoCoReportParser = jaCoCoReportParser;
	}

	public DebugCoverageResolutionReport run(final File workDir, final File sourceDir,
			final Set<File> jaCoCoReportFiles) {
		final Set<MethodWithCoverageInfo> coverageInfo = jaCoCoReportParser.getMethodCoverage(jaCoCoReportFiles);
		final ParseResult parseResult = parseMethods(sourceDir);

		final CoverageResolver resolver = CoverageResolver.with(coverageInfo);
		final CoverageResult result = resolver.resolve(parseResult.methods);

		return new DebugCoverageResolutionReport(coverageInfo.size(), FilesUtils.toRelative(workDir, jaCoCoReportFiles),
				parseResult.javaFileCount, result.getResolvedMethods(), result.getEmptyMethods(),
				result.getUnresolvedMethods(), result.getAmbiguousCoverage());
	}

	private ParseResult parseMethods(final File workingDir) throws UncheckedIOException {
		final AtomicInteger javaFileCount = new AtomicInteger(0);
		final Set<ParsedMethod> methods = new HashSet<>();

		try (Stream<Path> walkStream = Files.walk(workingDir.toPath())) {
			walkStream.filter(p -> p.toFile().isFile()).forEach(file -> {
				if (file.toString().endsWith(".java")) {
					javaFileCount.incrementAndGet();
					methods.addAll(javaParser.getMethods(FilesUtils.readUtf8(file.toFile())));
				}
			});
		} catch (IOException ex) {
			throw new UncheckedIOException("Error while reading Java sources.", ex);
		}

		return new ParseResult(methods, javaFileCount.get());
	}
}
