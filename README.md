# test-gap-analysis

Implementation of a basic [test gap analysis](https://www.cqse.eu/publications/2016-did-we-test-the-right-thing-experiences-with-test-gap-analysis-in-practice.pdf) algorithm.
JaCoCo coverage reports are used to check if changed or added methods (of a Git repository) are covered by automated tests.
Covered in this scope means that any code branch of the methods is executed.
The reported methods are giving some pointers where completely untested code is and test cases should be added.

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
The code of the inner methods shouldn't be considered as code of the outer methods.
Therefore the code of the inner methods is masked.
The same masking applies to comments.
Comment changes shouldn't be treated as as code change as well.

For example the method

```java
public String doIt(String arg1, final boolean isDebugMode) {
    // trim the string or just be happy
    return Optional.ofNullable(arg1).map(a -> /* make it short */ a.trim()).orElse(":-)");
}
```

contains a lambda method and comments.
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

The coverage information is resolved on a per top level type base.

In a first preparation step special cases like (static) initializers are handled.
They need special treatment because all static initializers are concatenated to a single special method by the Java compiler.
Instance initializers even do not appear in the byte code as a special method.
Instead they are injected at the beginning of each constructor.

In the main step the first code line of the parsed methods is used to find the corresponding JaCoCo method coverage.

Lambda methods need a special treatment because there could be multiple of them in a single line.
This is solved by sorting the lambdas from left to right.
For the parsed code the code column is used, whereas for the JaCoCo coverage the synthetic name generated by the Java compiler is used for ordering (e.g. first `lambda$test$1` and next `lambda$test$2`).

Also constructors are matched via their arguments if there is a initializer present or an initialized member variable.
Reason is again that the initializers and member variable initializations are injected into the constructors and the line of all constructors is the code line of the first initializer or member variable initialization.

### Report generation

A report named `test-gap-report.json` is placed in the `target` folder of the Maven project.
The report contains the information printed by the plugin in a machine readable format.

This is an example of an shortened JSON report:

```javascript
{
  "workDir": "test-gap-analysis\test-gap-analysis",
  "oldCommitHash": "c87e3c027be7781d39b06b8a95b3cbc926eb03df",
  "compareWithWorkingCopyChanges": true,
  "jaCoCoReportFiles": [
    "target/site/jacoco/jacoco.xml"
  ],
  "jaCoCoCoverageCount": 233,
  "newOrChangedFiles": [
    {
      "repositoryPath": "test-gap-analysis/src/main/java/com/scheible/testgapanalysis/jacoco/JaCoCoHelper.java",
      "skipped": false,
      "state": "CHANGED"
    },
    ...
  ],
  "consideredNewOrChangedFilesCount": 12,
  "coveredMethodsCount": 37,
  "uncoveredMethodsCount": 4,
  "unresolvableMethodsCount": 5,
  "coveredMethods": [
    {
      "topLevelTypeFqn": "com.scheible.testgapanalysis.jacoco.JaCoCoHelper",
      "description": " lambda method",
      "sourceLine": 124,
      "sourceColumn": 10,
      "coveredMethodName": "lambda$getIsNotChildOfSubDirsPredicate$3",
      "coveredMethodLine": 124
    },
	...
  ],
  "uncoveredMethods": [
    {
      "topLevelTypeFqn": "com.scheible.testgapanalysis.parser.ParsedMethod",
      "description": "#equals(...)",
      "sourceLine": 133,
      "sourceColumn": 2,
      "coveredMethodName": "equals",
      "coveredMethodLine": 136
    },
    ...
  ],
  "unresolvableMethods": [
    {
      "topLevelTypeFqn": "com.scheible.testgapanalysis.TestGapAnalysis",
      "description": " constructor with 2 arguments",
      "sourceLine": 36,
      "sourceColumn": 3
    },
    ...
  ]
}
```

## Sample output of the plugin

Sample output is from a development version of the plugin.
At the bottom all changed or added methods (which are not a getter or setter) that are not covered are listed.

```
Performing test gap analysis in 'test-gap-analysis\test-gap-analysis'.
Found coverage info about 233 methods in [target/site/jacoco/jacoco.xml].
Comparing the working copy changes with the repository head (c87e3c0).
Found 20 new or changed Java files (12 non-test Java files are considered):
 - [skipped, changed] test-gap-analysis/nbactions.xml
 - [changed] test-gap-analysis/src/main/java/com/scheible/testgapanalysis/DebugCoverageResolution.java
 - [new] test-gap-analysis/src/main/java/com/scheible/testgapanalysis/DebugCoverageResolutionReport.java
 - [changed] test-gap-analysis/src/main/java/com/scheible/testgapanalysis/TestGapAnalysis.java
   ...
Method blacklist (excluded from coverage check): all getter and setter
Covered methods:
 - com.scheible.testgapanalysis.DebugCoverageResolution.parseMethods(...) at 50:2 resolved to 'parseMethods' with line 51
 - com.scheible.testgapanalysis.DebugCoverageResolution.run(...) at 38:2 resolved to 'run' with line 40
 - com.scheible.testgapanalysis.DebugCoverageResolutionReport constructor with 5 arguments at 22:2 resolved to '<init>' with line 24
 - com.scheible.testgapanalysis.TestGapAnalysis lambda method at 100:75 resolved to 'lambda$performTestGapAnalysis$4' with line 100
   ...
Uncovered methods:
 - com.scheible.testgapanalysis.TestGapReport#toString(...) at 42:3 resolved to 'toString' with line 44
 - com.scheible.testgapanalysis.TestGapReport#toString(...) at 99:3 resolved to 'toString' with line 101
 - com.scheible.testgapanalysis.parser.ParsedMethod#equals(...) at 133:2 resolved to 'equals' with line 136
 - com.scheible.testgapanalysis.parser.ParsedMethod#toString(...) at 157:2 resolved to 'toString' with line 159
Unresolvable methods (no coverage information available):
   ...
```

## Source code style

1. avoid `static` methods for easier testing
    1. exception: (package) private helper methods in classes that are static to enforce functional purity
    1. exception: completely stateless `*Utils` classes with static methods only
        1. utility classes must be `abstract` and have a private default constructor
    1. exception: real constants with names in upper case delimited by underscores
1. `logger` has to be `protected final` but not `static`: `protected final Logger logger = LoggerFactory.getLogger(getClass());` (see https://www.slf4j.org/faq.html#declared_static)
1. restrict file, method and lambda lengths to reasonable values
1. code dependency
    1. no code cycles on package level
    1. no dependencies between a package and any of its (sub-)sub-packages (only the other way around)
1. restrict maximal number of parameters to a reasonable value
    1. in case of model class constructors with too many parameters use a http://rdafbn.blogspot.com/2012/07/step-builder-pattern_28.html
        1. extra abstract class with `<ModelClassName>Builder` name and private default constructor
        1. static `builder()` method in model class as only way to instantiate the class
        1. (package private) constructor in model class with `BuilderImpl` as only parameter
        1. inner step interfaces in `<ModelClassName>Builder` with `Step` suffix
        1. inner static class `BuilderImpl` implementing all the steps
1. usage of immutable data structures only by making all members `final`
    1. for members of type collection defensive copies and `Collections.unmodifiableXzy(...)` in constructor
        1. the elements in the collections must also be immutable
    1. local variables and parameters must not use `final` because it adds too much noise (see https://github.com/spring-io/spring-javaformat#final)
        1. only ever mutate parameters in `private` methods (for example recursive methods), for all other methods return proper result types
1. usage of `Optional<?>` (was actually designed for method return types only, but is the only JDK built-in way to indicate a nullable value (all `@Nullable` annotations are from third-party libraries))
    1. never pass or return `null`, use `Optional<?>` instead (this eliminates the need for `null` checks everywhere)
    1. but prefer method overloading or usage of a builder over `Optional<?>` method parameters
    1. even use `Optional<?>` for class fields (avoids unnecessary `Optional.ofNullable(...)` calls in getter of immutable objects)
1. Java source file organization (derived from https://github.com/spring-projects/spring-framework/wiki/Code-Style#java-source-file-organization)
    1. `enum` types if simple ones (contains only constants)
    1. `static` fields and initializers
    1. normal fields
    1. constructors (ordering of parameters should be consistent with the order of the corresponding fields)
    1. `static` factory methods
    1. all other methods
        1. overridden methods have to be grouped together
    1. getters and setters (ordering of fields and getters/setters should be consistent with order of the corresponding fields)
    1. `equals(...)`, `hashCode()` and `toString()`
    1. inner types (`static`/inner classes, `interface` and non-simple `enum` types)
        1. don't overuse inner types, as soon as the type is also useful in some other context it should be a top-level type
1. don't use checked (and unchecked) exceptions for program flow, always prefer returning proper result types (exceptions are reserved for real, unexpected errors)
    1. wrapping an `IOException` with `UncheckedIOException` (or more general any exception in a `IllegalStateException`) is okay but takes away the possibility from the caller to react to errors (for example by skipping a single file that cause an `IOException` while reading it)
1. no static imports in production code (only allowed in tests)
1. no wildcard imports in general
1. `equals(...)`, `hashCode()` and `toString()` must be implemented for all model/domain classes but not service or utility classes
    1. `toString()` should only be used for debugging purposes (printed in logs (`DEBUG` level) or in IDE) and never use to extract the state of an object
    1. `toString()` should contain all interesting information (too long information can be shorten or summarized)
    1. on high-level `equals(...)` must first test for identity (`==`) and then for type compatibility with `instanceof`
    1. `equals(...)` must compare different type of fields differently:
        1. `float` and `double` with `Float.compare(...)` resp. `Double.compare(...)`
        1. all other primitive types and enums with `==`
        1. object references with `Objects.equals(...)`
    1. `hashCode()` must use `Objects.hash(...)`
1. always use `this.` for class fields but never for instance methods (same approach as https://github.com/spring-projects/spring-framework/wiki/Code-Style#field-and-method-references)
1. `static` fields and methods of own class must never be prefixed with the own class name
