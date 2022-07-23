package com.scheible.testgapanalysis.jacoco;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import com.scheible.testgapanalysis.common.JavaMethodUtils;

/**
 *
 * @author sj
 */
public class MethodWithCoverageInfo {

	private final String className;
	private final String simpleClassName;
	private final String enclosingSimpleName;
	private final String name;
	private final String description;
	private final int line;
	private final int coveredInstructionCount;

	public MethodWithCoverageInfo(final String className, final String name, final String description, final int line,
			final int coveredInstructionCount) {
		this.className = className;
		this.simpleClassName = JavaMethodUtils.getSimpleName(className, "/");
		this.enclosingSimpleName = JavaMethodUtils.getSimpleName(simpleClassName, "$");
		this.name = name;
		this.description = description;
		this.line = line;
		this.coveredInstructionCount = coveredInstructionCount;
	}

	public static MethodWithCoverageInfo merge(final Collection<MethodWithCoverageInfo> methods) {
		checkEmptyMergeCollection(methods);

		String className = null;
		String name = null;
		String description = null;
		Integer line = null;

		int coveredInstructionCount = 0;

		for (final MethodWithCoverageInfo method : methods) {
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

		return new MethodWithCoverageInfo(className, name, description, line, coveredInstructionCount);
	}

	private static void checkEmptyMergeCollection(final Collection<MethodWithCoverageInfo> methods) {
		if (methods.isEmpty()) {
			throw new IllegalArgumentException("Can't merge a empty set of methods with coverage info!");
		}
	}

	public boolean isLambdaMethod() {
		return name.startsWith("lambda$");
	}

	public Optional<Integer> getLambdaIndex() {
		if (isLambdaMethod()) {
			return Optional.of(Integer.valueOf(name.substring(name.lastIndexOf('$') + 1)));
		} else {
			return Optional.empty();
		}
	}

	public boolean isStaticInitializer() {
		return "<clinit>".equals(name);
	}

	public boolean isConstructor() {
		return "<init>".equals(name);
	}

	public boolean isNonLambdaMethod() {
		return !isLambdaMethod() && !isStaticInitializer() && !isConstructor();
	}

	public String getClassName() {
		return className;
	}

	public String getSimpleClassName() {
		return simpleClassName;
	}

	public String getEnclosingSimpleName() {
		return enclosingSimpleName;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getLine() {
		return line;
	}

	public int getCoveredInstructionCount() {
		return coveredInstructionCount;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof MethodWithCoverageInfo) {
			final MethodWithCoverageInfo other = (MethodWithCoverageInfo) obj;
			return Objects.equals(className, other.className) && Objects.equals(simpleClassName, other.simpleClassName)
					&& Objects.equals(enclosingSimpleName, other.enclosingSimpleName)
					&& Objects.equals(name, other.name) && Objects.equals(description, other.description)
					&& Objects.equals(line, other.line)
					&& Objects.equals(coveredInstructionCount, other.coveredInstructionCount);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(className, simpleClassName, enclosingSimpleName, name, description, line,
				coveredInstructionCount);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[className='" + className + "', simpleClassName='" + simpleClassName
				+ "', enclosingSimpleName='" + enclosingSimpleName + "', name='" + name + "', line=" + line
				+ ", description='" + description + "', coveredInstructionCount=" + coveredInstructionCount + "]";
	}
}
