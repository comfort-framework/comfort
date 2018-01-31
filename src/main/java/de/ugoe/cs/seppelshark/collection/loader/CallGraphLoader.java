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

package de.ugoe.cs.seppelshark.collection.loader;

import de.ugoe.cs.seppelshark.FileNameUtils;
import de.ugoe.cs.seppelshark.Utils;
import de.ugoe.cs.seppelshark.annotations.SupportsJava;
import de.ugoe.cs.seppelshark.annotations.SupportsPython;
import de.ugoe.cs.seppelshark.collection.loader.callgraph.ClassVisitor;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.configuration.LoaderConfiguration;
import de.ugoe.cs.seppelshark.data.graphs.CallEdge;
import de.ugoe.cs.seppelshark.data.graphs.CallGraph;
import de.ugoe.cs.seppelshark.data.graphs.CallType;
import de.ugoe.cs.seppelshark.data.models.PythonMethod;
import de.ugoe.cs.seppelshark.exception.LoaderException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.bcel.classfile.ClassParser;

/**
 * @author Fabian Trautsch
 */
public class CallGraphLoader extends BaseLoader {

    public CallGraphLoader(GeneralConfiguration generalConfiguration, LoaderConfiguration loaderConfiguration) {
        super(generalConfiguration, loaderConfiguration);
    }

    @SupportsJava
    public CallGraph loadJavaCallGraph() throws LoaderException {
        logger.info("Parsing class files...");
        ClassParser cp;
        Set<Path> classFiles;
        try {
            classFiles = Utils.getAllFilesFromProjectForRegex(generalConf.getProjectDir(), ".*\\.class");
        } catch (IOException e) {
            throw new LoaderException("Could not read project directory: "+generalConf.getProjectDir());
        }

        if(classFiles.size() == 0) {
            throw new LoaderException("No class files found at path "+generalConf.getProjectDir()+" have you "
                + "compiled the project already?");
        }
        logger.info("Building call graph network...");
        CallGraph callGraph = new CallGraph();
        try {
            FileNameUtils fileNameUtils = new FileNameUtils(generalConf);
            for(Path classFilePath: classFiles) {
                cp = new ClassParser(classFilePath.toString());
                ClassVisitor visitor = new ClassVisitor(cp.parse(), callGraph, fileNameUtils);
                visitor.start();
            }
        } catch (IOException e) {
            throw new LoaderException("Error in executing CallGraphLoader: "+e.getMessage());
        }

        logger.info("Finished extracting call graph.");
        return callGraph;
    }

    @SupportsPython
    public CallGraph loadPythonCallGraph() throws LoaderException {
        String regexToDeleteAllParanthesis = "\\s*\\([^\\)]*\\)\\s*";
        Pattern pattern = Pattern.compile(regexToDeleteAllParanthesis);

        FileNameUtils fileNameUtils = new FileNameUtils(generalConf);
        Map<String, Integer> callsFromMethod = new HashMap<>();
        try {
            List<String> fileLines = Files.readAllLines(loaderConf.getCallGraphLocation());
            logger.info("Building call graph network...");

            CallGraph callGraph = new CallGraph();
            for(String line : fileLines) {
                String[] lineParts = line.split("\\;");
                String caller = lineParts[0].split("\\,")[0];
                Path callerPath = Paths.get(lineParts[0].split("\\,")[1]);
                String callee = lineParts[1].split("\\,")[0];
                Path calleePath = Paths.get(lineParts[1].split("\\,")[1]);

                Matcher matcherCaller = pattern.matcher(caller);
                caller = matcherCaller.replaceAll("");
                Matcher matcherCallee = pattern.matcher(callee);
                callee = matcherCallee.replaceAll("");

                Integer callerCallNumber = callsFromMethod.getOrDefault(caller, 0);
                callsFromMethod.put(caller, callerCallNumber+1);

                // Get Python method from string
                PythonMethod pyCaller = getPythonMethodForCallString(caller);
                // Set correct path
                pyCaller.setFileName(fileNameUtils.getPathForPythonModuleFQN(pyCaller.getFQNOfUnit()));

                PythonMethod pyCallee = getPythonMethodForCallString(callee);
                pyCallee.setFileName(fileNameUtils.getPathForPythonModuleFQN(pyCallee.getFQNOfUnit()));

                callGraph.addEdge(new CallEdge(CallType.INVOKE_PYTHON, callerCallNumber, pyCaller, pyCallee));
                logger.debug("{} calls {}...", pyCaller, pyCallee);


            }
            logger.info("Finished extracting call graph.");
            return callGraph;
        } catch (IOException e) {
            throw new LoaderException(e.getMessage());
        }
    }

    private PythonMethod getPythonMethodForCallString(String callString) {
        // input: tests.data.demo:Demo.bar
        // Result: tests.data = package, demo = module, Demo = namespace, bar = method
        String[] parts = callString.split("\\:");
        String packageAndModule = parts[0];
        String nameSpaceAndMethod = parts[1];

        String[] packageAndModuleParts = packageAndModule.split("\\.");
        String module = packageAndModuleParts[packageAndModuleParts.length-1];
        String pPackage = String.join(".", Arrays.copyOfRange(packageAndModuleParts,
                0, packageAndModuleParts.length-1));

        String[] nameSpaceAndMethodParts = nameSpaceAndMethod.split("\\.");
        String method = nameSpaceAndMethodParts[nameSpaceAndMethodParts.length-1];
        String namespace = String.join(".", Arrays.copyOfRange(nameSpaceAndMethodParts,
                0, nameSpaceAndMethodParts.length-1));

        if(namespace.equals("")) {
            namespace = null;
        }

        if(pPackage.equals("")) {
            pPackage = null;
        }

        return new PythonMethod(pPackage, module, namespace, method, null);
    }
}
