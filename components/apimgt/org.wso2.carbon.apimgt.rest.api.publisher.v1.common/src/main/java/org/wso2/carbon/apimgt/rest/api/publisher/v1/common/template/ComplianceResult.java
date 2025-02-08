/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

/**
 * Compliance result class
 * This class is used to store the compliance result of a artifact
 */
public class ComplianceResult {

        private boolean isCompliant;
        private String message;

        public ComplianceResult(boolean isCompliant, String message) {
            this.isCompliant = isCompliant;
            this.message = message;
        }

        public boolean isCompliant() {
            return isCompliant;
        }

        public String getMessage() {
            return message;
        }
}
