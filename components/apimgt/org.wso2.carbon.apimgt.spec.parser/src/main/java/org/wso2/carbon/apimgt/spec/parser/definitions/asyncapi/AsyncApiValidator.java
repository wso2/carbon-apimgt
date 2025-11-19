/*
 *   Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.models.Document;
import io.apicurio.datamodels.models.util.JsonUtil;
import io.apicurio.datamodels.validation.DefaultSeverityRegistry;
import io.apicurio.datamodels.validation.IValidationSeverityRegistry;
import io.apicurio.datamodels.validation.ValidationProblem;
import io.apicurio.datamodels.validation.ValidationProblemSeverity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;


public class AsyncApiValidator {

    private static final Log log = LogFactory.getLog(AsyncApiValidator.class);

    private AsyncApiValidator() {
        // static util
    }

    /**
     * Utility method to safely validate the Async API content
     */
    public static boolean validateAsyncApiContent(String apiDefinition, List<String> errorMessages) {
        // Parse the incoming JSON into a Jackson ObjectNode
        ObjectNode originalParsed;
        try {
            originalParsed = (ObjectNode) JsonUtil.parseJSON(apiDefinition);
        } catch (Exception e) {
            String msg = "Invalid AsyncAPI syntax: " + e.getMessage();
            log.error(msg, e);
            errorMessages.add(msg);
            return false;
        }

        //If parsing succeeded, create Apicurio Document and run model validation
        if (originalParsed != null) {
            Document doc = Library.readDocument(originalParsed);
            if (doc == null || doc.root() == null) {
                String msg = "Unable to parse AsyncAPI definition into Apicurio Document model.";
                log.error(msg);
                errorMessages.add(msg);
                return false;
            } else {
                // Log model type (ModelType has a useful toString)
                log.debug("Parsed model type: " + doc.root().modelType());

                // Validate using Apicurio validation rules
                IValidationSeverityRegistry severityRegistry = new DefaultSeverityRegistry();
                List<ValidationProblem> problems =
                        Library.validate(doc, severityRegistry);

                if (problems == null || problems.isEmpty()) {
                    log.debug("No validation problems found.");
                    return true;
                } else {
                    // Format problems into a readable string
                    String formatted = formatProblems(problems);
                    log.debug("Validation Problems:\n" + formatted);

                    // Determine if there are any high or medium severity problems
                    // This can also include ValidationProblemSeverity low as well
                    boolean hasErrors = problems.stream()
                            .anyMatch(p -> p.severity == ValidationProblemSeverity.high
                                    || p.severity == ValidationProblemSeverity.medium);

                    // Collect all problem messages into validationErrorMessages
                    for (ValidationProblem problem : problems) {
                        errorMessages.add(problem.message);
                    }

                    if (hasErrors) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Format the list of problems as a string.
     * @param problems Validation problems
     */
    private static String formatProblems(List<ValidationProblem> problems) {
        StringBuilder builder = new StringBuilder();
        problems.forEach(problem -> {
            builder.append("[");
            builder.append(problem.errorCode);
            builder.append("] |");
            builder.append(problem.severity);
            builder.append("| {");
            builder.append(problem.nodePath.toString(true));
            builder.append("->");
            builder.append(problem.property);
            builder.append("} :: ");
            builder.append(problem.message);
            builder.append("\n");
        });
        return builder.toString();
    }

    }
