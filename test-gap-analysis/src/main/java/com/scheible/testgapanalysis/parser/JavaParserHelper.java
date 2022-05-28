package com.scheible.testgapanalysis.parser;

import static java.lang.Boolean.FALSE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
public class JavaParserHelper {

	public static Set<ParsedMethod> getMethods(final String code) {
		final Set<ParsedMethod> result = new HashSet<>();

		final AtomicBoolean debugMode = new AtomicBoolean(false);

		final ParserConfiguration configuration = new ParserConfiguration();
		configuration.setLanguageLevel(LanguageLevel.BLEEDING_EDGE);
		final JavaParser javaParser = new JavaParser(configuration);

		final ParseResult<CompilationUnit> parserResult = javaParser.parse(code);
		if (!parserResult.isSuccessful()) {
			throw new ParseProblemException(parserResult.getProblems());
		}

		parserResult.getResult().get().accept(new VoidVisitorAdapter<Void>() {
			@Override
			public void visit(final ClassOrInterfaceDeclaration node, final Void arg) {
				// if class is marked with '//#debug' enable debug mode
				if (node.getName().getComment().map(Comment::getContent).orElse("").contains("#debug")) {
					debugMode.set(true);
				}

				super.visit(node, arg);
			}

			@Override
			public void visit(final ConstructorDeclaration node, final Void arg) {
				if (node.getRange().isPresent()) {
					final Range range = node.getRange().get();
					final String relevantCode = Masker.apply(code, range, findMasks(node), debugMode.get());
					final List<String> argumentTypes = node.getParameters().stream().map(Parameter::getType)
							.map(t -> t.asString() + (((Parameter) t.getParentNode().get()).isVarArgs() ? "[]" : ""))
							.collect(Collectors.toList());

					final boolean enumConstructor = node.getParentNode().filter(pn -> pn instanceof EnumDeclaration)
							.isPresent();
					final boolean innerClassConstructor = node.getParentNode()
							.filter(pn -> pn instanceof ClassOrInterfaceDeclaration)
							.map(pn -> (ClassOrInterfaceDeclaration) pn).map(pn -> pn.isInnerClass() && !pn.isStatic())
							.orElse(FALSE);

					final MethodType type = enumConstructor
							? MethodType.ENUM_CONSTRUCTOR
							: innerClassConstructor ? MethodType.INNER_CLASS_CONSTRUCTOR : MethodType.CONSTRUCTOR;

					result.add(new ParsedMethod(type, ParserUtils.getTopLevelFqn(node), ParserUtils.getScope(node),
							"<init>", relevantCode, ParserUtils.getCodeLines(node), range.begin.column,
							!ParserUtils.containsCode(node.getBody()), argumentTypes,
							ParserUtils.getTypeParameters(node), ParserUtils.getOuterDeclaringType(node)));
				}

				super.visit(node, arg);
			}

			@Override
			public void visit(final InitializerDeclaration node, final Void arg) {
				if (node.getRange().isPresent()) {
					final Range range = node.getRange().get();
					final String relevantCode = Masker.apply(code, range, findMasks(node), debugMode.get());

					result.add(new ParsedMethod(
							node.isStatic() ? MethodType.STATIC_INITIALIZER : MethodType.INITIALIZER,
							ParserUtils.getTopLevelFqn(node), ParserUtils.getScope(node),
							node.isStatic() ? "<clinit>" : "<initbl>", relevantCode, ParserUtils.getCodeLines(node),
							range.begin.column, !ParserUtils.containsCode(node.getBody()), 0));
				}

				super.visit(node, arg);
			}

			@Override
			public void visit(final MethodDeclaration node, final Void arg) {
				if (node.getRange().isPresent() && node.getBody().isPresent()) {
					final Range range = node.getRange().get();
					final String relevantCode = Masker.apply(code, range, findMasks(node), debugMode.get());

					result.add(new ParsedMethod(node.isStatic() ? MethodType.STATIC_METHOD : MethodType.METHOD,
							ParserUtils.getTopLevelFqn(node), ParserUtils.getScope(node), node.getNameAsString(),
							relevantCode, ParserUtils.getCodeLines(node), range.begin.column,
							!ParserUtils.containsCode(node.getBody().get()), node.getParameters().size()));
				}

				super.visit(node, arg);
			}

			@Override
			public void visit(final LambdaExpr node, final Void arg) {
				if (node.getRange().isPresent()) {
					final Range range = node.getRange().get();
					final String relevantCode = Masker.apply(code, range, findMasks(node), debugMode.get());

					result.add(new ParsedMethod(MethodType.LAMBDA_METHOD, ParserUtils.getTopLevelFqn(node),
							ParserUtils.getScope(node), "lambda", relevantCode, ParserUtils.getCodeLines(node),
							range.begin.column, !ParserUtils.containsCode(node.getBody()),
							node.getParameters().size()));
				}

				super.visit(node, arg);
			}
		}, null);

		return result;
	}

	private static boolean isMethod(final Node node) {
		return node instanceof ConstructorDeclaration || node instanceof InitializerDeclaration
				|| node instanceof MethodDeclaration || node instanceof LambdaExpr;
	}

	private static boolean isComment(final Node node) {
		return node instanceof LineComment || node instanceof BlockComment;
	}

	/**
	 * Identifies all parts of a method (constructor, (static) initializer, (lambda) method) that shouldn't be
	 * treated as part of its own code (could be either other nested methods or comments).
	 */
	private static List<Range> findMasks(final Node node) {
		if (isMethod(node)) {
			final List<Range> masks = new ArrayList<>();
			findMasks(node, masks);
			return masks;
		} else {
			throw new IllegalArgumentException("Only methods are allowed!");
		}
	}

	private static void findMasks(final Node node, final List<Range> masks) {
		addCommentRangeIfAny(node, masks);

		for (final Node child : node.getChildNodes()) {
			if (isMethod(child) || isComment(child)) {
				if (child.getRange().isPresent()) {
					masks.add(child.getRange().get());
				}

				addCommentRangeIfAny(child, masks);
			} else {
				findMasks(child, masks);
			}
		}
	}

	private static void addCommentRangeIfAny(final Node node, final Collection<Range> masks) {
		if (node.getComment().isPresent() && node.getComment().get().getRange().isPresent()) {
			masks.add(node.getComment().get().getRange().get());
		}
	}
}
