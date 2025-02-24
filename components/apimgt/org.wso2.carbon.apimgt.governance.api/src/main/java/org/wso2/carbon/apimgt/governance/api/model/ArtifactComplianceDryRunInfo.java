/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.api.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the compliance dry run information of an artifact
 */
@JsonSerialize(using = ArtifactComplianceDryRunInfo.ArtifactComplianceSerializer.class)
public class ArtifactComplianceDryRunInfo {

    private final Map<APIMGovernancePolicy, Map<RulesetInfo, List<RuleViolation>>> violations = new HashMap<>();

    public Map<APIMGovernancePolicy, Map<RulesetInfo, List<RuleViolation>>> getViolations() {
        return Collections.unmodifiableMap(violations);
    }

    public void addRuleViolationsForRuleset(APIMGovernancePolicy policy, RulesetInfo ruleset,
                                            List<RuleViolation> ruleViolations) {
        violations.computeIfAbsent(policy, k -> new HashMap<>()).put(ruleset, ruleViolations);
    }

    public static String toJson(ArtifactComplianceDryRunInfo dryRunInfo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(ArtifactComplianceDryRunInfo.class, new ArtifactComplianceSerializer());
            objectMapper.registerModule(module);
            return objectMapper.writeValueAsString(dryRunInfo);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Custom JSON Serializer for ArtifactComplianceDryRunInfo.
     */
    public static class ArtifactComplianceSerializer extends JsonSerializer<ArtifactComplianceDryRunInfo> {
        @Override
        public void serialize(ArtifactComplianceDryRunInfo dryRunInfo, JsonGenerator gen,
                              SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();

            gen.writeObjectFieldStart("compliance-check");

            if (dryRunInfo.getViolations().isEmpty()) {
                // If there are no violations, return "pass"
                gen.writeStringField("result", "pass");
            } else {
                // If there are violations, return "fail"
                gen.writeStringField("result", "fail");

                gen.writeFieldName("violations");
                gen.writeStartArray();

                for (Map.Entry<APIMGovernancePolicy, Map<RulesetInfo, List<RuleViolation>>> policyEntry :
                        dryRunInfo.getViolations().entrySet()) {

                    gen.writeStartObject();
                    gen.writeStringField("policy", policyEntry.getKey().getName());

                    gen.writeFieldName("rulesets");
                    gen.writeStartArray();

                    for (Map.Entry<RulesetInfo, List<RuleViolation>> rulesetEntry : policyEntry
                            .getValue().entrySet()) {
                        gen.writeStartObject();
                        gen.writeStringField("ruleset", rulesetEntry.getKey().getName());
                        gen.writeStringField("type", String.valueOf
                                (rulesetEntry.getKey().getRuleType()));

                        gen.writeFieldName("rule-violations");
                        gen.writeStartArray();

                        for (RuleViolation violation : rulesetEntry.getValue()) {
                            gen.writeStartObject();
                            gen.writeStringField("path", violation.getViolatedPath());
                            gen.writeStringField("message", violation.getRuleMessage());
                            gen.writeStringField("severity", String.valueOf(violation.getSeverity()));
                            gen.writeEndObject();
                        }
                        gen.writeEndArray();

                        gen.writeEndObject();
                    }
                    gen.writeEndArray();
                    gen.writeEndObject();
                }

                gen.writeEndArray();
            }
            gen.writeEndObject();
            gen.writeEndObject();
        }
    }

}
