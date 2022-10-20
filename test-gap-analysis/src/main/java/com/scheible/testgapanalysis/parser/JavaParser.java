package com.scheible.testgapanalysis.parser;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
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

	private static final Pattern INSTANCEOF_WITH_FINAL_WORKAROUND_PATTERN = Pattern
			.compile("\\sinstanceof\\s+?final\\s");

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	public Set<ParsedMethod> getMethods(String code, String path) {
		ParserConfiguration configuration = new ParserConfiguration();
		configuration.setLanguageLevel(LanguageLevel.BLEEDING_EDGE);
		com.github.javaparser.JavaParser javaParser = new com.github.javaparser.JavaParser(configuration);

		// needed as long https://github.com/javaparser/javaparser/issues/2445#issuecomment-964188096 is not fixed
		code = INSTANCEOF_WITH_FINAL_WORKAROUND_PATTERN.matcher(code).replaceAll(" instanceof ");

		ParseResult<CompilationUnit> parserResult = javaParser.parse(code);
		if (!parserResult.isSuccessful()) {
			this.logger.error("Parsing methods of '{}' failed cause: {}", path, parserResult.getProblems().stream()
					.map(Problem::getVerboseMessage).collect(Collectors.joining(", ")));
			return Collections.emptySet();
		}

		MethodVisitor methodVisitor = new MethodVisitor(code);
		parserResult.getResult().get().accept(methodVisitor, null);
		return methodVisitor.getResult();
	}
}
