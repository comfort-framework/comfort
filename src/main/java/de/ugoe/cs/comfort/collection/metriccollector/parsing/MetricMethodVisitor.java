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

package de.ugoe.cs.comfort.collection.metriccollector.parsing;

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
public class MetricMethodVisitor extends EmptyVisitor {

    private MethodGen mg;
    private ConstantPoolGen cp;
    private String format;
    private static final Logger LOGGER = LogManager.getLogger(MetricMethodVisitor.class.getName());
    private int numAsserts = 0;

    MetricMethodVisitor(MethodGen m, JavaClass jc) {
        mg = m;
        cp = mg.getConstantPool();
    }

    int start() {
        if (mg.isAbstract() || mg.isNative()) {
            return 0;
        }
        for (InstructionHandle ih = mg.getInstructionList().getStart(); ih != null; ih = ih.getNext()) {
            Instruction i = ih.getInstruction();

            if (!visitInstruction(i)) {
                i.accept(this);
            }
        }
        return numAsserts;
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();
        return ((InstructionConst.getInstruction(opcode) != null)
                && !(i instanceof ConstantPushInstruction)
                && !(i instanceof ReturnInstruction));
    }

    @Override
    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL i) {
        LOGGER.debug("Called method {}", i.getMethodName(cp));
        if(i.getMethodName(cp).contains("assert")) {
            numAsserts++;
        }
    }

    @Override
    public void visitINVOKEINTERFACE(INVOKEINTERFACE i) {
        LOGGER.debug("Called method {}", i.getMethodName(cp));
        if(i.getMethodName(cp).contains("assert")) {
            numAsserts++;
        }
    }

    @Override
    public void visitINVOKESPECIAL(INVOKESPECIAL i) {
        LOGGER.debug("Called method {}", i.getMethodName(cp));
        if(i.getMethodName(cp).contains("assert")) {
            numAsserts++;
        }
    }

    @Override
    public void visitINVOKESTATIC(INVOKESTATIC i) {
        LOGGER.debug("Called method {}", i.getMethodName(cp));
        if(i.getMethodName(cp).contains("assert")) {
            numAsserts++;
        }
    }

    @Override
    public void visitINVOKEDYNAMIC(INVOKEDYNAMIC i) {
        LOGGER.debug("Called method {}", i.getMethodName(cp));
        if(i.getMethodName(cp).contains("assert")) {
            numAsserts++;
        }
    }
}
