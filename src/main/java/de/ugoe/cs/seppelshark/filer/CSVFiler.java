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

package de.ugoe.cs.seppelshark.filer;

import static java.nio.charset.StandardCharsets.UTF_8;

import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.filer.models.Mutation;
import de.ugoe.cs.seppelshark.filer.models.Result;
import de.ugoe.cs.seppelshark.filer.models.ResultSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Fabian Trautsch
 */
public class CSVFiler implements IFiler{

    @Override
    public void storeResults(GeneralConfiguration configuration, ResultSet results) throws IOException {
        List<Result> sortedResultsById = new ArrayList<>(results.getResults());
        sortedResultsById.sort(Comparator.comparing(Result::getId));

        // Create metrics csv
        List<String> metricHeaders = getHeadersForMetricsCSVFile(results);
        String resultsAsCSV = String.join(",", metricHeaders)+"\n";

        resultsAsCSV += sortedResultsById.stream()
                .map(result -> toMetricsCSVRow(result, metricHeaders))
                .collect(Collectors.joining(System.getProperty("line.separator")));

        Files.write(Paths.get(configuration.getFilerConfiguration().getMetricsCSVPath()),
                resultsAsCSV.getBytes(UTF_8));

        // Create mutation csv
        if(configuration.getFilerConfiguration().getMutationCSVPath() != null) {
            List<String> mutationHeaders = getHeadersForMutationsCSVFile(results);
            resultsAsCSV = String.join(",", mutationHeaders) + "\n";
            resultsAsCSV += sortedResultsById.stream()
                    .map(result -> toMutationCSVRow(result, mutationHeaders))
                    .flatMap(Collection::stream)
                    .collect(Collectors.joining(System.getProperty("line.separator")));


            Files.write(Paths.get(configuration.getFilerConfiguration().getMutationCSVPath()),
                    resultsAsCSV.getBytes(UTF_8));
        }
    }

    private List<String> toMutationCSVRow(Result result, List<String> headers) {
        List<String> csvRows = new ArrayList<>();
        StringBuilder csvRow;

        List<Mutation> sortedMutationResultsByLineNumber = new ArrayList<>(result.getMutationResults());
        sortedMutationResultsByLineNumber.sort(Comparator.comparing(Mutation::getLineNumber));

        for(Mutation mutationResult: sortedMutationResultsByLineNumber) {
            csvRow  = new StringBuilder();
            csvRow.append(result.getId()).append(",");
            csvRow.append(result.getPathToFile()).append(",");
            csvRow.append(mutationResult.getLocation()).append(",");
            csvRow.append(mutationResult.getMType()).append(",");
            csvRow.append(mutationResult.getLineNumber()).append(",");
            csvRow.append(mutationResult.getResult());
            csvRow.append(System.getProperty("line.separator"));

            csvRows.add(csvRow.toString().substring(0, csvRow.toString().length()
                    - System.getProperty("line.separator").length()));
        }
        return csvRows;
    }

    private List<String> getHeadersForMetricsCSVFile(ResultSet results) {
        List<String> headers = new ArrayList<>();
        headers.add("id");
        headers.add("path");

        Set<String> metricHeaders = new HashSet<>();
        for(Result result : results.getResults()) {
            metricHeaders.addAll(result.getMetrics().keySet());
        }
        headers.addAll(metricHeaders);
        return headers;
    }

    private String toMetricsCSVRow(Result result, List<String> headers) {
        StringBuilder csvRow = new StringBuilder();
        for(String header: headers) {
            switch (header) {
                case "id":
                    csvRow.append(result.getId());
                    break;
                case "path":
                    csvRow.append(result.getPathToFile());
                    break;
                default:
                    csvRow.append(result.getMetrics().get(header));
                    break;
            }
            csvRow.append(",");
        }

        return csvRow.toString().substring(0, csvRow.toString().length() - 1);
    }

    private List<String> getHeadersForMutationsCSVFile(ResultSet results) {
        List<String> headers = new ArrayList<>();
        headers.add("id");
        headers.add("path");
        headers.add("location");
        headers.add("m_type");
        headers.add("line_number");
        headers.add("result");
        return headers;
    }
}
