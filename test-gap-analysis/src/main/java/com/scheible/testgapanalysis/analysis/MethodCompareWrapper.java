package com.scheible.testgapanalysis.analysis;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 * Wrapper for identifying unchanged methods in different states of the codebase. For example source code
 * positions are ignored because line numbers might have shifted, due to changes before or after the method.
 *
 * @author sj
 */
public class MethodCompareWrapper {

	private final ParsedMethod method;

	public MethodCompareWrapper(ParsedMethod method) {
		this.method = method;
	}

	public ParsedMethod getParsedMethod() {
		return this.method;
	}

	public static Set<ParsedMethod> unwrap(Set<MethodCompareWrapper> methodWrappers) {
		return methodWrappers.stream().map(MethodCompareWrapper::getParsedMethod).collect(Collectors.toSet());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof MethodCompareWrapper) {
			MethodCompareWrapper otherWrapper = (MethodCompareWrapper) obj;
			return Objects.equals(this.method.getParameterTypes(), otherWrapper.method.getParameterTypes())
					&& Objects.equals(this.method.getName(), otherWrapper.method.getName())
					&& Objects.equals(this.method.getRelevantCode(), otherWrapper.method.getRelevantCode())
					&& Objects.equals(this.method.getScope(), otherWrapper.method.getScope())
					&& Objects.equals(this.method.getTopLevelTypeFqn(), otherWrapper.method.getTopLevelTypeFqn())
					&& Objects.equals(this.method.getMethodType(), otherWrapper.method.getMethodType());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.method.getParameterTypes(), this.method.getName(), this.method.getRelevantCode(),
				this.method.getScope(), this.method.getTopLevelTypeFqn(), this.method.getMethodType());
	}

	@Override
	public String toString() {
		return this.method.toString();
	}
}
