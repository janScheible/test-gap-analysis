# test-gap-analysis

Implementation of a basic [test gap analysis](https://www.cqse.eu/publications/2016-did-we-test-the-right-thing-experiences-with-test-gap-analysis-in-practice.pdf) algorithm.
JaCoCo coverage reports are used to check if changed or added methods (of a Git repository) are covered by automated tests.
Covered in this scope means that any code branch of the methods is executed.
The reported methods are giving some pointers where completely untested code is and test cases should to be added.

## Prerequisites for usage

1. recent JDK 8 (also tested with Java 11)
1. recent Maven (tested with 3.6.1)
1. `mvn clean install` of [Pocketsaw 1.3.2](https://github.com/janScheible/pocketsaw/tree/1.3.2)
1. **[optionally]** [maven-skip-execution-profile-extension](https://github.com/janScheible/spring-boot-netbeans-getting-started/tree/master/skip-execution-profile/maven-skip-execution-profile-extension) with a [.mvn directory like this](https://github.com/janScheible/spring-boot-netbeans-getting-started/tree/master/spring-boot-netbeans-single-module/.mvn) in the root folder for a smooth Netbeans experience
1. a project with a Git repository and a local working copy
1. JaCoCo with `xml` reports for test coverage

## Use cases

The plugin can be used in two ways that are described in the next sections.

### Local development iterations

The code changes in a local Git working copy are inspected and not covered methods are reported.
This is useful for local development iterations before pushing the changes to a remote repository and starting a CI build.

```DOS .bat
mvn com.scheible.testgapanalysis:test-gap-analysis-maven-plugin:1.2.0:clean-jacoco-reports^
 jacoco:prepare-agent^
 surefire:test^
 jacoco:report^
 com.scheible.testgapanalysis:test-gap-analysis-maven-plugin:1.2.0:perform
```

By also adding `jacoco:prepare-agent-integration`, `failsafe:integration-test` and `jacoco:report-integration` integration tests are also considered (method coverages of all JaCoCo reports are merged).

### CI build

The code changes done between a reference commit and the head commit of the repository are inspected and not covered methods are reported.
This mode can for example be used to record the test gap between the current release and the previous one in a CI build.
The reference commit hash can be specified either via `<configuration>`-element `<referenceCommitHash>` in the Maven POM or as parameter `-Dtest-gap-analysis.reference-commit-hash`.

## How it works

### Identification of the change set

In the first step the changed and new Java files are identified.
In case of changed files the contents of the previous revision and the current one are available.

### Parsing of the Java files

All files of the change set are then parsed for methods in the second step.
Methods are (static) methods, (static) initializers, constructors and lambda methods.
Methods can be nested.
The code of the inner methods shouldn't be considered as code of the outer messages.
Therefore the code of the inner methods is masked.
The same masking applies to comments.
Comment changes shouldn't be treated as as code change as well.

For example the method

```java
public String doIt(String arg1, final boolean isDebugMode) {
	// trim the string or jsut be happy
	return Optional.ofNullable(arg1).map(a -> /* make it short */ a.trim()).orElse(":-)");
}
```

contains a lambda method.
Changes in the lambda method shouldn't be treated as changes in the `doIt(...)` method but as changes of the lambda method itself.

After masking the code of the method looks like this:

```
public String doIt(String arg1, final boolean isDebugMode) {
	###################################
	return Optional.ofNullable(arg1).map(#################################).orElse(":-)");
}
```

Also the lambda method has all outher code and comments masked:

```
####################################### a -> ################### a.trim() ################
```

The hash characters are only used for visualization purposes in debug mode.
In the normal mode collapsed whitespaces are used instead.
Collapsed means that more than one whitespace is collapsed to a single on.

### Identification of changed/new methods

Only parsed methods of the current Java files that are either changed or new are of interest in this step.
All unchanged methods are filtered out.

### Correlation with the JaCoCo coverage report

tdb

### Report generation

tbd

## Sample output

Sample output is from the upcomming pocketsaw2.
At the bottom all changed or added methods (which are not a getter or setter) that are not covered are listed.

```
Comparing the working copy changes with the repository head.
Found coverage info about 297 methods.
Found 8 new or changed Java files.
New or changed methods (excluding setter and getter):
 - com.scheible.pocketsaw2.api.unittest.Pocketsaw#createAnalysis@aa6ed6814a638d238063be02c723d96f4e2103884afd5e4cd3bf30ca7358334f
 - com.scheible.pocketsaw2.engine.CycleDetector#analyzeCode@0c5b08fc0dea8d3794188cbbee5525430d2dc6486310bcbff04b8e28d77e600c
 - com.scheible.pocketsaw2.api.unittest.Pocketsaw#analize@76e731a0f9eb8ae75db6ab3e3b8b94ecfafe194142721d4d48dc8e0b20ed9aec
 - com.scheible.pocketsaw2.engine.CycleDetector#analyzeDescriptors@0f42df1a5a8b9a441fbe3a31595270eddb5154a95bd073d63a7e1c5e77004c57
Covered methods:
 - com.scheible.pocketsaw2.api.dependency.CodeUnit#getNativeCodeType@0
 - com.scheible.pocketsaw2.common.Arrays2#<init>@0
 - com.scheible.pocketsaw2.common.graph.algorithm.Dijkstra#findAllShortestPaths@116
 - com.scheible.pocketsaw2.api.unittest.Pocketsaw#initialize@0
 - com.scheible.pocketsaw2.api.visualization.CodeCycle#getShortestCycle@0
 - com.scheible.pocketsaw2.addon.descriptor.builder.PrefixMatcherBuilder#getExcludes@0
 - com.scheible.pocketsaw2.common.graph.algorithm.Dijkstra$QueueNode#equals@0
 - com.scheible.pocketsaw2.engine.DependencyGraphFactory#collectNeighbors@0
 - com.scheible.pocketsaw2.api.dependency.CodeGroup#getNativeCodeGroup@0
 - com.scheible.pocketsaw2.addon.code.dependencycruiser.DependencyCruiserSource#<clinit>@7
 - ...
Uncovered new or changed methods (excluding setter and getter):
 - com.scheible.pocketsaw2.api.unittest.Pocketsaw#createAnalysis
 - com.scheible.pocketsaw2.api.unittest.Pocketsaw#analize
```
