package com.scheible.testgapanalysis.parser;

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
import com.scheible.testgapanalysis.common.ToStringBuilder;
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

	ParsedMethod(BuilderImpl builder) {
		this.methodType = builder.methodType;
		this.topLevelTypeFqn = builder.topLevelTypeFqn;
		this.topLevelSimpleName = JavaMethodUtils.getSimpleName(builder.topLevelTypeFqn, ".");
		this.scope = Collections.unmodifiableList(new ArrayList<>(builder.scope));
		this.enclosingSimpleName = this.scope.isEmpty()
				? this.topLevelSimpleName
				: builder.scope.get(builder.scope.size() - 1);
		this.name = builder.name;
		this.relevantCode = builder.relevantCode;
		this.codeLines = Collections
				.unmodifiableList(new ArrayList<>(builder.codeLines.stream().sorted().collect(Collectors.toList())));
		this.codeColumn = builder.codeColumn;
		this.empty = builder.empty;

		this.argumentTypes = Collections.unmodifiableList(builder.argumentTypes != null
				? new ArrayList<>(builder.argumentTypes)
				: IntStream.range(0, builder.argumentCount).boxed().map(i -> "Object").collect(Collectors.toList()));

		this.argumentCount = this.argumentTypes.size();
		this.typeParameters = Collections.unmodifiableMap(
				builder.typeParameters != null ? new HashMap<>(builder.typeParameters) : Collections.emptyMap());
		this.outerDeclaringType = builder.outerDeclaringType;
	}

	public static MethodTypeStep builder() {
		return new BuilderImpl();
	}

	public boolean isInitializer() {
		return this.methodType == MethodType.INITIALIZER;
	}

	public boolean isStaticInitializer() {
		return this.methodType == MethodType.STATIC_INITIALIZER;
	}

	public boolean isConstructor() {
		return this.methodType == MethodType.CONSTRUCTOR;
	}

	public boolean isEnumConstructor() {
		return this.methodType == MethodType.ENUM_CONSTRUCTOR;
	}

	public boolean isInnerClassConstructor() {
		return this.methodType == MethodType.INNER_CLASS_CONSTRUCTOR;
	}

	public boolean isAnyConstructor() {
		return isConstructor() || isEnumConstructor() || isInnerClassConstructor();
	}

	public boolean isMethod() {
		return this.methodType == MethodType.METHOD;
	}

	public boolean isStaticMethod() {
		return this.methodType == MethodType.STATIC_METHOD;
	}

	public boolean isAnyNonLambdaMethod() {
		return isMethod() || isStaticMethod();
	}

	public boolean isLambdaMethod() {
		return this.methodType == MethodType.LAMBDA_METHOD;
	}

	public boolean containsLine(int line) {
		for (int current : this.codeLines) {
			if (current == line) {
				return true;
			}
		}

		return false;
	}

	public int getFirstCodeLine() {
		return this.codeLines.get(0);
	}

	public String getDescription() {
		String description;

		if (isMethod()) {
			description = "#" + getName() + "(...)";
		} else if (isStaticMethod()) {
			description = "." + getName() + "(...)";
		} else if (isConstructor()) {
			description = " constructor with " + getArgumentTypes().size() + " arguments";
		} else if (isInnerClassConstructor()) {
			description = " inner class constructor with " + getArgumentTypes().size() + " arguments";
		} else if (isEnumConstructor()) {
			description = " enum constructor with " + getArgumentTypes().size() + " arguments";
		} else if (isInitializer()) {
			description = " initializer";
		} else if (isStaticInitializer()) {
			description = " static initializer";
		} else if (isLambdaMethod()) {
			description = " lambda method";
		} else {
			description = " unknown type '" + this.methodType + "'";
		}

		return description;
	}

	public MethodType getMethodType() {
		return this.methodType;
	}

	public String getTopLevelTypeFqn() {
		return this.topLevelTypeFqn;
	}

	public String getTopLevelSimpleName() {
		return this.topLevelSimpleName;
	}

	public List<String> getScope() {
		return this.scope;
	}

	public String getEnclosingSimpleName() {
		return this.enclosingSimpleName;
	}

	public String getName() {
		return this.name;
	}

	public String getRelevantCode() {
		return this.relevantCode;
	}

	public List<Integer> getCodeLines() {
		return this.codeLines;
	}

	public int getCodeColumn() {
		return this.codeColumn;
	}

	public boolean isEmpty() {
		return this.empty;
	}

	public List<String> getArgumentTypes() {
		return this.argumentTypes;
	}

	public int getArgumentCount() {
		return this.argumentCount;
	}

	public Map<String, String> getTypeParameters() {
		return this.typeParameters;
	}

	public Optional<String> getOuterDeclaringType() {
		return this.outerDeclaringType;
	}

	@Override
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ParsedMethod) {
			ParsedMethod other = (ParsedMethod) obj;
			return Objects.equals(this.methodType, other.methodType)
					&& Objects.equals(this.topLevelTypeFqn, other.topLevelTypeFqn)
					&& Objects.equals(this.topLevelSimpleName, other.topLevelSimpleName)
					&& Objects.equals(this.scope, other.scope)
					&& Objects.equals(this.enclosingSimpleName, other.enclosingSimpleName)
					&& Objects.equals(this.name, other.name) && Objects.equals(this.relevantCode, other.relevantCode)
					&& Objects.equals(this.codeLines, other.codeLines) && this.codeColumn == other.codeColumn
					&& this.empty == other.empty && Objects.equals(this.argumentTypes, other.argumentTypes)
					&& this.argumentCount == other.argumentCount
					&& Objects.equals(this.typeParameters, other.typeParameters)
					&& Objects.equals(this.outerDeclaringType, other.outerDeclaringType);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.methodType, this.topLevelTypeFqn, this.topLevelSimpleName, this.scope,
				this.enclosingSimpleName, this.name, this.relevantCode, this.codeLines, this.codeColumn, this.empty,
				this.argumentTypes, this.argumentCount, this.typeParameters, this.outerDeclaringType);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("methodType", this.methodType)
				.append("topLevelTypeFqn", this.topLevelTypeFqn).append("topLevelSimpleName", this.topLevelSimpleName)
				.append("scope", this.scope).append("enclosingSimpleName", this.enclosingSimpleName)
				.append("name", this.name)
				.append("relevantCode", ToStringBuilder.shorten(this.relevantCode.replaceAll("\\R", ""), 30))
				.append("codeLines", this.codeLines).append("codeColumn", this.codeColumn).append("empty", this.empty)
				.append("argumentTypes", this.argumentTypes).append("argumentCount", this.argumentCount)
				.append("typeParameters", this.typeParameters).append("outerDeclaringType", this.outerDeclaringType)
				.build();
	}
}
