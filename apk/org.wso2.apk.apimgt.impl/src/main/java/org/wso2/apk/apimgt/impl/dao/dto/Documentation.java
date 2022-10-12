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
package org.wso2.apk.apimgt.impl.dao.dto;

import java.util.Date;

/**
 * Documentation for API. There can be several types of documentation. Refer {@link DocumentationType}.
 *
 * @see DocumentationType
 */
@SuppressWarnings("unused")
public class Documentation extends DocumentationInfo {

    private static final long serialVersionUID = 1L;

    private String summary;
    private String sourceUrl;
    private DocumentVisibility visibility;
    private Date lastUpdated;
    private String filePath;
    private Date createdDate;

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
        super(type, name);
    }


    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public DocumentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(DocumentVisibility visibility) {
        this.visibility = visibility;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public enum DocumentVisibility {
        OWNER_ONLY("owner_only"), PRIVATE("private"), API_LEVEL("api_level");

        private String visibility;

        private DocumentVisibility(String visibility) {
            this.visibility = visibility;
        }
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "Documentation [summary=" + summary + ", sourceUrl=" + sourceUrl + ", visibility=" + visibility
                + ", lastUpdated=" + lastUpdated + ", filePath=" + filePath + ", createdDate=" + createdDate
                + ", otherTypeName=" + otherTypeName + ", toString()=" + super.toString() + "]";
    }

}
