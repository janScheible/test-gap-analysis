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
public class MethodCompareWrapper implements Comparable<MethodCompareWrapper> {

	private final ParsedMethod method;

	public MethodCompareWrapper(final ParsedMethod method) {
		this.method = method;
	}

	public ParsedMethod getParsedMethod() {
		return method;
	}

	public static Set<ParsedMethod> unwrap(final Set<MethodCompareWrapper> methodWrappers) {
		return methodWrappers.stream().map(MethodCompareWrapper::getParsedMethod).collect(Collectors.toSet());
	}

	@Override
	public int compareTo(final MethodCompareWrapper other) {
		if (method.getTopLevelTypeFqn().equals(other.method.getTopLevelTypeFqn())) {
			return method.getName().compareTo(other.method.getName());
		} else {
			return method.getTopLevelTypeFqn().compareTo(other.method.getTopLevelTypeFqn());
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof MethodCompareWrapper) {
			final MethodCompareWrapper otherWrapper = (MethodCompareWrapper) obj;
			return Objects.equals(method.getArgumentTypes(), otherWrapper.method.getArgumentTypes())
					&& Objects.equals(method.getName(), otherWrapper.method.getName())
					&& Objects.equals(method.getRelevantCode(), otherWrapper.method.getRelevantCode())
					&& Objects.equals(method.getScope(), otherWrapper.method.getScope())
					&& Objects.equals(method.getTopLevelTypeFqn(), otherWrapper.method.getTopLevelTypeFqn())
					&& Objects.equals(method.getMethodType(), otherWrapper.method.getMethodType());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(method.getArgumentTypes(), method.getName(), method.getRelevantCode(), method.getScope(),
				method.getTopLevelTypeFqn(), method.getMethodType());
	}

	@Override
	public String toString() {
		return method.toString();
	}
}
