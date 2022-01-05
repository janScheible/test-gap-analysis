package com.scheible.testgapanalysis.parser;

import static com.scheible.testgapanalysis.parser.ParserUtils.getFirstCodeLine;
import static com.scheible.testgapanalysis.parser.ParserUtils.getScope;
import static com.scheible.testgapanalysis.parser.ParserUtils.getTopLevelFqn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.type.Type;
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

		final CompilationUnit compilationUnit = StaticJavaParser.parse(code);
		compilationUnit.accept(new VoidVisitorAdapter<Void>() {
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
							.map(Type::asString).map(t -> t.contains(".") ? t.substring(t.lastIndexOf('.') + 1) : t)
							.collect(Collectors.toList());

					result.add(new ParsedMethod(MethodType.CONSTRUCTOR, getTopLevelFqn(node), getScope(node), "<init>",
							relevantCode, getFirstCodeLine(node), range.begin.column, argumentTypes));
				}

				super.visit(node, arg);
			}

			@Override
			public void visit(final InitializerDeclaration node, final Void arg) {
				if (node.getRange().isPresent()) {
					final Range range = node.getRange().get();
					final String relevantCode = Masker.apply(code, range, findMasks(node), debugMode.get());

					result.add(
							new ParsedMethod(node.isStatic() ? MethodType.STATIC_INITIALIZER : MethodType.INITIALIZER,
									getTopLevelFqn(node), getScope(node), node.isStatic() ? "<clinit>" : "<initbl>",
									relevantCode, getFirstCodeLine(node), range.begin.column));
				}

				super.visit(node, arg);
			}

			@Override
			public void visit(final MethodDeclaration node, final Void arg) {
				if (node.getRange().isPresent() && node.getBody().isPresent()) {
					final Range range = node.getRange().get();
					final String relevantCode = Masker.apply(code, range, findMasks(node), debugMode.get());

					result.add(new ParsedMethod(node.isStatic() ? MethodType.STATIC_METHOD : MethodType.METHOD,
							getTopLevelFqn(node), getScope(node), node.getNameAsString(), relevantCode,
							getFirstCodeLine(node), range.begin.column));
				}

				super.visit(node, arg);
			}

			@Override
			public void visit(final LambdaExpr node, final Void arg) {
				if (node.getRange().isPresent()) {
					final Range range = node.getRange().get();
					final String relevantCode = Masker.apply(code, range, findMasks(node), debugMode.get());

					result.add(new ParsedMethod(MethodType.LAMBDA_METHOD, getTopLevelFqn(node), getScope(node),
							"lambda", relevantCode, getFirstCodeLine(node), range.begin.column));
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
