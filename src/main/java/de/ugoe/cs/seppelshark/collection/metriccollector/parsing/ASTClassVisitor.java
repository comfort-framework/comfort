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

package de.ugoe.cs.seppelshark.collection.metriccollector.parsing;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Fabian Trautsch
 */
public class ASTClassVisitor extends VoidVisitorAdapter<Void> {
    private String classPackage;
    private String fqn = "";
    private ASTClassNode astClassNode;

    public ASTClassVisitor(ASTClassNode astClassNode) {
        this.astClassNode = astClassNode;
    }

    public void visit(PackageDeclaration n, Void arg) {
        classPackage = n.getNameAsString();
        super.visit(n, arg);
    }

    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        fqn += n.getNameAsString()+".";
        super.visit(n, arg);
        astClassNode.setNodePackage(classPackage);
        astClassNode.setNodeClass(StringUtils.removeEnd(fqn, "."));

        Integer cloc = 0;
        for(Comment comment : n.getAllContainedComments()) {
            cloc += comment.getEnd().get().line-comment.getBegin().get().line+1;
        }

        Integer lloc = n.getEnd().get().line-n.getBegin().get().line+1-cloc;
        astClassNode.setCLOC(cloc);
        astClassNode.setLLOC(lloc);
        fqn = fqn.replace(n.getNameAsString()+".", "");
    }
}
