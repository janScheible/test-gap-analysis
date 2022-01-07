package com.scheible.testgapanalysis;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheible.testgapanalysis.common.Files2;
import com.scheible.testgapanalysis.jacoco.JaCoCoHelper;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResolver;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResult;
import com.scheible.testgapanalysis.parser.JavaParserHelper;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class DebugCoverageResolution {

	private static final Logger logger = LoggerFactory.getLogger(DebugCoverageResolution.class);

	public static void run(final File workingDir) {
		final Set<MethodWithCoverageInfo> coverageInfo = loadJaCoCoCoverage(workingDir);
		final Set<ParsedMethod> methods = parseMethods(workingDir);

		final CoverageResolver resolver = CoverageResolver.with(methods, coverageInfo);
		final CoverageResult result = resolver.resolve(methods);

		logger.info("Resolved methods:");
		result.getResolved().entrySet().forEach(e -> logger.info(" - {} -> {}", e.getKey(), e.getValue()));

		logger.info("Unresolvable methods (no coverage information available):");
		result.getUnresolved().forEach(u -> logger.info(" - {}", u));
	}

	private static Set<ParsedMethod> parseMethods(final File workingDir) throws UncheckedIOException {
		final AtomicInteger javaFileCount = new AtomicInteger(0);
		final Set<ParsedMethod> methods = new HashSet<>();

		try (Stream<Path> walkStream = Files.walk(Paths.get(workingDir.getAbsolutePath(), "src\\main\\java"))) {
			walkStream.filter(p -> p.toFile().isFile()).forEach(file -> {
				if (file.toString().endsWith(".java")) {
					javaFileCount.incrementAndGet();
					methods.addAll(JavaParserHelper.getMethods(Files2.readUtf8(file.toFile())));
				}
			});
		} catch (IOException ex) {
			throw new UncheckedIOException("Error while reading Java sources.", ex);
		}

		logger.info("Found {} Java files.", javaFileCount.get());

		return methods;
	}

	private static Set<MethodWithCoverageInfo> loadJaCoCoCoverage(final File workingDir) {
		final Set<File> jaCoCoFiles = JaCoCoHelper.findJaCoCoFiles(workingDir);
		final Set<MethodWithCoverageInfo> coverageInfo = JaCoCoHelper.getMethodCoverage(jaCoCoFiles);

		if (coverageInfo.isEmpty()) {
			logger.info("No coverage info available!");
		} else {
			logger.info("Found coverage info about {} methods in {}.", coverageInfo, jaCoCoFiles);
		}

		return coverageInfo;
	}
}
