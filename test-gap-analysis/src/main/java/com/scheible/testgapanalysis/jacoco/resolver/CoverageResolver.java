package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.common.JavaMethodUtil.normalizeMethodArguments;
import static com.scheible.testgapanalysis.common.JavaMethodUtil.parseDescriptorArguments;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class CoverageResolver {

	private final Map<TopLevelType, Set<ParsedMethod>> allMethods;
	private final Map<TopLevelType, Set<MethodWithCoverageInfo>> coverageReport;

	CoverageResolver(final Map<TopLevelType, Set<ParsedMethod>> allMethods,
			final Map<TopLevelType, Set<MethodWithCoverageInfo>> coverageReport) {
		this.allMethods = allMethods;
		this.coverageReport = coverageReport;
	}

	/**
	 * A coverage resolver is created with all parsed methods of a set of Java files and a fitting coverage
	 * report.
	 */
	public static CoverageResolver with(final Set<ParsedMethod> allMethods,
			final Set<MethodWithCoverageInfo> coverage) {
		return new CoverageResolver(groupMethodsByType(allMethods), groupCoverageByType(coverage));
	}

	/**
	 * Resolves the coverage info for the given methods (could be all methods passed at initialization time or
	 * only some of them). The complete set of methods passed at initialization time is used to tackle special
	 * cases like initializers.
	 */
	public CoverageResult resolve(final Set<ParsedMethod> methods) {
		final CoverageResult result = new CoverageResult();

		for (final Entry<TopLevelType, Set<ParsedMethod>> typeMethodsEntry : groupMethodsByType(methods).entrySet()) {
			result.add(resolveType(typeMethodsEntry.getKey(), typeMethodsEntry.getValue()));
		}

		return result;
	}

	CoverageResult resolveType(final TopLevelType type, final Set<ParsedMethod> methods) {
		final CoverageResult result = new CoverageResult();

		final Set<MethodWithCoverageInfo> coverage = coverageReport.getOrDefault(type, Collections.emptySet());

		// handle initializer
		handleInitializer(type, methods);

		// line matching
		final Map<Integer, Set<ParsedMethod>> lineMethodMapping = toLineMapping(methods,
				ParsedMethod::getFirstCodeLine);
		final Map<Integer, Set<MethodWithCoverageInfo>> lineCoverageMapping = toLineMapping(coverage,
				MethodWithCoverageInfo::getLine);

		for (final Entry<Integer, Set<ParsedMethod>> lineMethodEntry : lineMethodMapping.entrySet()) {
			result.add(resolveLine(lineMethodEntry.getValue(), lineCoverageMapping.get(lineMethodEntry.getKey())));
		}

		return result;
	}

	/**
	 * Initializers don't have dedicated coverage info but are part of the constructor coverage (the Java compiler
	 * injects all initializers into the constructors). That means as soon as the coverage of a initalizer should
	 * be resolved all constructors of the class have to be considered instead.
	 */
	private void handleInitializer(final TopLevelType type, final Set<ParsedMethod> methods) {
		final Set<ParsedMethod> initializerMethods = methods.stream().filter(ParsedMethod::isInitializer)
				.collect(Collectors.toSet());
		if (!initializerMethods.isEmpty()) {
			methods.removeAll(initializerMethods);

			allMethods.get(type).stream().filter(ParsedMethod::isConstructor).forEach(m -> methods.add(m));
		}
	}

	CoverageResult resolveLine(final Set<ParsedMethod> methods, final Set<MethodWithCoverageInfo> coverage) {
		final Map<ParsedMethod, MethodWithCoverageInfo> resolved = new HashMap<>();
		final Set<ParsedMethod> unresolved = new HashSet<>();

		if (coverage != null) {
			final Set<ParsedMethod> methodsWithoutLambda = methods.stream().filter(m -> !m.isLambdaMethod())
					.collect(Collectors.toSet());
			final Set<MethodWithCoverageInfo> coverageWithoutLambda = coverage.stream()
					.filter(mwci -> !mwci.isLambdaMethod()).collect(Collectors.toSet());

			if (methodsWithoutLambda.size() == 1 && coverageWithoutLambda.size() == 1) {
				resolved.put(methodsWithoutLambda.iterator().next(), coverageWithoutLambda.iterator().next());
			} else if (!methodsWithoutLambda.isEmpty()) {
				unresolved.addAll(methodsWithoutLambda);
			}

			final List<ParsedMethod> lambdaMethods = methods.stream().filter(ParsedMethod::isLambdaMethod)
					.sorted((a, b) -> Integer.compare(a.getCodeColumn(), b.getCodeColumn()))
					.collect(Collectors.toList());
			final List<MethodWithCoverageInfo> lambdaCoverage = coverage.stream()
					.filter(MethodWithCoverageInfo::isLambdaMethod)
					.sorted((a, b) -> a.getLambdaIndex().get().compareTo(b.getLambdaIndex().get()))
					.collect(Collectors.toList());

			if (lambdaMethods.size() == lambdaCoverage.size()) {
				for (int i = 0; i < lambdaMethods.size(); i++) {
					resolved.put(lambdaMethods.get(i), lambdaCoverage.get(i));
				}
			} else {
				unresolved.addAll(lambdaMethods);
			}
		} else {
			// resolve constructors that lost there line number in case of initializers present
			for (final ParsedMethod constructor : methods.stream().filter(ParsedMethod::isConstructor)
					.collect(Collectors.toSet())) {
				resolveConstructor(constructor).ifPresent(constructorCoverage -> {
					methods.remove(constructor);
					resolved.put(constructor, constructorCoverage);
				});
			}

			if (!methods.isEmpty()) {
				unresolved.addAll(methods);
			}
		}

		return new CoverageResult(resolved, unresolved);
	}

	/**
	 * Normally constructors are resolved via line (of first code) as well. But in case of one or more initializer
	 * blocks the line number of the constructors in the JaCoCo report is not correct anymore. It is always the
	 * first line of code of the static initalizer instead of the real constructor position. Fallback to argument
	 * matching in such a case.
	 */
	private Optional<MethodWithCoverageInfo> resolveConstructor(final ParsedMethod constructor) {
		final Set<MethodWithCoverageInfo> coveredConstructors = coverageReport
				.getOrDefault(TopLevelType.of(constructor), Collections.emptySet()).stream()
				.filter(MethodWithCoverageInfo::isConstructor)
				.filter(mwci -> mwci.getEnclosingSimpleName().equals(constructor.getEnclosingSimpleName()))
				.filter(mwci -> normalizeMethodArguments(constructor.getArgumentTypes().get())
						.equals(normalizeMethodArguments(parseDescriptorArguments(mwci.getDescription()))))
				.collect(Collectors.toSet());

		if (coveredConstructors.size() == 1) {
			return Optional.of(coveredConstructors.iterator().next());
		} else {
			return Optional.empty();
		}
	}

	private static Map<TopLevelType, Set<ParsedMethod>> groupMethodsByType(final Set<ParsedMethod> methods) {
		return methods.stream().collect(
				Collectors.groupingBy(TopLevelType::of, Collectors.mapping(Function.identity(), Collectors.toSet())));
	}

	private static Map<TopLevelType, Set<MethodWithCoverageInfo>> groupCoverageByType(
			final Set<MethodWithCoverageInfo> methods) {
		return methods.stream().collect(
				Collectors.groupingBy(TopLevelType::of, Collectors.mapping(Function.identity(), Collectors.toSet())));
	}

	private static <T> Map<Integer, Set<T>> toLineMapping(final Set<T> items,
			final Function<T, Integer> lineExtractor) {
		return items.stream().collect(
				Collectors.groupingBy(lineExtractor, Collectors.mapping(Function.identity(), Collectors.toSet())));
	}
}
