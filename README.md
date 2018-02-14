# COMFORT Framwork
[![Build Status](https://travis-ci.org/comfort-framework/comfort.svg?branch=master)](https://travis-ci.org/comfort-framework/comfort)
[![codecov](https://codecov.io/gh/comfort-framework/comfort/branch/master/graph/badge.svg)](https://codecov.io/gh/comfort-framework/comfort)


### Description 
The COMFORT (**C**ollection **O**f **M**etrics **FOR** **T**ests) framework has the goal
to make the collection of test specific metrics easier. To achieve this goal it provides
different data loaders, filters, and metric collectors. Furhtermore, it provides different
filers, which store the results appropriately.

The metric collection can be done on method level (e.g., for each method that is annotated with @Test for
projects that use JUnit) or on class level (e.g., each class that contains methods which are annotated
with @Test). All data loaders/filters/collectors have different dependencies for their execution. E.g., some
are working only on class-level, some only for Java (or Python), and some have additional dependencies (e.g., 
the MutationDataCollector).


### Loader
- **CallGraphLoader** (Java and Python): Loads a call graph. For Java, a static call graph generator is executed, 
which is based on [java-callgraph](https://github.com/gousiosg/java-callgraph). The result
is a call graph, which shows which method calls which other method. For Python the loader loads
data which was generated using the [comfort-callgraph](https://github.com/comfort-framework/comfort-callgraph) library.

- **ChangeSetLoader** (language independent): Loads the data from a mongo database that was populated executing the 
[vcsSHARK](https://github.com/smartshark/vcsSHARK). It gets all files that were changed together with a test 
(and how often it was changed with it).

- **ClassFilesLoader** (Java): Loads all class files that are found in the project.

- **DependencyGraphLoader** (Java and Python): Loads the dependency data from a project. Result is a graph which 
shows which classes (java) or modules (python) have a dependency on another class/module. For java, we make use 
of [jdeps](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jdeps.html)

- **ProjectFilesLoader** (Java and Python): Loads data from the project directory. It detects all test files and all 
code files and stores their location.

- **TestCoverageLoader**: Loads the test coverage data that was either created using the 
[comfort-jacoco-listener](https://github.com/comfort-framework/comfort-jacoco-listener)(Java) or
the [comfort-smother](https://github.com/comfort-framework/comfort-smother) (Python). Result is a set of tests, 
together with which part in the code they are executing.

**Important notes**:
- For the DependencyGraphLoader, CallGraphLoader, and TestCoverageLoader the project must be compilable
- For the TestCoverageLoader the tests of the project must be executable.

**Filter**
- **DeletePythonPackagesFilter**: Filters out every python package (i.e., all \_\_init__ files).

- **DirectConnectionToTestFilter**: Filters out every dependency or call that is not directly outgoing from a test. 
Meaning, transatitive calls are no longer in the dataset.

- **MergeInnerClassToMainClassFilter**: Merges calls/dependencies from a inner class to the main class. E.g., if 
Class$InnerClass.m1 calls Class2.m2, after the filter was applied it results in Class.m1 calls Class2.m2.

- **SameProjectFilter**: We exclude everything in the analysis that does not belong to the project (e.g., dependencies
on java.lang)

- **TransformStaticCallGraphToDependencyGraphFilter**: This filter transforms a call graph to a 
dependency graph.


**MetricCollector**

- **CodeCoverCollector**: Calculates the test granularity based on this 
[paper](http://onlinelibrary.wiley.com/doi/10.1002/smr.362/full).

- **CoEvolutionTestTypeCollector**: (TODO: Separation of unit/integration) Detects unit tests by looking at
how often a test was changed together with another file.
 
- **DependencyCollector**: Collects data about the amount of dependent units of a test (i.e., how many units)
are needed to execute the test.
  
- **DirectnessCollector**: Collects data about how many units are directly connected with the test.

- **IEEETestTypeCollector**: Separates the tests into unit and integration test based on the IEEE unit definition. 
Hence, a test is a unit test if it only tests classes from the same package (Java) / sub-module (Python)

- **ISTQBTestTypeCollector**: Separates the tests into unit and integration test based on the ISTQB unit definition. 
Hence, a test is a unit test if it only tests exactly one class (Java) / module (Python).

- **LOCAndMcCabeCollector**: Collects CLOC, LLOC and McCabe metrics for each test.

- **MaximumCallGraphDepthCollector**: Collects the maximum depth that each test reaches in a call graph.

- **MutationDataCollector**: Collects data about generated mutations, killed mutations, mutation score and detailed
information about each generated mutation for each test using [PIT](https://github.com/hcoles/pitest). Currently,
this only works for Java Projects that use Maven as build management system.

- **NamingConventionTestTypeCollector**: Separates the tests into unit and integration test based on the name of 
the test.

- **NumAssertionCollector**: Collects the number of asserts for each test.

- **TestCoverageCollector**: Collects the coverage for each test.


**Filer**
- **CSVFiler**: Stores the results in a CSV file.

- **SmartSHARKFiler**: Stores the results in a database that was created via [SmartSHARK](https://github.com/smartshark)


**Important Notes**
Not every filter/metric collector works with every data type. In the table we will summarize what works:

**Loader**

| Loader                | Supports Java? | Supports Python? | Resulting Granularity |
|-----------------------|----------------|------------------|-----------------------|
| CallGraphLoader       | Yes            | Yes              | Method-Level          |
| ChangeSetLoader       | Yes            | Yes              | File-Level            |
| ClassFilesLoader      | Yes            | No               | File-Level            |
| DependencyGraphLoader | Yes            | Yes              | Class-Level           |
| ProjectFilesLoader    | Yes            | Yes              | File-Level            |
| TestCoverageLoader    | Yes            | Yes              | Method-Level          |

**Filter**

| DataType                 | DeletePythonPackagesFilter | DirectConnectionToTestFilter | MergeInnerClassToMainClassFilter | SameProjectFilter | TransformCallGraphToDependencyGraph |
|--------------------------|----------------------------|------------------------------|----------------------------------|-------------------|-------------------------------------|
| CallGraph (Java)         | -                          | x                            | -                                | x                 | -                                   |
| CallGraph (Python)       | -                          | x                            | -                                | x                 | -                                   |
| ChangeSet                | -                          | -                            | -                                | -                 | -                                   |
| ClassFiles (Java)        | -                          | -                            | -                                | -                 | -                                   |
| DependencyGraph (Java)   | -                          | x                            | x                                | x                 | x                                   |
| DependencyGraph (Python) | x                          | x                            | -                                | x                 | x                                   |
| ProjectFiles             | -                          | -                            | -                                | -                 | -                                   |
| Coverage Data (Java)     | -                          | -                            | -                                | -                 | -                                   |
| Coverage Data (Python)   | -                          | -                            | -                                | -                 | -                                   |

**MetricCollector**
In "()" there is given how the output is generated. Class-level means that the output can be generated per Test**CLASS**, 
while method-level means the output can be generated per Test**METHOD**

| DataType                 | CoEvolutionTestTypeCollector | DependencyCollector | DirectnessCollector           | IEEETestTypeCollector         | ISTQBTestTypeCollector        | LOCAndMcCabeCollector         | MaximumCallGraphDepthCollector | MutationDataCollector | NamingConventionTestTypeCollector | NumAssertionCollector         | TestCoverageCollector         |
|--------------------------|------------------------------|---------------------|-------------------------------|-------------------------------|-------------------------------|-------------------------------|--------------------------------|-----------------------|-----------------------------------|-------------------------------|-------------------------------|
| CallGraph (Java)         | -                            | x (method-level)    | x (class-level, method-level) | x (class-level, method-level) | x (class-level, method-level) | -                             | x (class-level, method-level)  | -                     | -                                 | x (class-level, method-level) | x (class-level, method-level) |
| CallGraph (Python)       | -                            | x (method-level)    | x (class-level, method-level) | x (class-level, method-level) | x (class-level, method-level) | -                             | x (class-level, method-level)  | -                     | -                                 | x (class-level, method-level) | -                             |
| ChangeSet                | x (class-level)              | -                   | -                             | -                             | -                             | -                             | -                              | -                     | -                                 | -                             | -                             |
| ClassFiles (Java)        | -                            | -                   | -                             | -                             | -                             | -                             | -                              | -                     | -                                 | x (method-level)              | -                             |
| DependencyGraph (Java)   | -                            | x (class-level)     | -                             | x (class-level)               | x (class-level)               | -                             | -                              | -                     | -                                 | -                             | x (class-level)               |
| DependencyGraph (Python) | -                            | x (class-level)     | -                             | x (class-level)               | x (class-level)               | -                             | -                              | -                     | -                                 | -                             | x (class-level)               |
| ProjectFiles (Java)      | -                            | -                   | -                             | -                             | -                             | x (class-level, method-level) | -                              | -                     | x (class-level)                   | -                             | -                             |
| ProjectFiles (Python)    | -                            | -                   | -                             | -                             | -                             | x (method-level)              | -                              | -                     | x (class-level)                   | -                             | -                             |
| Coverage Data (Java)     | -                            | x (method-level)    | -                             | x (class-level, method-level) | x (class-level, method-level) | -                             | -                              | x (method-level)      | -                                 | -                             | x (class-level, method-level) |
| Coverage Data (Python)   | -                            | x (method-level)    | -                             | x (class-level, method-level) | x (class-level, method-level) | -                             | -                              | -                     | -                                 | -                             | x (class-level, method-level) |


### Extending

**Loader**
To create a loader, you need to inherit from the BaseLoader class. Afterwards, you can provide annotations 
(e.g., @SupportsJava) to mark if your new loader supports Java or Python or both.

**Filter**
To create a filter you need to inherit from the BaseFilter class. Afterwards, you can create methods that
take the data type that your filter can process as input. Furthermore, you can provide annotations 
(e.g., @SupportsJava) to mark if your new filter methods supports Java or Python or both.

**MetricCollector**
To create a metriccollector you need to inherit from the BaseMetricCollector class. Afterwards, you can create 
methods that take the data type that your metriccollector can process as input. Furthermore, you can provide annotations 
(e.g., @SupportsJava) to mark if your new filter methods supports Java or Python or both and annotations
to say at which level your results will be produced (e.g., @SupportsMethod to show that your results will be
on a method-level).

**Filer**
To create a new filer you need to implement the IFiler interface.


### Test
You can test the project by calling
```bash
gradle check
```

### Build
You can build the project by calling
```bash
gradle shadowJar
```

### Use
You can call COMFORT in the following way
```bash
java -jar build/libs/comfort-1.0.0-all.jar "path_to_config.json"
```

For an example of a config.json file, please look 
[here](https://github.com/comfort-framework/comfort/tree/master/src/test/resources/test-configurations)
