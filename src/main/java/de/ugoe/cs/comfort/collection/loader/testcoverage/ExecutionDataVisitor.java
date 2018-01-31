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

package de.ugoe.cs.comfort.collection.loader.testcoverage;

import com.google.common.collect.Maps;
import java.util.Map;
import org.jacoco.core.data.*;

/**
 * Based on: https://github.com/SonarSource/sonar-java/blob/master/java-jacoco/src/main/java/org/sonar/plugins/jacoco/ExecutionDataVisitor.java
 * @author Fabian Trautsch
 */
public class ExecutionDataVisitor implements ISessionInfoVisitor, IExecutionDataVisitor {

    private final Map<String, ExecutionDataStore> sessions = Maps.newHashMap();
    private ExecutionDataStore executionDataStore;

    @Override
    public void visitSessionInfo(SessionInfo info) {
        String sessionId = info.getId();
        executionDataStore = sessions.computeIfAbsent(sessionId, k -> new ExecutionDataStore());
    }

    @Override
    public void visitClassExecution(ExecutionData data) {
        executionDataStore.put(data);
    }

    public Map<String, ExecutionDataStore> getSessions() {
        return sessions;
    }
}

