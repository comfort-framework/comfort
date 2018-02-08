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

package de.ugoe.cs.comfort.collection.metriccollector.mutation.operators;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Fabian Trautsch
 */
public abstract class BaseOperator {
    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    protected Path file;

    protected List<String> lines;
    protected int normalizedLineNumber;
    protected String line;

    public BaseOperator(Path file, int lineNumber) throws IOException {
        this.file = file;

        // Read in File
        lines = Files.readAllLines(file);
        logger.debug("Read lines: {}", lines);

        // Set normalized line number (because Arrays start at 0 and not 1)
        normalizedLineNumber = lineNumber-1;

        // Get line that should be changed
        line = lines.get(normalizedLineNumber);
        logger.info("Reading Line: "+line);
    }

    protected void storeChangedLine(String newLine) throws IOException {
        logger.debug("New Line: "+newLine);
        lines.set(normalizedLineNumber, newLine);

        logger.debug("Writing to: "+file);
        Files.write(file, lines, Charset.forName("UTF-8"));
    }

    protected String getChangedNewLineForPattern(String character, String replacement) {
        Pattern pat = Pattern.compile(Pattern.quote(character)+"+");
        Matcher matcher = pat.matcher(line);

        String changedLine = line;
        while(matcher.find()) {
            if(matcher.group().equals(character)) {
                changedLine = changedLine.substring(0, matcher.start())+replacement+line.substring(matcher.end());
            }
        }
        return changedLine;
    }

    public abstract void changeFile() throws IOException;
}
