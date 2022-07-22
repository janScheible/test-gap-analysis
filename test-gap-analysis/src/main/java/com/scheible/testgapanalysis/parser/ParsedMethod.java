package com.scheible.testgapanalysis.parser;

import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.scheible.testgapanalysis.common.JavaMethodUtils;
import com.scheible.testgapanalysis.parser.ParsedMethodBuilder.BuilderImpl;
import com.scheible.testgapanalysis.parser.ParsedMethodBuilder.MethodTypeStep;

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
	private final Optional<String> outerDeclaringType;

	ParsedMethod(final BuilderImpl builder) {
		this.methodType = builder.methodType;
		this.topLevelTypeFqn = builder.topLevelTypeFqn;
		this.topLevelSimpleName = JavaMethodUtils.getSimpleName(builder.topLevelTypeFqn, ".");
		this.scope = Collections.unmodifiableList(new ArrayList<>(builder.scope));
		this.enclosingSimpleName = scope.isEmpty() ? topLevelSimpleName : builder.scope.get(builder.scope.size() - 1);
		this.name = builder.name;
		this.relevantCode = builder.relevantCode;
		this.codeLines = Collections
				.unmodifiableList(new ArrayList<>(builder.codeLines.stream().sorted().collect(Collectors.toList())));
		this.codeColumn = builder.codeColumn;
		this.empty = builder.empty;

		this.argumentTypes = Collections.unmodifiableList(builder.argumentTypes != null
				? new ArrayList<>(builder.argumentTypes)
				: IntStream.range(0, builder.argumentCount).boxed().map(i -> "Object").collect(Collectors.toList()));

		argumentCount = argumentTypes.size();
		this.typeParameters = Collections
				.unmodifiableMap(builder.typeParameters != null ? new HashMap<>(builder.typeParameters) : emptyMap());
		this.outerDeclaringType = builder.outerDeclaringType;
	}

	public static MethodTypeStep builder() {
		return new BuilderImpl();
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

	public MethodType getMethodType() {
		return methodType;
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

	public int getCodeColumn() {
		return codeColumn;
	}

	public boolean isEmpty() {
		return empty;
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
		return outerDeclaringType;
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
				+ (outerDeclaringType.isPresent() ? ", outerDeclaringType=" + outerDeclaringType.get() : "") + "]";
	}
}
