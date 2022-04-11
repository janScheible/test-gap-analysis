package com.scheible.testgapanalysis.parser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.scheible.testgapanalysis.common.JavaMethodUtil;

/**
 *
 * @author sj
 */
public class ParsedMethod {

	public enum MethodType {
		INITIALIZER, STATIC_INITIALIZER, CONSTRUCTOR, ENUM_CONSTRUCTOR, METHOD, STATIC_METHOD, LAMBDA_METHOD;
	}

	private final MethodType methodType;
	private final String topLevelTypeFqn;
	private final String topLevelSimpleName;
	private final List<String> scope;
	private final String enclosingSimpleName;
	private final String name;
	private final String relevantCode;
	private final int codeLine; // the line where the node of the methods starts
	private final int firstCodeLine; // the first line with real code
	private final int codeColumn;
	private final List<String> argumentTypes;

	public ParsedMethod(final MethodType methodType, final String topLevelTypeFqn, final List<String> scope,
			final String name, final String relevantCode, final int codeLine, final int firstCodeLine,
			final int codeColumn) {
		this(methodType, topLevelTypeFqn, scope, name, relevantCode, codeLine, firstCodeLine, codeColumn, null);
	}

	public ParsedMethod(final MethodType methodType, final String topLevelTypeFqn, final List<String> scope,
			final String name, final String relevantCode, final int codeLine, final int firstCodeLine,
			final int codeColumn, final List<String> argumentTypes) {
		this.methodType = methodType;
		this.topLevelTypeFqn = topLevelTypeFqn;
		this.topLevelSimpleName = JavaMethodUtil.getSimpleName(topLevelTypeFqn, ".");
		this.scope = scope;
		this.enclosingSimpleName = scope.isEmpty() ? topLevelSimpleName : scope.get(scope.size() - 1);
		this.name = name;
		this.relevantCode = relevantCode;
		this.codeLine = codeLine;
		this.firstCodeLine = firstCodeLine;
		this.codeColumn = codeColumn;
		this.argumentTypes = argumentTypes;
	}

	public MethodType getMethodType() {
		return methodType;
	}

	public boolean isInitializer() {
		return methodType == MethodType.INITIALIZER;
	}

	public boolean isStaticInitializer() {
		return methodType == MethodType.STATIC_INITIALIZER;
	}

	public boolean isConstructor() {
		return methodType == MethodType.CONSTRUCTOR;
	}

	public boolean isEnumConstructor() {
		return methodType == MethodType.ENUM_CONSTRUCTOR;
	}

	public boolean isMethod() {
		return methodType == MethodType.METHOD;
	}

	public boolean isStaticMethod() {
		return methodType == MethodType.STATIC_METHOD;
	}

	public boolean isLambdaMethod() {
		return methodType == MethodType.LAMBDA_METHOD;
	}

	public String getTopLevelTypeFqn() {
		return topLevelTypeFqn;
	}

	public String getTopLevelSimpleName() {
		return topLevelSimpleName;
	}

	public List<String> getScope() {
		return scope;
	}

	public String getEnclosingSimpleName() {
		return enclosingSimpleName;
	}

	public String getName() {
		return name;
	}

	public String getRelevantCode() {
		return relevantCode;
	}

	public int getCodeLine() {
		return codeLine;
	}

	public int getFirstCodeLine() {
		return firstCodeLine;
	}

	public int getCodeColumn() {
		return codeColumn;
	}

	public Optional<List<String>> getArgumentTypes() {
		return Optional.ofNullable(argumentTypes);
	}

	public String getDescription() {
		String description = null;

		if (isMethod()) {
			description = "#" + getName() + "(...)";
		} else if (isStaticMethod()) {
			description = "." + getName() + "(...)";
		} else if (isConstructor()) {
			description = " constructor with " + getArgumentTypes().get().size() + " arguments";
		} else if (isInitializer()) {
			description = " initializer";
		} else if (isStaticInitializer()) {
			description = " static initializer";
		} else if (isLambdaMethod()) {
			description = " lambda method";
		} else {
			throw new IllegalStateException("Unknown method type!");
		}

		return description;
	}

	@Override
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ParsedMethod) {
			final ParsedMethod other = (ParsedMethod) obj;
			return Objects.equals(methodType, other.methodType)
					&& Objects.equals(topLevelTypeFqn, other.topLevelTypeFqn)
					&& Objects.equals(topLevelSimpleName, other.topLevelSimpleName)
					&& Objects.equals(scope, other.scope)
					&& Objects.equals(enclosingSimpleName, other.enclosingSimpleName)
					&& Objects.equals(name, other.name) && Objects.equals(relevantCode, other.relevantCode)
					&& Objects.equals(codeLine, other.codeLine) && Objects.equals(firstCodeLine, other.firstCodeLine)
					&& Objects.equals(codeColumn, other.codeColumn)
					&& Objects.equals(argumentTypes, other.argumentTypes);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(methodType, topLevelTypeFqn, topLevelSimpleName, scope, enclosingSimpleName, name,
				relevantCode, codeLine, firstCodeLine, codeColumn, argumentTypes);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[methodType=" + methodType + ", topLevelTypeFqn='" + topLevelTypeFqn
				+ "', topLevelSimpleName='" + topLevelSimpleName + "', scope='" + scope + "', enclosingSimpleName='"
				+ enclosingSimpleName + "', name='" + name + "', codeLine=" + codeLine + ", firstCodeLine="
				+ firstCodeLine + ", codeColumn=" + codeColumn
				+ (argumentTypes != null ? ", argumentTypes=" + argumentTypes : "") + "]";
	}
}
