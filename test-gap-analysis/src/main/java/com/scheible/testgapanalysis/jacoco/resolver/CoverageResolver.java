package com.scheible.testgapanalysis.jacoco.resolver;

import static java.util.Collections.emptyList;

import static com.scheible.testgapanalysis.common.JavaMethodUtil.normalizeMethodArguments;
import static com.scheible.testgapanalysis.common.JavaMethodUtil.parseDescriptorArguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class CoverageResolver {

	private final Map<TopLevelType, Set<MethodWithCoverageInfo>> coverageReport;

	CoverageResolver(final Map<TopLevelType, Set<MethodWithCoverageInfo>> coverageReport) {
		this.coverageReport = coverageReport;
	}

	/**
	 * A coverage resolver is created with fitting coverage report.
	 */
	public static CoverageResolver with(final Set<MethodWithCoverageInfo> coverage) {
		return new CoverageResolver(groupCoverageByType(coverage));
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

		// first deal with the special cases
		result.add(resolveInitializers(methods, coverage));
		result.add(resolveStaticInitializers(methods, coverage));
		result.add(resolveConstructor(methods, coverage));

		// then resolve the rest based on line numbers
		final Map<Integer, Set<ParsedMethod>> lineMethodMapping = toLineMapping(methods, ParsedMethod::getFirstCodeLine,
				m -> !result.contains(m));
		final Map<Integer, Set<MethodWithCoverageInfo>> lineCoverageMapping = toLineMapping(coverage,
				MethodWithCoverageInfo::getLine);

		for (final Entry<Integer, Set<ParsedMethod>> lineMethodEntry : lineMethodMapping.entrySet()) {
			result.add(resolveLine(lineMethodEntry.getValue(), lineCoverageMapping.get(lineMethodEntry.getKey())));
		}

		return result;
	}

	/**
	 * Initializers don't have dedicated coverage equivalent but are part of the constructor coverage (the Java
	 * compiler injects all initializers into the beginning of each of the constructors). That means initializers
	 * have to be resolved to a constructor (to a code covered one in best case).
	 */
	private CoverageResult resolveInitializers(final Set<ParsedMethod> methods,
			final Set<MethodWithCoverageInfo> coverage) {
		final List<ParsedMethod> initializerMethods = methods.stream().filter(ParsedMethod::isInitializer)
				.collect(Collectors.toList());
		final List<MethodWithCoverageInfo> constructorCoverage = coverage.stream()
				.filter(MethodWithCoverageInfo::isConstructor).collect(Collectors.toList());

		if (initializerMethods.size() == 1 && constructorCoverage.size() == 1) {
			final Map<ParsedMethod, MethodWithCoverageInfo> resolvedInitializers = new HashMap<>();
			resolvedInitializers.put(initializerMethods.get(0), constructorCoverage.get(0));
			return new CoverageResult(resolvedInitializers, Collections.emptySet());
		} else {
			// try to find a constructor with covered code, if there is none use any non-covered constructor
			final Optional<MethodWithCoverageInfo> firstConstructorCoverage = constructorCoverage.stream()
					.sorted(Comparator.comparing(MethodWithCoverageInfo::getCoveredInstructionCount).reversed())
					.findFirst();

			if (firstConstructorCoverage.isPresent()) {
				final Map<ParsedMethod, MethodWithCoverageInfo> resolvedInitializers = new HashMap<>();
				initializerMethods.forEach(im -> resolvedInitializers.put(im, firstConstructorCoverage.get()));
				return new CoverageResult(resolvedInitializers, Collections.emptySet());

			}
		}

		return new CoverageResult();
	}

	/**
	 * All static initializers are concatenated by the Java compiler to a single special static method. Therefore
	 * all static initializers are resolved to that single static initializer special method.
	 */
	private CoverageResult resolveStaticInitializers(final Set<ParsedMethod> methods,
			final Set<MethodWithCoverageInfo> coverage) {
		final List<ParsedMethod> staticInitializerMethods = methods.stream().filter(ParsedMethod::isStaticInitializer)
				.collect(Collectors.toList());
		final List<MethodWithCoverageInfo> staticInitializerCoverage = coverage.stream()
				.filter(MethodWithCoverageInfo::isStaticInitializer).collect(Collectors.toList());

		if (!staticInitializerCoverage.isEmpty()) { // make sure that a coverage report is there
			final Map<ParsedMethod, MethodWithCoverageInfo> resolvedStaticInitializers = new HashMap<>();
			staticInitializerMethods
					.forEach(im -> resolvedStaticInitializers.put(im, staticInitializerCoverage.get(0)));
			return new CoverageResult(resolvedStaticInitializers, Collections.emptySet());
		}

		return new CoverageResult();
	}

	/**
	 * Constructors might loose there line number in the JaCoCo report. Could be caused by a initializer or a
	 * member variable that is initialized. In JaCoCo line of the constructor is always the first line of code of
	 * the initalizer or member variable that is initialized instead of the real constructor code line. Therefore
	 * argument matching is performed instead. Enum constructors and inner class constructors are also special
	 * cases.
	 */
	private CoverageResult resolveConstructor(final Set<ParsedMethod> methods,
			final Set<MethodWithCoverageInfo> coverage) {
		final Map<ParsedMethod, MethodWithCoverageInfo> resolved = new HashMap<>();
		final Set<ParsedMethod> unresolved = new HashSet<>();

		for (final ParsedMethod constructor : methods.stream()
				.filter(pm -> pm.isConstructor() || pm.isEnumConstructor() || pm.isInnerClassConstructor())
				.collect(Collectors.toList())) {
			final List<String> normalizedConstructorArguments = new ArrayList<>(
					normalizeMethodArguments(constructor.getArgumentTypes(), constructor.getParentTypeParameters()));

			// The constructors of enums have two additional parameter of type String and int. Most likely the name and
			// index of the enum const is passed via this parameter.
			if (constructor.isEnumConstructor()) {
				normalizedConstructorArguments.add(0, "int");
				normalizedConstructorArguments.add(0, "String");
			} else // Non-static nested classes (called inner classes) have and addition first constructor parameter.
			if (constructor.isInnerClassConstructor()) {
				normalizedConstructorArguments.add(0,
						constructor.getOuterDeclaringType().orElseThrow(() -> new IllegalStateException(
								"Inner class constructor " + constructor + " has no outer declaring type!")));
			}

			final List<MethodWithCoverageInfo> coveredConstructors = coverage.stream()
					.filter(MethodWithCoverageInfo::isConstructor)
					.filter(mwci -> mwci.getEnclosingSimpleName().equals(constructor.getEnclosingSimpleName()))
					.filter(mwci -> normalizedConstructorArguments.equals(
							normalizeMethodArguments(parseDescriptorArguments(mwci.getDescription()), emptyList())))
					.collect(Collectors.toList());
			if (coveredConstructors.size() == 1) {
				resolved.put(constructor, coveredConstructors.get(0));
			} else {
				unresolved.add(constructor);
			}
		}

		return new CoverageResult(resolved, unresolved);
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
			unresolved.addAll(methods);
		}

		return new CoverageResult(resolved, unresolved);
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
		return toLineMapping(items, lineExtractor, item -> true);
	}

	private static <T> Map<Integer, Set<T>> toLineMapping(final Set<T> items, final Function<T, Integer> lineExtractor,
			final Predicate<T> predicate) {
		return items.stream().filter(predicate).collect(
				Collectors.groupingBy(lineExtractor, Collectors.mapping(Function.identity(), Collectors.toSet())));
	}
}
