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

import static org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants.DELEM_UNDERSCORE;
import static org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants.JSON_FILE_TYPE;
import static org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants.PATH_SEPARATOR;
import static org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants.YAML_FILE_TYPE;
import static org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants.YML_FILE_TYPE;

/**
 * This class represents the Ruleset Content
 */
public class RulesetContent {
    byte[] content;
    ContentType contentType;
    String fileName;

    /**
     * Content Type of the Ruleset
     */
    public enum ContentType {
        YAML,
        JSON
    }

    public RulesetContent() {
    }

    public RulesetContent(RulesetContent other) {
        if (other != null) {
            this.content = other.content != null ? other.content.clone() : null;
            this.contentType = other.contentType;
            this.fileName = other.fileName;
        }
    }

    public byte[] getContent() {
        return content != null ? content.clone() : new byte[0];
    }

    public void setContent(byte[] content) {
        this.content = content != null ? content.clone() : null;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        fileName = fileName.replaceAll(PATH_SEPARATOR, DELEM_UNDERSCORE);
        if (fileName.endsWith(YAML_FILE_TYPE) || fileName.endsWith(YML_FILE_TYPE)) {
            this.contentType = ContentType.YAML;
        } else if (fileName.endsWith(JSON_FILE_TYPE)) {
            this.contentType = ContentType.JSON;
        }
        this.fileName = fileName;
    }
}
