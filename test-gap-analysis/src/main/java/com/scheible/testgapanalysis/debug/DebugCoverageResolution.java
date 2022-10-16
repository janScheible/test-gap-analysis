package com.scheible.testgapanalysis.debug;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.scheible.testgapanalysis.common.FilesUtils;
import com.scheible.testgapanalysis.jacoco.InstrumentedMethod;
import com.scheible.testgapanalysis.jacoco.JaCoCoReportParser;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResolver;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResult;
import com.scheible.testgapanalysis.parser.JavaParser;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class DebugCoverageResolution {

	private final JavaParser javaParser;
	private final JaCoCoReportParser jaCoCoReportParser;

	public DebugCoverageResolution(JavaParser javaParser, JaCoCoReportParser jaCoCoReportParser) {
		this.javaParser = javaParser;
		this.jaCoCoReportParser = jaCoCoReportParser;
	}

	public DebugCoverageResolutionReport run(File workDir, File sourceDir, Set<File> jaCoCoReportFiles) {
		Set<InstrumentedMethod> instrumentedMethods = this.jaCoCoReportParser.getInstrumentedMethods(jaCoCoReportFiles);
		ParseResult parseResult = parseMethods(sourceDir);

		CoverageResolver resolver = CoverageResolver.with(instrumentedMethods);
		CoverageResult result = resolver.resolve(parseResult.methods);

		return DebugCoverageResolutionReport.builder().setCoverageInfoCount(instrumentedMethods.size())
				.setJaCoCoReportFiles(FilesUtils.toRelative(workDir, jaCoCoReportFiles))
				.setJavaFileCount(parseResult.javaFileCount).setResolved(result.getResolvedMethods())
				.setEmpty(result.getEmptyMethods()).setUnresolved(result.getUnresolvedMethods())
				.setAmbiguousCoverage(result.getAmbiguousCoverage()).build();
	}

	private ParseResult parseMethods(File workingDir) throws UncheckedIOException {
		PathMatcher javaFileMatcher = FileSystems.getDefault().getPathMatcher("glob:**.java");

		try (Stream<Path> walkStream = Files.walk(workingDir.toPath())) {
			Set<File> javaFiles = walkStream.filter(javaFileMatcher::matches).map(Path::toFile)
					.collect(Collectors.toSet());

			Set<ParsedMethod> methods = javaFiles.stream()
					.flatMap(f -> this.javaParser.getMethods(FilesUtils.readUtf8(f)).stream())
					.collect(Collectors.toSet());

			return new ParseResult(methods, javaFiles.size());
		} catch (IOException ex) {
			throw new UncheckedIOException("Error while reading Java sources.", ex);
		}
	}

	private static class ParseResult {

		private final Set<ParsedMethod> methods;
		private final int javaFileCount;

		private ParseResult(Set<ParsedMethod> methods, int javaFileCount) {
			this.methods = methods;
			this.javaFileCount = javaFileCount;
		}
	}
}
