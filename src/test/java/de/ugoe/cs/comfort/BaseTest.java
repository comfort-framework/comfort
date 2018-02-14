/*
 * Copyright (C) 2017 University of Goettingen, Germany
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ugoe.cs.comfort;

import static org.junit.Assert.fail;

import de.ugoe.cs.comfort.data.models.JavaClass;
import de.ugoe.cs.comfort.data.models.JavaMethod;
import de.ugoe.cs.comfort.data.models.PythonMethod;
import de.ugoe.cs.comfort.data.models.PythonModule;
import de.ugoe.cs.comfort.filer.models.Mutation;
import de.ugoe.cs.comfort.filer.models.Result;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class BaseTest {
    /*
    Create Test Data
     */

    // Python DependencyGraph Test Data
    protected PythonModule pyTest1 = new PythonModule("tests", "test1", Paths.get("tests/test1.py"));
    protected PythonModule pyTest2 = new PythonModule("tests", "test2", Paths.get("tests/test2.py"));
    protected PythonModule pyTest3 = new PythonModule("tests", "test3", Paths.get("tests/test3.py"));
    protected PythonModule pyTest4 = new PythonModule("tests", "test4", Paths.get("tests/test4.py"));
    protected PythonModule pyTest5 = new PythonModule("tests", "test5", Paths.get("tests/test5.py"));
    protected PythonModule moduleInit = new PythonModule("module", "__init__", Paths.get("module/__init__.py"));
    protected PythonModule package1Init = new PythonModule("module.package1", "__init__", Paths.get("module/package1/__init__.py"));
    protected PythonModule module2 = new PythonModule("module.package1", "module2", Paths.get("module/package1/module2.py"));
    protected PythonModule subPackage1Init = new PythonModule("module.package1.sub_package1", "__init__", Paths.get("module/package1/sub_package1/__init__.py"));
    protected PythonModule module1 = new PythonModule("module.package1.sub_package1", "module1", Paths.get("module/package1/sub_package1/module1.py"));
    protected PythonModule module3 = new PythonModule("module.package1.sub_package1", "module3", Paths.get("module/package1/sub_package1/module3.py"));
    protected PythonModule pyUnittest = new PythonModule("unittest", "unittest", null);
    protected PythonModule pyMock = new PythonModule("mock", "mock", null);

    // Java DependencyGraph Test Data
    protected JavaClass main = new JavaClass("org.foo", "Main", Paths.get("src/main/java/org/foo/Main.java"));
    protected JavaClass entryController = new JavaClass("org.foo.controller", "EntryController", Paths.get("src/main/java/org/foo/controller/EntryController.java"));
    protected JavaClass iController = new JavaClass("org.foo.controller", "IController", Paths.get("src/main/java/org/foo/controller/IController.java"));
    protected JavaClass address = new JavaClass("org.foo.models", "Address", Paths.get("src/main/java/org/foo/models/Address.java"));
    protected JavaClass person = new JavaClass("org.foo.models", "Person", Paths.get("src/main/java/org/foo/models/Person.java"));
    protected JavaClass telephonebook = new JavaClass("org.foo.models", "Telephonebook", Paths.get("src/main/java/org/foo/models/Telephonebook.java"));
    protected JavaClass entryView = new JavaClass("org.foo.view", "EntryView", Paths.get("src/main/java/org/foo/view/EntryView.java"));
    protected JavaClass personView = new JavaClass("org.foo.view", "PersonView", Paths.get("src/main/java/org/foo/view/PersonView.java"));
    protected JavaClass fooTest = new JavaClass("unit", "fooTest", Paths.get("src/test/java/unit/fooTest.java"));
    protected JavaClass blubTest = new JavaClass("integration", "blubTest", Paths.get("src/test/java/integration/blubTest.java"));
    protected JavaClass testController = new JavaClass("org.foo.controller", "testController", Paths.get("src/test/java/org/foo/controller/testController.java"));
    protected JavaClass addressTest = new JavaClass("org.foo.models", "AddressTest", Paths.get("src/test/java/org/foo/models/AddressTest.java"));
    protected JavaClass personTest = new JavaClass("org.foo.models", "Persontest", Paths.get("src/test/java/org/foo/models/Persontest.java"));
    protected JavaClass blatestbla = new JavaClass("org.foo.view", "blatestbla", Paths.get("src/test/java/org/foo/view/blatestbla.java"));
    protected JavaClass testEntryView = new JavaClass("org.foo.view", "TestEntryView", Paths.get("src/test/java/org/foo/view/TestEntryView.java"));
    protected JavaClass object = new JavaClass("java.lang", "Object", null);
    protected JavaClass exception = new JavaClass("java.lang", "Exception", null);
    protected JavaClass string = new JavaClass("java.lang", "String", null);
    protected JavaClass integer = new JavaClass("java.lang", "Integer", null);
    protected JavaClass jassert = new JavaClass("org.junit", "Assert", null);
    protected JavaClass test = new JavaClass("org.junit", "Test", null);
    protected JavaClass C1 = new JavaClass("org.foo", "C1", null);
    protected JavaClass C11 = new JavaClass("org.foo", "C1$1", null);
    protected JavaClass C1subclass = new JavaClass("org.foo", "C1$subclass$subclass1", null);
    protected JavaClass C2 = new JavaClass("org.foo", "C2", null);
    protected JavaClass C21 = new JavaClass("org.foo", "C2$1", null);
    protected JavaClass C3 = new JavaClass("org.foo", "C3", null);
    protected JavaClass C4 = new JavaClass("org.foo", "C4", null);
    protected JavaClass T1 = new JavaClass("org.foo", "Test1", null);
    protected JavaClass T2 = new JavaClass("org.foo", "Test2", null);
    protected JavaClass T3 = new JavaClass("org.foo", "Test3", null);

    // Java CallGraph Test Data
    protected JavaMethod entryControllerInit = new JavaMethod("org.foo.controller", "EntryController", "<init>", new ArrayList<>(), Paths.get("src/main/java/org/foo/controller/EntryController.java"));
    protected JavaMethod iControllerInit = new JavaMethod("org.foo.controller", "IController", "<clinit>", new ArrayList<>(), Paths.get("src/main/java/org/foo/controller/IController.java"));
    protected JavaMethod addressInit = new JavaMethod("org.foo.models", "Address", "<init>", new ArrayList<>(), Paths.get("src/main/java/org/foo/models/Address.java"));

    protected JavaMethod addressInitWithParam = new JavaMethod("org.foo.models", "Address", "<init>", new ArrayList<String>() {{
        add("java.lang.String");
        add("java.lang.Integer");
    }}, Paths.get("src/main/java/org/foo/models/Address.java"));
    protected JavaMethod addressGetStreet = new JavaMethod("org.foo.models", "Address", "getStreet", new ArrayList<>(), Paths.get("src/main/java/org/foo/models/Address.java"));
    protected JavaMethod personInit = new JavaMethod("org.foo.models", "Person", "<init>", new ArrayList<>(), Paths.get("src/main/java/org/foo/models/Person.java"));
    protected JavaMethod telephoneBookInit = new JavaMethod("org.foo.models", "Telephonebook", "<init>", new ArrayList<String>() {{
        add("org.foo.models.Person");
        add("org.foo.controller.EntryController");
    }}, Paths.get("src/main/java/org/foo/models/Telephonebook.java"));
    protected JavaMethod entryViewInit = new JavaMethod("org.foo.view", "EntryView", "<init>", new ArrayList<>(), Paths.get("src/main/java/org/foo/view/EntryView.java"));
    protected JavaMethod personViewInit = new JavaMethod("org.foo.view", "PersonView", "<init>", new ArrayList<>(), Paths.get("src/main/java/org/foo/view/PersonView.java"));
    protected JavaMethod mainInit = new JavaMethod("org.foo", "Main", "<init>", new ArrayList<>(), Paths.get("src/main/java/org/foo/Main.java"));
    protected JavaMethod blubTestInit = new JavaMethod("integration", "blubTest", "<init>", new ArrayList<>(), Paths.get("src/test/java/integration/blubTest.java"));
    protected JavaMethod testControllerInit = new JavaMethod("org.foo.controller", "testController", "<init>", new ArrayList<>(), Paths.get("src/test/java/org/foo/controller/testController.java"));
    protected   JavaMethod addressTestInit = new JavaMethod("org.foo.models", "AddressTest", "<init>", new ArrayList<>(), Paths.get("src/test/java/org/foo/models/AddressTest.java"));
    protected  JavaMethod addressTestGetAddressTest = new JavaMethod("org.foo.models", "AddressTest", "getAddressTest", new ArrayList<>(), Paths.get("src/test/java/org/foo/models/AddressTest.java"));
    protected   JavaMethod addressTestGetAddressTest2 = new JavaMethod("org.foo.models", "AddressTest", "getAddressTest2", new ArrayList<>(), Paths.get("src/test/java/org/foo/models/AddressTest.java"));
    protected   JavaMethod personTestInit = new JavaMethod("org.foo.models", "Persontest", "<init>", new ArrayList<>(), Paths.get("src/test/java/org/foo/models/Persontest.java"));
    protected   JavaMethod personTestm2 = new JavaMethod("org.foo.models", "Persontest", "m2", new ArrayList<>(), Paths.get("src/test/java/org/foo/models/Persontest.java"));
    protected   JavaMethod blatestblaInit = new JavaMethod("org.foo.view", "blatestbla", "<init>", new ArrayList<>(), Paths.get("src/test/java/org/foo/view/blatestbla.java"));
    protected   JavaMethod testEntryViewInit = new JavaMethod("org.foo.view", "TestEntryView", "<init>", new ArrayList<>(), Paths.get("src/test/java/org/foo/view/TestEntryView.java"));
    protected   JavaMethod fooTestInit = new JavaMethod("unit", "fooTest", "<init>", new ArrayList<>(), Paths.get("src/test/java/unit/fooTest.java"));
    protected   JavaMethod javaLangObjectInit = new JavaMethod("java.lang", "Object", "<init>", new ArrayList<>(), null);
    protected   JavaMethod fooIT = new JavaMethod("org.foo", "FooIT", "<init>", new ArrayList<>(), Paths.get("src/test/java/org/foo/FooIT.java"));
    protected   JavaMethod fooIT2 = new JavaMethod("org.foo", "FooIT", "m2", new ArrayList<>(), Paths.get("src/test/java/org/foo/FooIT.java"));
    protected   JavaMethod javaLangIntegerValueOf = new JavaMethod("java.lang", "Integer", "valueOf", new ArrayList<String>() {{
        add("int");
    }}, null);
    protected   JavaMethod javaLangExceptionInit = new JavaMethod("java.lang", "Exception", "<init>", new ArrayList<String>() {{
        add("java.lang.String");
    }}, null);
    protected   JavaMethod junitAssertEquals = new JavaMethod("org.junit", "Assert", "assertEquals", new ArrayList<String>() {{
        add("java.lang.Object");
        add("java.lang.Object");
    }}, null);
    protected   JavaMethod junitAssertNotNull = new JavaMethod("org.junit", "Assert", "assertNotNull", new ArrayList<String>() {{
        add("java.lang.Object");
    }}, null);

    protected   JavaMethod T1M1 = new JavaMethod("org.foo.t1.Test1", "m1", new ArrayList<>(), null);
    protected   JavaMethod T1Test1 = new JavaMethod("org.foo.t1.Test1", "test1", new ArrayList<>(), null);
    protected   JavaMethod T1M2 = new JavaMethod("org.foo.t1.Test1", "m2", new ArrayList<>(), null);
    protected   JavaMethod T2M1 = new JavaMethod("org.foo.t2.Test2", "m1", new ArrayList<>(), null);
    protected   JavaMethod T2Test1 = new JavaMethod("org.foo.t2.Test2", "test1", new ArrayList<>(), null);
    protected   JavaMethod T2Test2 = new JavaMethod("org.foo.t2.Test2", "test2", new ArrayList<>(), null);
    protected   JavaMethod T2M2 = new JavaMethod("org.foo.t2.Test2", "m2", new ArrayList<>(), null);
    protected   JavaMethod T3Test1 = new JavaMethod("org.foo.t3.Test3", "test1", new ArrayList<>(), null);
    protected   JavaMethod C1M1_p1 = new JavaMethod("org.foo.bar.C1", "m1", new ArrayList<>(), null);
    protected   JavaMethod C1M1_p2 = new JavaMethod("org.foo.C1", "m1", new ArrayList<>(), null);
    protected   JavaMethod C2M1_p1 = new JavaMethod("org.foo.bar.C2", "m1", new ArrayList<>(), null);
    protected   JavaMethod C2Foo_p1 = new JavaMethod("org.foo.bar.C2", "foo", new ArrayList<>(), null);
    protected   JavaMethod C2M1_p2 = new JavaMethod("org.foo.bar.blub.C2", "m1", new ArrayList<>(), null);
    protected   JavaMethod C3M1 = new JavaMethod("org.foo.bar.C3", "m1", new ArrayList<>(), null);
    protected   JavaMethod C3M2 = new JavaMethod("org.foo.bar.C3", "m2", new ArrayList<>(), null);
    protected   JavaMethod C4M1 = new JavaMethod("org.foo.bar.C4", "m1", new ArrayList<>(), null);
    protected   JavaMethod covP1C1M1 = new JavaMethod("org.foo.bar.C1", "m1", new ArrayList<>(), null);
    protected   JavaMethod covP1C1M2 = new JavaMethod("org.foo.bar.C1", "m2", new ArrayList<>(), null);
    protected   JavaMethod covP1C2M1 = new JavaMethod("org.foo.bar.C2", "m1", new ArrayList<>(), null);
    protected   JavaMethod covP2C1M1 = new JavaMethod("org.foo.C1", "m1", new ArrayList<>(), null
    );

    // Python CallGraph Test Data
    protected PythonMethod testCallDemo = new PythonMethod("tests.data", "demo_testsuite", null, "test_callDemo", Paths.get("tests/data/demo_testsuite.py"));
    protected PythonMethod callDemo = new PythonMethod("tests.data", "demo", null, "callDemo", Paths.get("tests/data/demo.py"));
    protected PythonMethod demoInit = new PythonMethod("tests.data", "demo", "Demo", "__init__", Paths.get("tests/data/demo.py"));
    protected PythonMethod demoBar = new PythonMethod("tests.data", "demo", "Demo", "bar", Paths.get("tests/data/demo.py"));
    protected PythonMethod demoFoo = new PythonMethod("tests.data", "demo", "Demo", "foo", Paths.get("tests/data/demo.py"));
    protected PythonMethod pyTestDemoInit = new PythonMethod("package1.package2", "test_demo", "TestDemo", "__init__", null);
    protected PythonMethod pyModule1Init = new PythonMethod("package1.package2", "module1", "Module1", "__init__", null);
    protected PythonMethod pyModule2Init = new PythonMethod("package1.package2", "module2", "Module2", "__init__", null);
    protected PythonMethod pyModule2Foo = new PythonMethod("package1.package2", "module2", "Module2", "foo", null);
    protected PythonMethod pyModule3Init = new PythonMethod("package1.package2", "module3", "Module3", "__init__", null);
    protected PythonMethod pyModule4Init = new PythonMethod("package1.package2", "module4", "Module4", "__init__", null);
    protected PythonMethod pyModule5Init = new PythonMethod("package1.package2.package3", "module5", "Module5", "__init__", null);
    protected PythonMethod pyUnittestInit = new PythonMethod("unittest", "unittest", null, "__init__", null);
    protected PythonMethod pyTest1Test = new PythonMethod("tests", "test_module1", "Module1Test", "test", null);
    protected PythonMethod pyTest1Test2 = new PythonMethod("tests", "test_module1", "Module1Test", "test2", null);
    protected PythonMethod pyTest2Test = new PythonMethod("tests", "test_module2", "Module2Test", "test", null);
    protected PythonMethod pyTest2Test2 = new PythonMethod("tests", "test_module2", "Module2Test", "test2", null);

    
    // Java Coverage Data
    protected JavaMethod testMethodThatCallsOtherMethod = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "MethodThatCallsOtherMethodTest", "testMethodThatCallsOtherMethod", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/MethodThatCallsOtherMethodTest.java"));
    protected JavaMethod testCallToSubClassMethod = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "CallsSubClassMethodTest", "testCallToSubClassMethod", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/CallsSubClassMethodTest.java"));
    protected JavaMethod testSameMethodTested = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "SameMethod1Test", "testSameMethod", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/SameMethod1Test.java"));
    protected JavaMethod sameMethod2Test = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "SameMethod2Test", "testSameMethod", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/SameMethod2Test.java"));
    protected JavaMethod testSameMethodInDifferentClasses = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "SameMethodInDifferentClassesTest", "testSameMethodInDifferentClasses", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/SameMethodInDifferentClassesTest.java"));
    protected JavaMethod test1 = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "WithDifferentMethodsCallDifferentMethodsTest", "test1", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/WithDifferentMethodsCallDifferentMethodsTest.java"));
    protected JavaMethod test2 = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "WithDifferentMethodsCallDifferentMethodsTest", "test2", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/WithDifferentMethodsCallDifferentMethodsTest.java"));
    protected JavaMethod withDifferentMethodsCallSameMethodTest = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "WithDifferentMethodsCallSameMethodTest", "test1", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/WithDifferentMethodsCallSameMethodTest.java"));
    protected JavaMethod withDifferentMethodsCallSameMethodTest2 = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "WithDifferentMethodsCallSameMethodTest", "test2", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/WithDifferentMethodsCallSameMethodTest.java"));
    protected JavaMethod ifInMethodTest = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "IfInMethodTest", "test1", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/IfInMethodTest.java"));
    protected JavaMethod callMethodWithSameParameterFromDifferentPackagesTest = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "CallMethodWithSameParameterFromDifferentPackagesTest", "test1", new ArrayList<>(),
            Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/CallMethodWithSameParameterFromDifferentPackagesTest.java"));
    protected JavaMethod M1SubclassInit = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "Module1$SubClassModule1", "<init>", new ArrayList<>(),
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/Module1.java"));
    protected JavaMethod M1SubclassSum = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "Module1$SubClassModule1", "sum", new ArrayList<String>(){{add("int"); add("int");}},
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/Module1.java"));
    protected JavaMethod Module1Init = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "Module1", "<init>", new ArrayList<>(),
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/Module1.java"));
    protected JavaMethod Module1Sum = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "Module1", "sum", new ArrayList<String>(){{add("int"); add("int");}},
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/Module1.java"));
    protected JavaMethod Module2module1Call = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "Module2", "module1Call", new ArrayList<String>(){{add("int"); add("int");}},
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/Module2.java"));
    protected JavaMethod Module2Sum = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "Module2", "sum", new ArrayList<String>(){{add("int"); add("int");}},
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/Module2.java"));
    protected JavaMethod Module3BiggerThan = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "Module3", "biggerThan", new ArrayList<String>(){{add("java.lang.Integer"); add("java.lang.Integer");}},
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/Module3.java"));
    protected JavaMethod Module3CompareModule = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "Module3", "compareModule", new ArrayList<String>(){{add("de.ugoe.cs.comfort.codecoverage.Module4"); add("de.ugoe.cs.comfort.codecoverage.pkg.Module4");}},
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/Module3.java"));
    protected JavaMethod Module3CompareModule2 = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "Module3", "compareModule", new ArrayList<String>(){{add("de.ugoe.cs.comfort.codecoverage.Module4"); add("de.ugoe.cs.comfort.codecoverage.Module4");}},
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/Module3.java"));
    protected JavaMethod Module4Init = new JavaMethod("de.ugoe.cs.comfort.codecoverage", "Module4", "<init>", new ArrayList<>(),
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/Module4.java"));
    protected JavaMethod Module4PkgInit = new JavaMethod("de.ugoe.cs.comfort.codecoverage.pkg", "Module4", "<init>", new ArrayList<>(),
            Paths.get("src/main/java/de/ugoe/cs/comfort/codecoverage/pkg/Module4.java"));


    // Results
    protected Result res1 = new Result("de.foo.bar.ModelTest", Paths.get("src/de/foo/bar/ModelTest.java"));
    protected Result res2 = new Result("de.foo.bar.ModelTest1", Paths.get("src/de/foo/bar/ModelTest1.java"));

    protected Set<Mutation> mutations1 = new HashSet<Mutation>(){{
        add(new Mutation(
                "de.foo.bar.Model.addBatch",
                "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator",
                10,
                "NO_COVERAGE",
                null));
        add(new Mutation(
                "de.foo.bar.Model.addBatch",
                "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator",
                11,
                "SURVIVED",
                "INTERFACE"));
        add(new Mutation(
                "de.foo.bar.Model.addBatch",
                "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator",
                30,
                "SURVIVED",
                "INTERFACE"));
    }};

    protected Set<Mutation> mutations2 = new HashSet<Mutation>(){{
        add(new Mutation(
                "de.foo.bar.Model.addBatch",
                "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator",
                10,
                "NO_COVERAGE",
                null));
        add(new Mutation(
                "de.foo.bar.Model.addBatch",
                "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator",
                30,
                "KILLED",
                "INTERFACE"));
    }};





    protected String getPathToResource(String resourceName){
        try {
            return ClassLoader.getSystemClassLoader().getResource(resourceName).getFile();
        } catch (NullPointerException e) {
            fail("Resource "+resourceName+" could not be found in resource folder!");
        }
        return null;
    }
    
    
}
