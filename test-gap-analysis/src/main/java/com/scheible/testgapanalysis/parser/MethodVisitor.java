package com.scheible.testgapanalysis.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.Range;
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

/**
 *
 * @author sj
 */
class MethodVisitor extends VoidVisitorAdapter<Void> {

	private final String code;
	private final Set<ParsedMethod> result = new HashSet<>();

	private boolean debugMode = false;

	MethodVisitor(String code) {
		this.code = code;
	}

	Set<ParsedMethod> getResult() {
		return this.result;
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration node, Void arg) {
		// if class is marked with '//#debug' enable debug mode
		if (node.getName().getComment().map(Comment::getContent).orElse("").contains("#debug")) {
			this.debugMode = true;
		}

		super.visit(node, arg);
	}

	@Override
	public void visit(ConstructorDeclaration node, Void arg) {
		if (node.getRange().isPresent()) {
			Range range = node.getRange().get();
			String relevantCode = MaskUtils.apply(this.code, range, findMasks(node), this.debugMode);
			List<String> parameterTypes = node.getParameters().stream().map(Parameter::getType)
					.map(t -> t.asString() + (((Parameter) t.getParentNode().get()).isVarArgs() ? "[]" : ""))
					.collect(Collectors.toList());

			boolean enumConstructor = node.getParentNode().filter(pn -> pn instanceof EnumDeclaration).isPresent();
			boolean innerClassConstructor = node.getParentNode().filter(pn -> pn instanceof ClassOrInterfaceDeclaration)
					.map(pn -> (ClassOrInterfaceDeclaration) pn).map(pn -> pn.isInnerClass() && !pn.isStatic())
					.orElse(Boolean.FALSE);

			ParsedMethod.MethodType type = enumConstructor
					? ParsedMethod.MethodType.ENUM_CONSTRUCTOR
					: innerClassConstructor
							? ParsedMethod.MethodType.INNER_CLASS_CONSTRUCTOR
							: ParsedMethod.MethodType.CONSTRUCTOR;

			this.result.add(ParsedMethod.builder().setMethodType(type)
					.setTopLevelTypeFqn(ParserUtils.getTopLevelFqn(node)).setScope(ParserUtils.getScope(node))
					.setName("<init>").setRelevantCode(relevantCode).setCodeLines(ParserUtils.getCodeLines(node))
					.setCodeColumn(range.begin.column).setEmpty(!ParserUtils.containsCode(node.getBody()))
					.setParameterTypes(parameterTypes).setTypeParameters(ParserUtils.getTypeParameters(node))
					.setOuterDeclaringType(ParserUtils.getOuterDeclaringType(node)).build());
		}

		super.visit(node, arg);
	}

	@Override
	public void visit(InitializerDeclaration node, Void arg) {
		if (node.getRange().isPresent()) {
			Range range = node.getRange().get();
			String relevantCode = MaskUtils.apply(this.code, range, findMasks(node), this.debugMode);

			this.result.add(ParsedMethod.builder().setMethodType(
					node.isStatic() ? ParsedMethod.MethodType.STATIC_INITIALIZER : ParsedMethod.MethodType.INITIALIZER)
					.setTopLevelTypeFqn(ParserUtils.getTopLevelFqn(node)).setScope(ParserUtils.getScope(node))
					.setName(node.isStatic() ? "<clinit>" : "<initbl>").setRelevantCode(relevantCode)
					.setCodeLines(ParserUtils.getCodeLines(node)).setCodeColumn(range.begin.column)
					.setEmpty(!ParserUtils.containsCode(node.getBody())).setParameterCount(0).build());
		}

		super.visit(node, arg);
	}

	@Override
	public void visit(MethodDeclaration node, Void arg) {
		if (node.getRange().isPresent() && node.getBody().isPresent()) {
			Range range = node.getRange().get();
			String relevantCode = MaskUtils.apply(this.code, range, findMasks(node), this.debugMode);

			this.result.add(ParsedMethod.builder()
					.setMethodType(
							node.isStatic() ? ParsedMethod.MethodType.STATIC_METHOD : ParsedMethod.MethodType.METHOD)
					.setTopLevelTypeFqn(ParserUtils.getTopLevelFqn(node)).setScope(ParserUtils.getScope(node))
					.setName(node.getNameAsString()).setRelevantCode(relevantCode)
					.setCodeLines(ParserUtils.getCodeLines(node)).setCodeColumn(range.begin.column)
					.setEmpty(!ParserUtils.containsCode(node.getBody().get()))
					.setParameterCount(node.getParameters().size()).build());
		}

		super.visit(node, arg);
	}

	@Override
	public void visit(LambdaExpr node, Void arg) {
		if (node.getRange().isPresent()) {
			Range range = node.getRange().get();
			String relevantCode = MaskUtils.apply(this.code, range, findMasks(node), this.debugMode);

			this.result.add(ParsedMethod.builder().setMethodType(ParsedMethod.MethodType.LAMBDA_METHOD)
					.setTopLevelTypeFqn(ParserUtils.getTopLevelFqn(node)).setScope(ParserUtils.getScope(node))
					.setName("lambda").setRelevantCode(relevantCode).setCodeLines(ParserUtils.getCodeLines(node))
					.setCodeColumn(range.begin.column).setEmpty(!ParserUtils.containsCode(node.getBody()))
					.setParameterCount(node.getParameters().size()).build());
		}

		super.visit(node, arg);
	}

	private static boolean isMethod(Node node) {
		return node instanceof ConstructorDeclaration || node instanceof InitializerDeclaration
				|| node instanceof MethodDeclaration || node instanceof LambdaExpr;
	}

	private static boolean isComment(Node node) {
		return node instanceof LineComment || node instanceof BlockComment;
	}

	/**
	 * Identifies all parts of a method (constructor, (static) initializer, (lambda) method) that shouldn't be
	 * treated as part of its own code (could be either other nested methods or comments).
	 */
	private static List<Range> findMasks(Node node) {
		if (isMethod(node)) {
			List<Range> masks = new ArrayList<>();
			findMasks(node, masks);
			return masks;
		} else {
			throw new IllegalArgumentException("Only methods are allowed!");
		}
	}

	private static void findMasks(Node node, List<Range> masks) {
		addCommentRangeIfAny(node, masks);

		for (Node child : node.getChildNodes()) {
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

	private static void addCommentRangeIfAny(Node node, Collection<Range> masks) {
		if (node.getComment().isPresent() && node.getComment().get().getRange().isPresent()) {
			masks.add(node.getComment().get().getRange().get());
		}
	}
}
