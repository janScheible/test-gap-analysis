package com.scheible.testgapanalysis.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
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

	static int getFirstCodeLine(final Node node) {
		return node.getChildNodes().stream()
				.filter(c -> (c instanceof BlockStmt || c instanceof ExpressionStmt) && c.getRange().isPresent())
				.flatMap(bs -> flatMapToCodeNodes(bs).stream().filter(cn -> cn.getRange().isPresent())
						.map(cn -> cn.getRange().get().begin.line))
				.sorted().findFirst().orElse(node.getRange().get().begin.line);
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
		if (node instanceof BlockStmt || node instanceof TryStmt || node instanceof DoStmt) {
			for (final Node child : node.getChildNodes()) {
				findCodeNodes(child, childrenWithCode);
			}
		} else {
			childrenWithCode.add(node);
		}
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
