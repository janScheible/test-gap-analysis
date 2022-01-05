package com.scheible.testgapanalysis.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;

/**
 *
 * @author sj
 */
public class ParserUtils {

	static int getFirstCodeLine(final Node node) {
		return node.getChildNodes().stream().filter(c -> c instanceof BlockStmt && c.getRange().isPresent())
				.flatMap(bs -> bs.getChildNodes().stream().filter(cn -> cn.getRange().isPresent())
						.map(cn -> cn.getRange().get().begin.line))
				.sorted().findFirst().orElse(node.getRange().get().begin.line);
	}

	static String getTopLevelFqn(final Node node) {
		return ((ClassOrInterfaceDeclaration) getParents(node).get(0)).getFullyQualifiedName().get();
	}

	static List<String> getScope(final Node node) {
		final List<Node> parents = getParents(node);

		return parents
				.subList(1, parents.size()).stream().filter(pn -> pn instanceof ClassOrInterfaceDeclaration
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
}
