package com.scheible.testgapanalysis.parser;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.scheible.testgapanalysis.common.JavaMethodUtil;

/**
 *
 * @author sj
 */
public class ParsedMethod {

	public enum MethodType {
		INITIALIZER, STATIC_INITIALIZER, CONSTRUCTOR, ENUM_CONSTRUCTOR, INNER_CLASS_CONSTRUCTOR, METHOD, STATIC_METHOD, LAMBDA_METHOD;
	}

	private final MethodType methodType;
	private final String topLevelTypeFqn;
	private final String topLevelSimpleName;
	private final List<String> scope;
	private final String enclosingSimpleName;
	private final String name;
	private final String relevantCode;
	private final List<Integer> codeLines;
	private final int codeColumn;
	private final boolean empty;
	private final int argumentCount;
	private final List<String> argumentTypes;
	private final Map<String, String> typeParameters;
	private final String outerDeclaringType;

	public ParsedMethod(final MethodType methodType, final String topLevelTypeFqn, final List<String> scope,
			final String name, final String relevantCode, final List<Integer> codeLines, final int codeColumn,
			final boolean empty, final int argumentCount) {
		this(methodType, topLevelTypeFqn, scope, name, relevantCode, codeLines, codeColumn, empty,
				IntStream.range(0, argumentCount).boxed().map(i -> "Object").collect(Collectors.toList()), emptyMap(),
				Optional.empty());
	}

	public ParsedMethod(final MethodType methodType, final String topLevelTypeFqn, final List<String> scope,
			final String name, final String relevantCode, final List<Integer> codeLines, final int codeColumn,
			final boolean empty, final List<String> argumentTypes, final Map<String, String> typeParameters,
			final Optional<String> outerDeclaringType) {
		this.methodType = methodType;
		this.topLevelTypeFqn = topLevelTypeFqn;
		this.topLevelSimpleName = JavaMethodUtil.getSimpleName(topLevelTypeFqn, ".");
		this.scope = unmodifiableList(scope);
		this.enclosingSimpleName = scope.isEmpty() ? topLevelSimpleName : scope.get(scope.size() - 1);
		this.name = name;
		this.relevantCode = relevantCode;
		this.codeLines = unmodifiableList(codeLines.stream().sorted().collect(Collectors.toList()));
		this.codeColumn = codeColumn;
		this.empty = empty;
		this.argumentTypes = unmodifiableList(argumentTypes);
		argumentCount = argumentTypes.size();
		this.typeParameters = unmodifiableMap(typeParameters);
		this.outerDeclaringType = outerDeclaringType.orElse(null);
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

	public boolean isInnerClassConstructor() {
		return methodType == MethodType.INNER_CLASS_CONSTRUCTOR;
	}

	public boolean isAnyConstructor() {
		return isConstructor() || isEnumConstructor() || isInnerClassConstructor();
	}

	public boolean isMethod() {
		return methodType == MethodType.METHOD;
	}

	public boolean isStaticMethod() {
		return methodType == MethodType.STATIC_METHOD;
	}

	public boolean isAnyNonLambdaMethod() {
		return isMethod() || isStaticMethod();
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

	public List<Integer> getCodeLines() {
		return codeLines;
	}

	public boolean containsLine(final int line) {
		for (final int current : codeLines) {
			if (current == line) {
				return true;
			}
		}

		return false;
	}

	public int getFirstCodeLine() {
		return codeLines.get(0);
	}

	public boolean isEmpty() {
		return empty;
	}

	public int getCodeColumn() {
		return codeColumn;
	}

	public List<String> getArgumentTypes() {
		return argumentTypes;
	}

	public int getArgumentCount() {
		return argumentCount;
	}

	public Map<String, String> getTypeParameters() {
		return typeParameters;
	}

	public Optional<String> getOuterDeclaringType() {
		return Optional.of(outerDeclaringType);
	}

	public String getDescription() {
		String description = null;

		if (isMethod()) {
			description = "#" + getName() + "(...)";
		} else if (isStaticMethod()) {
			description = "." + getName() + "(...)";
		} else if (isConstructor()) {
			description = " constructor with " + getArgumentTypes().size() + " arguments";
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
					&& Objects.equals(codeLines, other.codeLines) && Objects.equals(codeColumn, other.codeColumn)
					&& Objects.equals(empty, other.empty) && Objects.equals(argumentTypes, other.argumentTypes)
					&& Objects.equals(argumentCount, other.argumentCount)
					&& Objects.equals(typeParameters, other.typeParameters)
					&& Objects.equals(outerDeclaringType, other.outerDeclaringType);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(methodType, topLevelTypeFqn, topLevelSimpleName, scope, enclosingSimpleName, name,
				relevantCode, codeLines, codeColumn, empty, argumentTypes, argumentCount, typeParameters,
				outerDeclaringType);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[methodType=" + methodType + ", topLevelTypeFqn='" + topLevelTypeFqn
				+ "', topLevelSimpleName='" + topLevelSimpleName + "', scope='" + scope + "', enclosingSimpleName='"
				+ enclosingSimpleName + "', name='" + name + "', codeLines=" + codeLines + ", codeColumn=" + codeColumn
				+ ", empty=" + empty + (!argumentTypes.isEmpty() ? ", parameterTypes=" + argumentTypes : "")
				+ ", argumentCount=" + argumentCount
				+ (!typeParameters.isEmpty() ? ", typeParameters=" + typeParameters : "")
				+ (outerDeclaringType != null ? ", outerDeclaringType=" + outerDeclaringType : "") + "]";
	}
}
