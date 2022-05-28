package com.scheible.testgapanalysis.parser;

import java.util.Set;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.ast.CompilationUnit;

/**
 *
 * @author sj
 */
public class JavaParser {

	public Set<ParsedMethod> getMethods(final String code) {
		final ParserConfiguration configuration = new ParserConfiguration();
		configuration.setLanguageLevel(LanguageLevel.BLEEDING_EDGE);
		final com.github.javaparser.JavaParser javaParser = new com.github.javaparser.JavaParser(configuration);

		final ParseResult<CompilationUnit> parserResult = javaParser.parse(code);
		if (!parserResult.isSuccessful()) {
			throw new ParseProblemException(parserResult.getProblems());
		}

		final MethodVisitor methodVisitor = new MethodVisitor(code);
		parserResult.getResult().get().accept(methodVisitor, null);
		return methodVisitor.getResult();
	}
}
