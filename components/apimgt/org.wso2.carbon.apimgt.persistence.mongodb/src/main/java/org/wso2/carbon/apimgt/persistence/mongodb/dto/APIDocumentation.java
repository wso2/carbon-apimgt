
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

package org.wso2.carbon.apimgt.persistence.mongodb.dto;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.DocumentationInfo;

import java.util.Date;

public class APIDocumentation {
    @BsonProperty(value = "docId")
    private ObjectId id;
    private ObjectId gridFsReference;
    private String name;
    private String summary;
    private String sourceUrl;
    private Documentation.DocumentVisibility visibility;
    private Date lastUpdated;
    private String filePath;
    private Date createdDate;
    private String textContent;
    private DocumentationType type;
    private DocumentationInfo.DocumentSourceType sourceType;
    private String contentType;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Documentation.DocumentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Documentation.DocumentVisibility visibility) {
        this.visibility = visibility;
    }

    public DocumentationType getDocumentationType() {
        return type;
    }

    public void setDocumentationType(DocumentationType documentationType) {
        this.type = documentationType;
    }

    public DocumentationType getType() {
        return type;
    }

    public void setType(DocumentationType type) {
        this.type = type;
    }

    public DocumentationInfo.DocumentSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(DocumentationInfo.DocumentSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public ObjectId getGridFsReference() {
        return gridFsReference;
    }

    public void setGridFsReference(ObjectId gridFsReference) {
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

