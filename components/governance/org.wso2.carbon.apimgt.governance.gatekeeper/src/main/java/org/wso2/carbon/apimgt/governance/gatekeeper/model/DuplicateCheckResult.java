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

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a duplicate check operation.
 * Contains information about whether duplicates were found and details about similar APIs.
 */
public class DuplicateCheckResult {

    private String apiId;
    private boolean hasDuplicates;
    private List<SimilarAPI> similarAPIs;

    /**
     * Default constructor.
     */
    public DuplicateCheckResult() {
        this.similarAPIs = new ArrayList<>();
    }

    /**
     * Gets the API ID that was checked.
     *
     * @return API ID
     */
    public String getApiId() {
        return apiId;
    }

    /**
     * Sets the API ID that was checked.
     *
     * @param apiId API ID
     */
    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    /**
     * Checks if duplicates were found.
     *
     * @return true if duplicates exist
     */
    public boolean hasDuplicates() {
        return hasDuplicates;
    }

    /**
     * Sets whether duplicates were found.
     *
     * @param hasDuplicates true if duplicates exist
     */
    public void setHasDuplicates(boolean hasDuplicates) {
        this.hasDuplicates = hasDuplicates;
    }

    /**
     * Gets the list of similar APIs.
     *
     * @return List of similar APIs
     */
    public List<SimilarAPI> getSimilarAPIs() {
        return similarAPIs;
    }

    /**
     * Sets the list of similar APIs.
     *
     * @param similarAPIs List of similar APIs
     */
    public void setSimilarAPIs(List<SimilarAPI> similarAPIs) {
        this.similarAPIs = similarAPIs;
    }

    /**
     * Represents a similar API found during duplicate check.
     */
    public static class SimilarAPI {
        private String apiId;
        private double similarityScore;

        /**
         * Constructor.
         *
         * @param apiId           The similar API's ID
         * @param similarityScore The similarity score (0.0 to 1.0)
         */
        public SimilarAPI(String apiId, double similarityScore) {
            this.apiId = apiId;
            this.similarityScore = similarityScore;
        }

        /**
         * Gets the similar API's ID.
         *
         * @return API ID
         */
        public String getApiId() {
            return apiId;
        }

        /**
         * Sets the similar API's ID.
         *
         * @param apiId API ID
         */
        public void setApiId(String apiId) {
            this.apiId = apiId;
        }

        /**
         * Gets the similarity score.
         *
         * @return Similarity score between 0.0 and 1.0
         */
        public double getSimilarityScore() {
            return similarityScore;
        }

        /**
         * Sets the similarity score.
         *
         * @param similarityScore Similarity score between 0.0 and 1.0
         */
        public void setSimilarityScore(double similarityScore) {
            this.similarityScore = similarityScore;
        }

        @Override
        public String toString() {
            return "SimilarAPI{" 
                    + "apiId='" + apiId + '\'' 
                    + ", similarityScore=" + similarityScore 
                    + '}';
        }
    }

    @Override
    public String toString() {
        return "DuplicateCheckResult{" 
                + "apiId='" + apiId + '\'' 
                + ", hasDuplicates=" + hasDuplicates 
                + ", similarAPIs=" + similarAPIs 
                + '}';
    }
}
