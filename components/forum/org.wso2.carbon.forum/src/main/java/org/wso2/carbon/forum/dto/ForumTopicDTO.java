/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.forum.dto;

import java.util.Date;
import java.util.List;

public class ForumTopicDTO {

    private String topicId;

    private String subject;

    private String description;

    private String topicOwner;

    private String topicOwnerTenantDomain;

    private String topicResourceIdentifier;

    private Date createdDate;

    private long timestamp;

    private List<ForumReplyDTO> replies;

    private long replyCount;

    private String lastReplyBy;

    private long lastReplyTimestamp;

    private int userRating;

    private float averageRating;

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTopicOwner() {
        return topicOwner;
    }

    public void setTopicOwner(String topicOwner) {
        this.topicOwner = topicOwner;
    }

    public String getTopicOwnerTenantDomain() {
        return topicOwnerTenantDomain;
    }

    public void setTopicOwnerTenantDomain(String topicOwnerTenantDomain) {
        this.topicOwnerTenantDomain = topicOwnerTenantDomain;
    }

    public String getTopicResourceIdentifier() {
        return topicResourceIdentifier;
    }

    public void setTopicResourceIdentifier(String topicResourceIdentifier) {
        this.topicResourceIdentifier = topicResourceIdentifier;
    }

    public List<ForumReplyDTO> getReplies() {
        return replies;
    }

    public void setReplies(List<ForumReplyDTO> replies) {
        this.replies = replies;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public long getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(long replyCount) {
        this.replyCount = replyCount;
    }

    public String getLastReplyBy() {
        return lastReplyBy;
    }

    public void setLastReplyBy(String lastReplyBy) {
        this.lastReplyBy = lastReplyBy;
    }

    public long getLastReplyTimestamp() {
        return lastReplyTimestamp;
    }

    public void setLastReplyTimestamp(long lastReplyTimestamp) {
        this.lastReplyTimestamp = lastReplyTimestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getUserRating() {
        return userRating;
    }

    public void setUserRating(int userRating) {
        this.userRating = userRating;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }
}
