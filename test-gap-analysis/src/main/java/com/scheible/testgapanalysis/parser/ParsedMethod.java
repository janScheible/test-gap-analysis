package com.scheible.testgapanalysis.parser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author sj
 */
public class ParsedMethod {

	public enum MethodType {
		INITIALIZER, STATIC_INITIALIZER, CONSTRUCTOR, METHOD, STATIC_METHOD, LAMBDA_METHOD;
	}

	private final MethodType methodType;
	private final String topLevelTypeFqn;
	private final List<String> scope;
	private final String name;
	private final String relevantCode;
	private final int firstCodeLine;
	private final int codeColumn;
	private final List<String> argumentTypes;

	public ParsedMethod(final MethodType methodType, final String topLevelTypeFqn, final List<String> scope,
			final String name, final String relevantCode, final int firstCodeLine, final int codeColumn) {
		this(methodType, topLevelTypeFqn, scope, name, relevantCode, firstCodeLine, codeColumn, null);
	}

	public ParsedMethod(final MethodType methodType, final String topLevelTypeFqn, final List<String> scope,
			final String name, final String relevantCode, final int firstCodeLine, final int codeColumn,
			final List<String> argumentTypes) {
		this.methodType = methodType;
		this.topLevelTypeFqn = topLevelTypeFqn;
		this.scope = scope;
		this.name = name;
		this.relevantCode = relevantCode;
		this.firstCodeLine = firstCodeLine;
		this.codeColumn = codeColumn;
		this.argumentTypes = argumentTypes;
	}

	public MethodType getMethodType() {
		return methodType;
	}

	public String getTopLevelTypeFqn() {
		return topLevelTypeFqn;
	}

	public List<String> getScope() {
		return scope;
	}

	public String getName() {
		return name;
	}

	public String getRelevantCode() {
		return relevantCode;
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

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ParsedMethod) {
			final ParsedMethod other = (ParsedMethod) obj;
			return Objects.equals(methodType, other.methodType)
					&& Objects.equals(topLevelTypeFqn, other.topLevelTypeFqn) && Objects.equals(scope, other.scope)
					&& Objects.equals(name, other.name) && Objects.equals(relevantCode, other.relevantCode)
					&& Objects.equals(firstCodeLine, other.firstCodeLine)
					&& Objects.equals(argumentTypes, other.argumentTypes);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(methodType, topLevelTypeFqn, scope, name, relevantCode, firstCodeLine, argumentTypes);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[methodType=" + methodType + ", topLevelTypeFqn='" + topLevelTypeFqn
				+ "', scope='" + scope + "', name='" + name + "', firstCodeLine=" + firstCodeLine
				+ (argumentTypes != null ? ", argumentTypes=" + argumentTypes : "") + "]";
	}
}
