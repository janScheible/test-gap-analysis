package com.scheible.testgapanalysis.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.TryStmt;

/**
 *
 * @author sj
 */
public class ParserUtils {

	static Optional<Integer> getFirstCodeLine(final Node node) {
		return node.getChildNodes().stream()
				.filter(c -> (c instanceof BlockStmt || c instanceof ExpressionStmt) && c.getRange().isPresent())
				.flatMap(bs -> flatMapToCodeNodes(bs).stream().filter(cn -> cn.getRange().isPresent())
						.map(cn -> cn.getRange().get().begin.line))
				.sorted().findFirst();
	}

	/**
	 * Flat map a node to all direct children containing code.
	 */
	static Set<Node> flatMapToCodeNodes(final Node node) {
		final Set<Node> childrenWithCode = new HashSet<>();

		for (final Node child : node.getChildNodes()) {
			findCodeNodes(child, childrenWithCode);
		}

		return childrenWithCode;
	}

	/**
	 * Skip block, try and do statements (because they don't contain code) by recusring to their children.
	 */
	static void findCodeNodes(final Node node, final Set<Node> childrenWithCode) {
		if (node instanceof BlockStmt || node instanceof TryStmt || node instanceof DoStmt
				|| node instanceof ExpressionStmt || node instanceof VariableDeclarationExpr) {
			for (final Node child : node.getChildNodes()) {
				findCodeNodes(child, childrenWithCode);
			}
		} else if (!(node instanceof Comment || node instanceof AnnotationExpr)) {
			childrenWithCode.add(node);
		}
	}

	static String getTopLevelFqn(final Node node) {
		return ((TypeDeclaration<?>) getParents(node).get(0)).getFullyQualifiedName().get();
	}

	static List<String> getScope(final Node node) {
		final List<Node> parents = getParents(node);

		return parents.subList(1, parents.size()).stream()
				.filter(pn -> pn instanceof ClassOrInterfaceDeclaration || pn instanceof EnumDeclaration
						|| pn instanceof MethodDeclaration || isObjectCreationExprWithAnonymousClassBody(pn))
				.map(pn -> {
					return pn instanceof NodeWithSimpleName
							? ((NodeWithSimpleName) pn).getNameAsString()
							: ((ObjectCreationExpr) pn).getTypeAsString();
				}).collect(Collectors.toList());
	}

	private static boolean isObjectCreationExprWithAnonymousClassBody(final Node node) {
		return node instanceof ObjectCreationExpr && ((ObjectCreationExpr) node).getAnonymousClassBody().isPresent();
	}

	private static List<Node> getParents(final Node node) {
		final List<Node> parents = new ArrayList<>();

		Node current = node;
		while (current != null) {
			current = current.getParentNode().orElse(null);
			if (current != null && !(current instanceof CompilationUnit)) {
				parents.add(0, current);
			}
		}

		return parents;
	}

	static Map<String, String> getTypeParameters(final ConstructorDeclaration node) {
		final List<Node> parents = ParserUtils.getParents(node);
		final Map<String, String> typeParameters = new HashMap<>();

		for (int i = parents.size() - 1; i >= 0; i--) {
			if (parents.get(i) instanceof ClassOrInterfaceDeclaration) {
				final ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) parents
						.get(i);
				classOrInterfaceDeclaration.getTypeParameters().stream().forEach(tp -> typeParameters.put(
						tp.getNameAsString(),
						tp.getTypeBound().isNonEmpty() ? tp.getTypeBound().get(0).getNameAsString() : "Object"));

				if (classOrInterfaceDeclaration.isStatic()) {
					break;
				}
			}
		}
		node.getTypeParameters().stream().forEach(tp -> typeParameters.put(tp.getNameAsString(),
				tp.getTypeBound().isNonEmpty() ? tp.getTypeBound().get(0).getNameAsString() : "Object"));
		return typeParameters;
	}

	/**
	 * For example for a inner class constructor it is the outer class name.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	static Optional<String> getOuterDeclaringType(final Node node) {
		final Optional<TypeDeclaration> outerDeclaringType = node.findAncestor(TypeDeclaration.class)
				.flatMap(p -> p.findAncestor(TypeDeclaration.class));
		return outerDeclaringType.map(TypeDeclaration::getNameAsString);
	}
}
