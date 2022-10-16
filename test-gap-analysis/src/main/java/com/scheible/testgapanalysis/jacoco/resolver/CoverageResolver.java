package com.scheible.testgapanalysis.jacoco.resolver;

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

import com.scheible.testgapanalysis.common.JavaMethodUtils;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class CoverageResolver {

	private final Map<TopLevelType, Set<MethodWithCoverageInfo>> coverageReport;

	CoverageResolver(Map<TopLevelType, Set<MethodWithCoverageInfo>> coverageReport) {
		this.coverageReport = coverageReport;
	}

	/**
	 * A coverage resolver is created with fitting coverage report.
	 */
	public static CoverageResolver with(Set<MethodWithCoverageInfo> coverage) {
		return new CoverageResolver(groupCoverageByType(coverage));
	}

	/**
	 * Resolves the coverage info for the given methods (could be all methods passed at initialization time or
	 * only some of them). The complete set of methods passed at initialization time is used to tackle special
	 * cases like initializers.
	 */
	public CoverageResult resolve(Set<ParsedMethod> methods) {
		CoverageResult result = CoverageResult.ofEmptyMethods(methods);

		for (Entry<TopLevelType, Set<ParsedMethod>> typeMethodsEntry : groupMethodsByType(
				methods.stream().filter(m -> !m.isEmpty()).collect(Collectors.toSet())).entrySet()) {
			result.add(resolveType(typeMethodsEntry.getKey(), typeMethodsEntry.getValue()));
		}

		return result;
	}

	CoverageResult resolveType(TopLevelType type, Set<ParsedMethod> methods) {
		CoverageResult result = new CoverageResult();

		Set<MethodWithCoverageInfo> coverage = this.coverageReport.getOrDefault(type, Collections.emptySet());

		result.add(resolveInitializers(filter(methods, ParsedMethod::isInitializer), coverage));
		result.add(resolveStaticInitializers(filter(methods, ParsedMethod::isStaticInitializer), coverage));
		result.add(resolveConstructor(filter(methods, ParsedMethod::isAnyConstructor), coverage));
		result.add(resolveNonLambdaMethods(filter(methods, ParsedMethod::isAnyNonLambdaMethod), coverage));
		result.add(resolveLambdaMethods(filter(methods, ParsedMethod::isLambdaMethod), coverage));

		return result;
	}

	/**
	 * Initializers don't have dedicated coverage equivalent but are part of the constructor coverage (the Java
	 * compiler injects all initializers into the beginning of each of the constructors). That means initializers
	 * have to be resolved to a constructor (to a code covered one in best case).
	 */
	private CoverageResult resolveInitializers(Set<ParsedMethod> initializers, Set<MethodWithCoverageInfo> coverage) {
		List<MethodWithCoverageInfo> coverageConstructors = coverage.stream()
				.filter(MethodWithCoverageInfo::isConstructor).collect(Collectors.toList());

		if (initializers.size() == 1 && coverageConstructors.size() == 1) {
			Map<ParsedMethod, MethodWithCoverageInfo> resolvedInitializers = new HashMap<>();
			resolvedInitializers.put(initializers.iterator().next(), coverageConstructors.get(0));
			return new CoverageResult(resolvedInitializers, Collections.emptySet());
		} else {
			// try to find a constructor with covered code, if there is none use any non-covered constructor
			Optional<MethodWithCoverageInfo> firstConstructorCoverage = coverageConstructors.stream()
					.sorted(Comparator.comparing(MethodWithCoverageInfo::getCoveredInstructionCount).reversed())
					.findFirst();

			if (firstConstructorCoverage.isPresent()) {
				Map<ParsedMethod, MethodWithCoverageInfo> resolvedInitializers = initializers.stream()
						.collect(Collectors.toMap(Function.identity(), initilaizer -> firstConstructorCoverage.get()));
				return new CoverageResult(resolvedInitializers, Collections.emptySet());
			}
		}

		return new CoverageResult();
	}

	/**
	 * All static initializers are concatenated by the Java compiler to a single special static method. Therefore
	 * all static initializers are resolved to that single static initializer special method.
	 */
	private CoverageResult resolveStaticInitializers(Set<ParsedMethod> staticInitializers,
			Set<MethodWithCoverageInfo> coverage) {
		List<MethodWithCoverageInfo> coverageStaticInitializers = coverage.stream()
				.filter(MethodWithCoverageInfo::isStaticInitializer).collect(Collectors.toList());

		if (!coverageStaticInitializers.isEmpty()) { // make sure that a coverage report is there
			Map<ParsedMethod, MethodWithCoverageInfo> resolvedStaticInitializers = staticInitializers.stream().collect(
					Collectors.toMap(Function.identity(), staticInitilaizer -> coverageStaticInitializers.get(0)));
			return new CoverageResult(resolvedStaticInitializers, Collections.emptySet());
		}

		return new CoverageResult();
	}

	/**
	 * Constructors might loose there line number in the JaCoCo report. Could be caused by a initializer or a
	 * member variable that is initialized. In JaCoCo line of the constructor is always the first line of code of
	 * the initalizer or member variable that is initialized instead of the real constructor code line. Therefore
	 * parameter matching is performed instead. Enum constructors and inner class constructors are also special
	 * cases.
	 */
	private CoverageResult resolveConstructor(Set<ParsedMethod> constructors, Set<MethodWithCoverageInfo> coverage) {
		Map<ParsedMethod, MethodWithCoverageInfo> resolved = new HashMap<>();
		Set<ParsedMethod> unresolved = new HashSet<>();

		for (ParsedMethod constructor : constructors) {
			List<String> normalizedConstructorParameters = new ArrayList<>(JavaMethodUtils
					.normalizeMethodParameters(constructor.getParameterTypes(), constructor.getTypeParameters()));

			// The constructors of enums have two additional parameter of type String and int. Most likely the name and
			// index of the enum const is passed via this parameter.
			if (constructor.isEnumConstructor()) {
				normalizedConstructorParameters.add(0, "int");
				normalizedConstructorParameters.add(0, "String");
			} else // Non-static nested classes (called inner classes) have and addition first constructor parameter.
			if (constructor.isInnerClassConstructor()) {
				normalizedConstructorParameters.add(0,
						constructor.getOuterDeclaringType().orElseThrow(() -> new IllegalStateException(
								"Inner class constructor " + constructor + " has no outer declaring type!")));
			}

			List<MethodWithCoverageInfo> coverageConstructors = coverage.stream()
					.filter(MethodWithCoverageInfo::isConstructor)
					.filter(mwci -> mwci.getEnclosingSimpleName().equals(constructor.getEnclosingSimpleName()))
					.filter(mwci -> normalizedConstructorParameters.equals(JavaMethodUtils.normalizeMethodParameters(
							JavaMethodUtils.convertParameterDescriptor(mwci.getDescription()), Collections.emptyMap())))
					.collect(Collectors.toList());
			if (coverageConstructors.size() == 1) {
				resolved.put(constructor, coverageConstructors.get(0));
			} else {
				unresolved.add(constructor);
			}
		}

		return new CoverageResult(resolved, unresolved);
	}

	/**
	 * For non lambda methods the JaCoCo line numbers are reliable. In addition to the line number methods names
	 * and parameter count are used to resolve.
	 */
	private CoverageResult resolveNonLambdaMethods(Set<ParsedMethod> methods, Set<MethodWithCoverageInfo> coverage) {
		Map<ParsedMethod, MethodWithCoverageInfo> resolved = new HashMap<>();
		Set<ParsedMethod> unresolved = new HashSet<>();

		List<MethodWithCoverageInfo> candidates = new ArrayList<>();
		for (ParsedMethod method : methods) {
			for (MethodWithCoverageInfo coverageMethod : coverage.stream()
					.filter(MethodWithCoverageInfo::isNonLambdaMethod).collect(Collectors.toSet())) {
				if (method.containsLine(coverageMethod.getLine()) && method.getName().equals(coverageMethod.getName())
						&& method.getParameterCount() == JavaMethodUtils
								.convertParameterDescriptor(coverageMethod.getDescription()).size()) {
					candidates.add(coverageMethod);
				}
			}

			if (candidates.size() == 1) {
				resolved.put(method, candidates.get(0));
			} else {
				unresolved.add(method);
			}

			candidates.clear();
		}

		return new CoverageResult(resolved, unresolved);
	}

	/**
	 * Lambda methods have no name and an un-relaiable number of parameters (only a smaller than sanity check can
	 * be made). But line numbers can be used and the lambda identifiers (e.g. lambda$1) allows column sorting.
	 */
	private CoverageResult resolveLambdaMethods(Set<ParsedMethod> lambdas, Set<MethodWithCoverageInfo> coverage) {
		CoverageResult result = new CoverageResult();

		// mappings based on line number overlap
		Map<ParsedMethod, Set<MethodWithCoverageInfo>> mapping = new HashMap<>();
		Map<MethodWithCoverageInfo, Set<ParsedMethod>> inverseMapping = new HashMap<>();

		for (ParsedMethod lambda : lambdas) {
			for (MethodWithCoverageInfo lambdaCoverage : coverage.stream()
					.filter(MethodWithCoverageInfo::isLambdaMethod).collect(Collectors.toSet())) {
				if (lambda.containsLine(lambdaCoverage.getLine())) {
					mapping.computeIfAbsent(lambda, key -> new HashSet<>()).add(lambdaCoverage);
					inverseMapping.computeIfAbsent(lambdaCoverage, key -> new HashSet<>()).add(lambda);
				}
			}
		}

		// resolve each cluster individually
		for (Entry<ParsedMethod, Set<MethodWithCoverageInfo>> entry : mapping.entrySet()) {
			if (!result.contains(entry.getKey())) {
				result.add(resolveLambdaMethodsCluster(entry, mapping, inverseMapping));
			}
		}

		return result;
	}

	/**
	 * Resolves a cluster of lambda methods. A cluster consists of parsed methods and methods with coverage info
	 * that overlapp in terms of code lines.
	 */
	private CoverageResult resolveLambdaMethodsCluster(Entry<ParsedMethod, Set<MethodWithCoverageInfo>> entry,
			Map<ParsedMethod, Set<MethodWithCoverageInfo>> mapping,
			Map<MethodWithCoverageInfo, Set<ParsedMethod>> inverseMapping) {
		List<ParsedMethod> lambdas = entry.getValue().stream().flatMap(mwci -> inverseMapping.get(mwci).stream())
				.sorted(Comparator.comparing(ParsedMethod::getFirstCodeLine).thenComparing(ParsedMethod::getCodeColumn))
				.distinct().collect(Collectors.toList());
		List<MethodWithCoverageInfo> lambdaCoverage = lambdas.stream().flatMap(pm -> mapping.get(pm).stream())
				.sorted(Comparator.comparing(MethodWithCoverageInfo::getLine)
						.thenComparing(Comparator.comparing(mwci -> mwci.getLambdaIndex().get())))
				.distinct().collect(Collectors.toList());

		// Unfortunately we can't compare parameter counts because the compiler adds a parameter for every
		// (effective) final variable used in the scope of the lambda. And those aren't visible in the source code...
		if (lambdaCoverage.size() == lambdas.size()) {
			Map<ParsedMethod, MethodWithCoverageInfo> currentResolved = new HashMap<>();
			boolean withoutErrors = true;

			for (int i = 0; i < lambdaCoverage.size(); i++) {
				if (lambdas.get(i).getParameterCount() <= JavaMethodUtils
						.convertParameterDescriptor(lambdaCoverage.get(i).getDescription()).size()) {
					currentResolved.put(lambdas.get(i), lambdaCoverage.get(i));
				} else {
					withoutErrors = false;
					break;
				}
			}

			if (withoutErrors) {
				return new CoverageResult(currentResolved, Collections.emptySet());
			}
		}

		return new CoverageResult(Collections.emptyMap(), new HashSet<>(lambdas));
	}

	private static Map<TopLevelType, Set<ParsedMethod>> groupMethodsByType(Set<ParsedMethod> methods) {
		return methods.stream().collect(
				Collectors.groupingBy(TopLevelType::of, Collectors.mapping(Function.identity(), Collectors.toSet())));
	}

	private static Map<TopLevelType, Set<MethodWithCoverageInfo>> groupCoverageByType(
			Set<MethodWithCoverageInfo> methods) {
		return methods.stream().collect(
				Collectors.groupingBy(TopLevelType::of, Collectors.mapping(Function.identity(), Collectors.toSet())));
	}

	private static Set<ParsedMethod> filter(Set<ParsedMethod> methods, Predicate<ParsedMethod> predicate) {
		return methods.stream().filter(predicate).collect(Collectors.toSet());
	}
}
