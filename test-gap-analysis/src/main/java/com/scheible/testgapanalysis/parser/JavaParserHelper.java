package com.scheible.testgapanalysis.parser;

import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.scheible.testgapanalysis.common.Sha256;

/**
 *
 * @author sj
 */
public class JavaParserHelper {

	public static Set<ParsedMethod> getMethods(final String code) {
		final CompilationUnit compilationUnit = StaticJavaParser.parse(code);
		final Set<ParsedMethod> result = new HashSet<>();

		compilationUnit.accept(new VoidVisitorAdapter<Void>() {
			@Override
			public void visit(final ClassOrInterfaceDeclaration n, final Void arg) {
				if (!n.isInterface()) {
					n.getMethods().forEach(m -> {
						if (!m.isAbstract()) {
							result.add(new ParsedMethod(n.getFullyQualifiedName().orElse("") + dollaryName(n),
									m.getNameAsString(), m.getBody().map(b -> Sha256.hash(b.toString())).orElse("-")));
						}
					});
				}

				super.visit(n, arg);
			}
		}, null);

		return result;
	}

	private static String dollaryName(final TypeDeclaration<?> n) {
		if (n.isNestedType()) {
			return dollaryName((TypeDeclaration<?>) n.getParentNode().get()) + "$" + n.getNameAsString();
		} else {
			return "";
		}
	}
}
