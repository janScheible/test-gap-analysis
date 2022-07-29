package com.scheible.testgapanalysis.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.TypeParameter;

/**
 *
 * @author sj
 */
public abstract class ParserUtils {

	private ParserUtils() {
	}

	/**
	 * Checks for any child nodes that are not comments.
	 */
	static boolean containsCode(Node node) {
		List<Node> codeNodes = node.stream(Node.TreeTraversal.PREORDER).filter(n -> !(n instanceof Comment))
				.collect(Collectors.toList());
		codeNodes.remove(node);
		return !codeNodes.isEmpty();
	}

	static List<Integer> getCodeLines(Node node) {
		return IntStream.rangeClosed(node.getRange().get().begin.line, node.getRange().get().end.line).boxed()
				.collect(Collectors.toList());
	}

	static String getTopLevelFqn(Node node) {
		return ((TypeDeclaration<?>) getParents(node).get(0)).getFullyQualifiedName().get();
	}

	static List<String> getScope(Node node) {
		List<Node> parents = getParents(node);

		return parents.subList(1, parents.size()).stream()
				.filter(pn -> pn instanceof ClassOrInterfaceDeclaration || pn instanceof EnumDeclaration
						|| pn instanceof RecordDeclaration || pn instanceof MethodDeclaration
						|| isObjectCreationExprWithAnonymousClassBody(pn))
				.map(pn -> {
					return pn instanceof NodeWithSimpleName
							? ((NodeWithSimpleName) pn).getNameAsString()
							: ((ObjectCreationExpr) pn).getTypeAsString();
				}).collect(Collectors.toList());
	}

	private static boolean isObjectCreationExprWithAnonymousClassBody(Node node) {
		return node instanceof ObjectCreationExpr && ((ObjectCreationExpr) node).getAnonymousClassBody().isPresent();
	}

	private static List<Node> getParents(Node node) {
		List<Node> parents = new ArrayList<>();

		Node current = node;
		while (current != null) {
			current = current.getParentNode().orElse(null);
			if (current != null && !(current instanceof CompilationUnit)) {
				parents.add(0, current);
			}
		}

		return parents;
	}

	static Map<String, String> getTypeParameters(ConstructorDeclaration node) {
		List<Node> parents = ParserUtils.getParents(node);
		NodeList<TypeParameter> typeParameters = new NodeList<>();

		for (int i = parents.size() - 1; i >= 0; i--) {
			if (parents.get(i) instanceof ClassOrInterfaceDeclaration) {
				ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) parents.get(i);
				typeParameters.addAll(classOrInterfaceDeclaration.getTypeParameters());

				if (classOrInterfaceDeclaration.isStatic()) {
					break;
				}
			} else if (parents.get(i) instanceof RecordDeclaration) {
				RecordDeclaration recordDeclaration = (RecordDeclaration) parents.get(i);
				typeParameters.addAll(recordDeclaration.getTypeParameters());

				break;
			}
		}
		typeParameters.addAll(node.getTypeParameters());

		return typeParameters.stream().collect(Collectors.toMap(TypeParameter::getNameAsString,
				tp -> tp.getTypeBound().isNonEmpty() ? tp.getTypeBound().get(0).getNameAsString() : "Object"));
	}

	/**
	 * For example for a inner class constructor it is the outer class name.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	static Optional<String> getOuterDeclaringType(Node node) {
		Optional<TypeDeclaration> outerDeclaringType = node.findAncestor(TypeDeclaration.class)
				.flatMap(p -> p.findAncestor(TypeDeclaration.class));
		return outerDeclaringType.map(TypeDeclaration::getNameAsString);
	}
}
