package com.scheible.testgapanalysis.jacoco;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import com.scheible.testgapanalysis.common.JavaMethodUtils;
import com.scheible.testgapanalysis.common.ToStringBuilder;

/**
 *
 * @author sj
 */
public class InstrumentedMethod {

	private final String className;
	private final String simpleClassName;
	private final String enclosingSimpleName;
	private final String name;
	private final String description;
	private final int line;
	private final int coveredInstructionCount;

	public InstrumentedMethod(String className, String name, String description, int line,
			int coveredInstructionCount) {
		this.className = className;
		this.simpleClassName = JavaMethodUtils.getSimpleName(className, "/");
		this.enclosingSimpleName = JavaMethodUtils.getSimpleName(this.simpleClassName, "$");
		this.name = name;
		this.description = description;
		this.line = line;
		this.coveredInstructionCount = coveredInstructionCount;
	}

	public static InstrumentedMethod merge(Collection<InstrumentedMethod> methods) {
		checkEmptyMergeCollection(methods);

		String className = null;
		String name = null;
		String description = null;
		Integer line = null;

		int coveredInstructionCount = 0;

		for (InstrumentedMethod method : methods) {
			if (className == null && name == null && description == null && line == null) {
				className = method.getClassName();
				name = method.getName();
				description = method.getDescription();
				line = method.getLine();
				coveredInstructionCount = method.getCoveredInstructionCount();
			} else {
				if (!Objects.equals(className, method.getClassName()) || !Objects.equals(name, method.getName())
						|| !Objects.equals(description, method.getDescription())
						|| !Objects.equals(line, method.getLine())) {
					throw new IllegalArgumentException(
							methods + " can't be merged because they refer to different methods!");
				}

				coveredInstructionCount += method.getCoveredInstructionCount();
			}

		}

		return new InstrumentedMethod(className, name, description, line, coveredInstructionCount);
	}

	private static void checkEmptyMergeCollection(Collection<InstrumentedMethod> methods) {
		if (methods.isEmpty()) {
			throw new IllegalArgumentException("Can't merge a empty set of methods with coverage info!");
		}
	}

	public boolean isLambdaMethod() {
		return this.name.startsWith("lambda$");
	}

	public Optional<Integer> getLambdaIndex() {
		if (isLambdaMethod()) {
			return Optional.of(Integer.valueOf(this.name.substring(this.name.lastIndexOf('$') + 1)));
		} else {
			return Optional.empty();
		}
	}

	public boolean isStaticInitializer() {
		return "<clinit>".equals(this.name);
	}

	public boolean isConstructor() {
		return "<init>".equals(this.name);
	}

	public boolean isNonLambdaMethod() {
		return !isLambdaMethod() && !isStaticInitializer() && !isConstructor();
	}

	public String getClassName() {
		return this.className;
	}

	public String getSimpleClassName() {
		return this.simpleClassName;
	}

	public String getEnclosingSimpleName() {
		return this.enclosingSimpleName;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public int getLine() {
		return this.line;
	}

	public int getCoveredInstructionCount() {
		return this.coveredInstructionCount;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof InstrumentedMethod) {
			InstrumentedMethod other = (InstrumentedMethod) obj;
			return Objects.equals(this.className, other.className)
					&& Objects.equals(this.simpleClassName, other.simpleClassName)
					&& Objects.equals(this.enclosingSimpleName, other.enclosingSimpleName)
					&& Objects.equals(this.name, other.name) && Objects.equals(this.description, other.description)
					&& this.line == other.line && this.coveredInstructionCount == other.coveredInstructionCount;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.className, this.simpleClassName, this.enclosingSimpleName, this.name, this.description,
				this.line, this.coveredInstructionCount);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("className", this.className)
				.append("simpleClassName", this.simpleClassName).append("enclosingSimpleName", this.enclosingSimpleName)
				.append("name", this.name).append("line", this.line).append("description", this.description)
				.append("coveredInstructionCount", this.coveredInstructionCount).build();
	}
}
