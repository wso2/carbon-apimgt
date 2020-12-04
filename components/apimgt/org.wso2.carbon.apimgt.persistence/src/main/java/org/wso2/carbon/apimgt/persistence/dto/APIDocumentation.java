/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.persistence.dto;

import org.bson.types.ObjectId;
import org.wso2.carbon.apimgt.api.model.DocumentationType;

import java.util.Date;

public class APIDocumentation {
    private ObjectId id;
    private String gridFsReference;
    private String name;
    private String summary;
    private String sourceUrl;
    private Documentation.DocumentVisibility visibility;
    private Date lastUpdated;
    private String filePath;
    private Date createdDate;
    private DocumentContent documentContent;
    private DocumentationType documentationType;
    private Documentation.DocumentSourceType documentationSourceType;

    public Documentation.DocumentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Documentation.DocumentVisibility visibility) {
        this.visibility = visibility;
    }

    public DocumentationType getDocumentationType() {
        return documentationType;
    }

    public void setDocumentationType(DocumentationType documentationType) {
        this.documentationType = documentationType;
    }

    public Documentation.DocumentSourceType getDocumentationSourceType() {
        return documentationSourceType;
    }

    public void setDocumentationSourceType(
            Documentation.DocumentSourceType documentationSourceType) {
        this.documentationSourceType = documentationSourceType;
    }

    public DocumentContent getDocumentContent() {
        return documentContent;
    }

    public void setDocumentContent(DocumentContent documentContent) {
        this.documentContent = documentContent;
    }

    public String getGridFsReference() {
        return gridFsReference;
    }

    public void setGridFsReference(String gridFsReference) {
        this.gridFsReference = gridFsReference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
