package com.scheible.testgapanalysis.parser;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;

/**
 *
 * @author sj
 */
public class JavaParser {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	public Set<ParsedMethod> getMethods(String code, String path) {
		ParserConfiguration configuration = new ParserConfiguration();
		configuration.setLanguageLevel(LanguageLevel.BLEEDING_EDGE);
		com.github.javaparser.JavaParser javaParser = new com.github.javaparser.JavaParser(configuration);

		ParseResult<CompilationUnit> parserResult = javaParser.parse(code);
		if (!parserResult.isSuccessful()) {
			logger.error("Parsing methods of '{}' failed cause: {}", path, parserResult.getProblems().stream()
					.map(Problem::getVerboseMessage).collect(Collectors.joining(", ")));
			return Collections.emptySet();
		}

		MethodVisitor methodVisitor = new MethodVisitor(code);
		parserResult.getResult().get().accept(methodVisitor, null);
		return methodVisitor.getResult();
	}
}
