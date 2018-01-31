/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.ugoe.cs.comfort.collection.loader.callgraph;

import de.ugoe.cs.comfort.FileNameUtils;
import de.ugoe.cs.comfort.data.graphs.CallEdge;
import de.ugoe.cs.comfort.data.graphs.CallGraph;
import de.ugoe.cs.comfort.data.graphs.CallType;
import de.ugoe.cs.comfort.data.models.JavaMethod;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The simplest of method visitors, prints any invoked method
 * signature for all method invocations.
 *
 * Class copied with modifications from CJKM: http://www.spinellis.gr/sw/ckjm/
 */
public class MethodVisitor extends EmptyVisitor {

    private MethodGen mg;
    private ConstantPoolGen cp;
    private String format;
    private CallGraph callGraph;
    private static final Logger LOGGER = LogManager.getLogger(MethodVisitor.class.getName());
    private JavaMethod methodNode;
    private FileNameUtils fileNameUtils;
    private int orderNumber = 0;

    MethodVisitor(MethodGen m, JavaClass jc, CallGraph callGraph, FileNameUtils fileNameUtils) {
        mg = m;
        this.callGraph = callGraph;
        cp = mg.getConstantPool();

        this.fileNameUtils = fileNameUtils;
        // Create a method node for this method
        this.methodNode = new JavaMethod(
                jc.getClassName(),
                mg.getName(),
                getArgumentStringForArgumentTypes(mg.getArgumentTypes()),
                getPathForToClassForClassName(jc.getClassName())
        );

    }

    private List<String> getArgumentStringForArgumentTypes(Type[] argumentTypes) {
        List<String> argumentsForMethod = new ArrayList<>();
        Arrays.stream(argumentTypes).forEach(arg -> argumentsForMethod.add(arg.toString()));
        return argumentsForMethod;
    }


    private Path getPathForToClassForClassName(String className) {
        try {
            return fileNameUtils.getPathForJavaClassFQN(className);
        } catch (FileNotFoundException e) {
            LOGGER.warn("Could not find path for class {}", className);
            return null;
        }
    }

    void start() {
        if (mg.isAbstract() || mg.isNative()) {
            return;
        }
        for (InstructionHandle ih = mg.getInstructionList().getStart(); ih != null; ih = ih.getNext()) {
            Instruction i = ih.getInstruction();

            if (!visitInstruction(i)) {
                i.accept(this);
            }
        }
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();
        return ((InstructionConst.getInstruction(opcode) != null)
                && !(i instanceof ConstantPushInstruction)
                && !(i instanceof ReturnInstruction));
    }

    @Override
    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL i) {
        JavaMethod referenceOn = new JavaMethod(
                i.getReferenceType(cp).toString(),
                i.getMethodName(cp),
                getArgumentStringForArgumentTypes(i.getArgumentTypes(cp)),
                getPathForToClassForClassName(i.getReferenceType(cp).toString())
        );
        CallEdge callEdge = new CallEdge(CallType.INVOKE_VIRTUAL, orderNumber, methodNode, referenceOn);
        callGraph.addEdge(callEdge);
        orderNumber++;

        LOGGER.debug("Created edge {} ", callEdge);
    }

    @Override
    public void visitINVOKEINTERFACE(INVOKEINTERFACE i) {
        JavaMethod referenceOn = new JavaMethod(
                i.getReferenceType(cp).toString(),
                i.getMethodName(cp),
                getArgumentStringForArgumentTypes(i.getArgumentTypes(cp)),
                getPathForToClassForClassName(i.getReferenceType(cp).toString())
        );
        CallEdge callEdge = new CallEdge(CallType.INVOKE_INTERFACE, orderNumber, methodNode, referenceOn);
        callGraph.addEdge(callEdge);
        orderNumber++;

        LOGGER.debug("Created edge {} ", callEdge);
    }

    @Override
    public void visitINVOKESPECIAL(INVOKESPECIAL i) {
        JavaMethod referenceOn = new JavaMethod(
                i.getReferenceType(cp).toString(),
                i.getMethodName(cp),
                getArgumentStringForArgumentTypes(i.getArgumentTypes(cp)),
                getPathForToClassForClassName(i.getReferenceType(cp).toString())
        );
        CallEdge callEdge = new CallEdge(CallType.INVOKE_SPECIAL, orderNumber, methodNode, referenceOn);
        callGraph.addEdge(callEdge);
        orderNumber++;

        LOGGER.debug("Created edge {} ", callEdge);
    }

    @Override
    public void visitINVOKESTATIC(INVOKESTATIC i) {
        JavaMethod referenceOn = new JavaMethod(
                i.getReferenceType(cp).toString(),
                i.getMethodName(cp),
                getArgumentStringForArgumentTypes(i.getArgumentTypes(cp)),
                getPathForToClassForClassName(i.getReferenceType(cp).toString())
        );
        CallEdge callEdge = new CallEdge(CallType.INVOKE_STATIC, orderNumber, methodNode, referenceOn);
        callGraph.addEdge(callEdge);
        orderNumber++;

        LOGGER.debug("Created edge {} ", callEdge);
    }

    @Override
    public void visitINVOKEDYNAMIC(INVOKEDYNAMIC i) {
        JavaMethod referenceOn = new JavaMethod(
                i.getType(cp).toString(),
                i.getMethodName(cp),
                getArgumentStringForArgumentTypes(i.getArgumentTypes(cp)),
                getPathForToClassForClassName(i.getReferenceType(cp).toString())
        );
        CallEdge callEdge = new CallEdge(CallType.INVOKE_DYNAMIC, orderNumber, methodNode, referenceOn);
        callGraph.addEdge(callEdge);
        orderNumber++;

        LOGGER.debug("Created edge {} ", callEdge);
    }

}
