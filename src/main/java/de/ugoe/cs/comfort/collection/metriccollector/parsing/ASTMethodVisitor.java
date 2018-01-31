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

package de.ugoe.cs.comfort.collection.metriccollector.parsing;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * Based on: https://github.com/dx42/gmetrics/blob/master/src/main/groovy/org/gmetrics/metric/cyclomatic/CyclomaticComplexityAstVisitor.groovy
 * @author Fabian Trautsch
 */
public class ASTMethodVisitor extends VoidVisitorAdapter<Void> {
    private static final List<String> BOOLEAN_LOGIC_OPERATIONS = new ArrayList<String>() {
        {
            add("&&");
            add("||");
        }
    };
    private Integer complexity = 1;
    private Set<ASTMethodNode> allMethodNodes;

    private String classPackage;
    private String fqn = "";

    public ASTMethodVisitor(Set<ASTMethodNode> allMethodNodes) {
        this.allMethodNodes = allMethodNodes;
    }

    public Integer getComplexity() {
        return complexity;
    }

    public String getClassPackage() {
        return classPackage;
    }

    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        fqn += n.getNameAsString()+".";
        super.visit(n, arg);
        fqn = fqn.replace(n.getNameAsString()+".", "");
    }

    public void visit(PackageDeclaration n, Void arg) {
        classPackage = n.getNameAsString();
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        if(n.isAbstract()) {
            complexity = 0;
        } else {
            complexity = 1;
            super.visit(n, arg);

        }

        Integer cloc = 0;
        for(Comment comment : n.getAllContainedComments()) {
            cloc += comment.getEnd().get().line-comment.getBegin().get().line+1;
        }

        ASTMethodNode method = new ASTMethodNode(classPackage, StringUtils.removeEnd(fqn, "."), n.getNameAsString());
        method.setMcCC(complexity);
        method.setLLOC(n.getEnd().get().line-n.getBegin().get().line+1-cloc);
        method.setCLOC(cloc);
        allMethodNodes.add(method);
    }

    @Override
    public void visit(IfStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    // While
    public void visit(WhileStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    // For
    public void visit(ForStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    public void visit(ForeachStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }


    // switch
    public void visit(SwitchEntryStmt n, Void arg) {
        // Default statement does not count to complexity
        if(n.getLabel().isPresent()) {
            complexity++;
        }
        super.visit(n, arg);
    }

    // catch
    public void visit(CatchClause n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    // binary expression
    public void visit(BinaryExpr n, Void arg) {
        handleBinaryExpression(n);
        super.visit(n, arg);
    }

    public void visit(ConditionalExpr n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }


    private void handleBinaryExpression(BinaryExpr expr) {
        if(BOOLEAN_LOGIC_OPERATIONS.contains(expr.getOperator().asString())) {
            complexity++;
        }
    }
}
