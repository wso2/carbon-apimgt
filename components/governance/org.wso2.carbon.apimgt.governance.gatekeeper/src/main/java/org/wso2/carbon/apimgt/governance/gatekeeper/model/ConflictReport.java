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
import java.util.HashMap;
import java.util.Map;

/**
 * Model class for conflict report when a duplicate API is detected.
 * Provides detailed evidence about the matched API and similarity metrics.
 */
public class ConflictReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("matchedApiUuid")
    private String matchedApiUuid;

    @JsonProperty("matchedApiName")
    private String matchedApiName;

    @JsonProperty("matchedApiVersion")
    private String matchedApiVersion;

    @JsonProperty("matchedApiContext")
    private String matchedApiContext;

    @JsonProperty("similarityScore")
    private double similarityScore;

    @JsonProperty("metadataSimilarity")
    private Map<String, Object> metadataSimilarity;

    @JsonProperty("pathSimilarity")
    private double pathSimilarity;

    @JsonProperty("schemaSimilarity")
    private double schemaSimilarity;

    @JsonProperty("message")
    private String message;

    @JsonProperty("recommendation")
    private String recommendation;

    /**
     * Default constructor.
     */
    public ConflictReport() {
        this.metadataSimilarity = new HashMap<>();
    }

    /**
     * Builder class for ConflictReport.
     */
    public static class Builder {
        private final ConflictReport report;

        public Builder() {
            this.report = new ConflictReport();
        }

        public Builder matchedApiUuid(String matchedApiUuid) {
            report.matchedApiUuid = matchedApiUuid;
            return this;
        }

        public Builder matchedApiName(String matchedApiName) {
            report.matchedApiName = matchedApiName;
            return this;
        }

        public Builder matchedApiVersion(String matchedApiVersion) {
            report.matchedApiVersion = matchedApiVersion;
            return this;
        }

        public Builder matchedApiContext(String matchedApiContext) {
            report.matchedApiContext = matchedApiContext;
            return this;
        }

        public Builder similarityScore(double similarityScore) {
            report.similarityScore = similarityScore;
            return this;
        }

        public Builder metadataSimilarity(Map<String, Object> metadataSimilarity) {
            report.metadataSimilarity = metadataSimilarity;
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            report.metadataSimilarity.put(key, value);
            return this;
        }

        public Builder pathSimilarity(double pathSimilarity) {
            report.pathSimilarity = pathSimilarity;
            return this;
        }

        public Builder schemaSimilarity(double schemaSimilarity) {
            report.schemaSimilarity = schemaSimilarity;
            return this;
        }

        public Builder message(String message) {
            report.message = message;
            return this;
        }

        public Builder recommendation(String recommendation) {
            report.recommendation = recommendation;
            return this;
        }

        public ConflictReport build() {
            return report;
        }
    }

    public String getMatchedApiUuid() {
        return matchedApiUuid;
    }

    public void setMatchedApiUuid(String matchedApiUuid) {
        this.matchedApiUuid = matchedApiUuid;
    }

    public String getMatchedApiName() {
        return matchedApiName;
    }

    public void setMatchedApiName(String matchedApiName) {
        this.matchedApiName = matchedApiName;
    }

    public String getMatchedApiVersion() {
        return matchedApiVersion;
    }

    public void setMatchedApiVersion(String matchedApiVersion) {
        this.matchedApiVersion = matchedApiVersion;
    }

    public String getMatchedApiContext() {
        return matchedApiContext;
    }

    public void setMatchedApiContext(String matchedApiContext) {
        this.matchedApiContext = matchedApiContext;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public Map<String, Object> getMetadataSimilarity() {
        return metadataSimilarity;
    }

    public void setMetadataSimilarity(Map<String, Object> metadataSimilarity) {
        this.metadataSimilarity = metadataSimilarity;
    }

    public double getPathSimilarity() {
        return pathSimilarity;
    }

    public void setPathSimilarity(double pathSimilarity) {
        this.pathSimilarity = pathSimilarity;
    }

    public double getSchemaSimilarity() {
        return schemaSimilarity;
    }

    public void setSchemaSimilarity(double schemaSimilarity) {
        this.schemaSimilarity = schemaSimilarity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    @Override
    public String toString() {
        return "ConflictReport{" +
                "matchedApiName='" + matchedApiName + '\'' +
                ", matchedApiUuid='" + matchedApiUuid + '\'' +
                ", similarityScore=" + String.format("%.2f%%", similarityScore * 100) +
                ", message='" + message + '\'' +
                '}';
    }
}
