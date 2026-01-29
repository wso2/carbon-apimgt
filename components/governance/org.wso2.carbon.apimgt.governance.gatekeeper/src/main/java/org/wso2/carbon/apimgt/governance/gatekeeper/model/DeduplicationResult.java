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

package org.wso2.carbon.apimgt.governance.gatekeeper.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing the validation result from the Gatekeeper.
 */
public class DeduplicationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("isDuplicate")
    private boolean duplicate;

    @JsonProperty("highConfidence")
    private boolean highConfidence;

    @JsonProperty("conflictReports")
    private List<ConflictReport> conflictReports;

    @JsonProperty("queryApiUuid")
    private String queryApiUuid;

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("threshold")
    private double threshold;

    @JsonProperty("message")
    private String message;

    /**
     * Default constructor.
     */
    public DeduplicationResult() {
        this.conflictReports = new ArrayList<>();
        this.duplicate = false;
        this.highConfidence = false;
    }

    /**
     * Creates a result indicating no duplicates found.
     *
     * @param queryApiUuid The API that was checked
     * @param organization The organization
     * @return DeduplicationResult indicating unique API
     */
    public static DeduplicationResult unique(String queryApiUuid, String organization) {
        DeduplicationResult result = new DeduplicationResult();
        result.setQueryApiUuid(queryApiUuid);
        result.setOrganization(organization);
        result.setDuplicate(false);
        result.setMessage("No duplicate APIs found. API is unique.");
        return result;
    }

    /**
     * Creates a result indicating duplicates were found.
     *
     * @param queryApiUuid    The API that was checked
     * @param organization    The organization
     * @param conflictReports List of conflict reports
     * @param highConfidence  Whether this is a high-confidence duplicate (>95%)
     * @return DeduplicationResult indicating duplicate API
     */
    public static DeduplicationResult duplicate(String queryApiUuid, String organization,
                                                 List<ConflictReport> conflictReports, boolean highConfidence) {
        DeduplicationResult result = new DeduplicationResult();
        result.setQueryApiUuid(queryApiUuid);
        result.setOrganization(organization);
        result.setDuplicate(true);
        result.setHighConfidence(highConfidence);
        result.setConflictReports(conflictReports);

        if (highConfidence) {
            result.setMessage("High-confidence duplicate detected (>95% similarity). " +
                    "Consider reusing the existing API or creating a new version.");
        } else {
            result.setMessage("Potential duplicate APIs detected. " +
                    "Review the conflict reports for details.");
        }

        return result;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public boolean isHighConfidence() {
        return highConfidence;
    }

    public void setHighConfidence(boolean highConfidence) {
        this.highConfidence = highConfidence;
    }

    public List<ConflictReport> getConflictReports() {
        return conflictReports;
    }

    public void setConflictReports(List<ConflictReport> conflictReports) {
        this.conflictReports = conflictReports;
    }

    public void addConflictReport(ConflictReport report) {
        this.conflictReports.add(report);
    }

    public String getQueryApiUuid() {
        return queryApiUuid;
    }

    public void setQueryApiUuid(String queryApiUuid) {
        this.queryApiUuid = queryApiUuid;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DeduplicationResult{" +
                "duplicate=" + duplicate +
                ", highConfidence=" + highConfidence +
                ", conflictCount=" + conflictReports.size() +
                ", message='" + message + '\'' +
                '}';
    }
}
