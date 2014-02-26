/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.api.model;

import java.util.Date;

/**
 * Documentation for API. There can be several types of documentation. Refer {@link DocumentationType}.
 *
 * @see DocumentationType
 */
@SuppressWarnings("unused")
public class Documentation {
    private DocumentationType type;
    private String name;
    private String summary;
    private DocumentSourceType sourceType;
    private String sourceUrl;
    private Date lastUpdated;
    private String filePath;

    public String getOtherTypeName() {
        return otherTypeName;
    }

    public void setOtherTypeName(String otherTypeName) {
        this.otherTypeName = otherTypeName;
    }

    private String otherTypeName;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Documentation(DocumentationType type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Documentation that = (Documentation) o;

        return name.equals(that.name) && type == that.type;

    }

    public DocumentationType getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public DocumentSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(DocumentSourceType sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public enum DocumentSourceType {
        INLINE("In line"), URL("URL"), FILE("File");

        private String type;

        private DocumentSourceType(String type) {
            this.type = type;
        }
    }
}
