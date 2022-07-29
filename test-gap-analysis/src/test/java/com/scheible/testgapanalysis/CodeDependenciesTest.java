package com.scheible.testgapanalysis;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import org.junit.runner.RunWith;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.dependencies.SliceAssignment;
import com.tngtech.archunit.library.dependencies.SliceIdentifier;

/**
 *
 * @author sj
 */
@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packagesOf = CodeDependenciesTest.class, importOptions = DoNotIncludeTests.class)
public class CodeDependenciesTest {

	private static class SlicePerPackage implements SliceAssignment {

		@Override
		public SliceIdentifier getIdentifierOf(JavaClass javaClass) {
			return SliceIdentifier.of(javaClass.getPackageName());
		}

		@Override
		public String getDescription() {
			return "Every package is treated as a slice.";
		}
	}

	@ArchTest
	static final ArchRule noPackageCyclesRule = slices().assignedFrom(new SlicePerPackage()).should().beFreeOfCycles();

	private static class DependOnDescendantPackagesCondition extends ArchCondition<JavaClass> {

		DependOnDescendantPackagesCondition() {
			super("depend on descendant packages");
		}

		@Override
		public void check(JavaClass clazz, ConditionEvents events) {
			for (Dependency dependency : clazz.getDirectDependenciesFromSelf()) {
				boolean dependencyOnDescendantPackage = isDependencyOnDescendantPackage(dependency.getOriginClass(),
						dependency.getTargetClass());
				events.add(new SimpleConditionEvent(dependency, dependencyOnDescendantPackage,
						dependency.getDescription()));
			}
		}

		private boolean isDependencyOnDescendantPackage(JavaClass origin, JavaClass target) {
			String originPackageName = origin.getPackageName();
			String targetSubPackagePrefix = target.getPackageName();
			return targetSubPackagePrefix.contains(originPackageName + ".");
		}
	}

	@ArchTest
	static final ArchRule packageLayeringRule = noClasses().should(new DependOnDescendantPackagesCondition())
			.because("lower packages shouldn't build on higher packages");
}
