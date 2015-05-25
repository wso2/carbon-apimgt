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

package org.wso2.carbon.forum;

import org.wso2.carbon.forum.dto.ForumPermissionDTO;
import org.wso2.carbon.forum.dto.ForumReplyDTO;
import org.wso2.carbon.forum.dto.ForumSearchDTO;
import org.wso2.carbon.forum.dto.ForumTopicDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ForumManager {

    /**
     *
     * @param forumTopicDTO
     * @throws ForumException
     */
    public String addTopic(ForumTopicDTO forumTopicDTO) throws ForumException;

    /**
     *
     * @param forumTopicDTO
     * @param username
     * @throws ForumException
     */
    public void updateTopic(ForumTopicDTO forumTopicDTO, String username) throws ForumException;

    /**
     *
     * @param topicId
     * @param username
     * @param tenantDomain
     * @throws ForumException
     */
    public void removeTopic(String topicId, String username, String tenantDomain) throws ForumException;

    /**
     *
     * @param start
     * @param count
     * @param tenantDomain
     * @param username
     * @return
     * @throws ForumException
     */
    public ForumSearchDTO<ForumTopicDTO> fetchForumTopics(int start, int count, String tenantDomain, String username) throws ForumException;

    /**
     * @param topicId
     * @param start
     * @param count
     * @param username
     * @param tenantDomain
     * @return
     * @throws ForumException
     */
    public ForumTopicDTO fetchForumTopicWithReplies(String topicId, int start, int count, String username, String tenantDomain)
                                                                                            throws ForumException;
    /**
     *
     * @param forumReplyDTO
     * @throws ForumException
     */
    public void addReply(ForumReplyDTO forumReplyDTO) throws ForumException;

    /**
     *
     * @param forumReplyDTO
     * @param username
     * @throws ForumException
     */
    public void updateReply(ForumReplyDTO forumReplyDTO, String username) throws ForumException;

    /**
     *
     * @param replyId
     * @param replyOwner
     * @param tenantDomain
     * @throws ForumException
     */
    public void removeReply(String replyId, String replyOwner, String tenantDomain) throws ForumException;

    /**
     *
     * @param topicResourceIdentifier
     * @param tenantDomain
     * @param allowedRoles
     * @param deniedRoles
     * @throws ForumException
     */
    public void applyPermissions(String topicResourceIdentifier, String tenantDomain,
                                 Set<ForumPermissionDTO> allowedRoles, Set<ForumPermissionDTO> deniedRoles) throws ForumException;

    /**
     *
     * @param start
     * @param count
     * @param searchString
     * @param user
     * @param tenantDomain
     * @return
     * @throws ForumException
     */
    public ForumSearchDTO<ForumTopicDTO> searchTopicsBySubject(int start, int count, String searchString, String user,
                                                     String tenantDomain) throws ForumException;
    
    /**
     * search the topics by search text for a given resourceid
     * @param start
     * @param count
     * @param searchString
     * @param resourceIdentifier
     * @param user
     * @param tenantDomain
     * @return
     * @throws ForumException
     */
    public ForumSearchDTO<ForumTopicDTO> searchTopicsBySubjectForResourceId(int start, int count, String searchString, 
                                                                          final String resourceIdentifier, String user, 
                                                                          String tenantDomain) throws ForumException;

    /**
     *
     * @param resourceIdentifier
     * @param user
     * @param tenantDomain
     * @return
     * @throws ForumException
     */
    public ForumSearchDTO<ForumTopicDTO> getTopicsByResourceId(int start, int count, final String resourceIdentifier,
                                                               String user, String tenantDomain) throws ForumException;

    /**
     * Rates the given topic and return the new rating.
     * Rates the given topic using registry rating,
     * @param topicId ID of the topic which should be rated.
     * @param rating User rate for the topic.
     * @param username Username
     * @param tenantDomain Tenant domain.
     * @return Average rating.
     * @throws ForumException When the topic cannot be rated.
     */
    public float rateTopic(String topicId, int rating, String username, String tenantDomain)throws ForumException;

    /**
     * Returns the user rating and the average rating of the given topic.
     * @param topicId Id of the topic which the ratings should be returned of.
     * @param username Username
     * @param tenantDomain Tenant domain.
     * @return User rating if the user is signed in, and average rating.
     * @throws ForumException When the ratings cannot be retrieved.
     */
    public Map<String, Object> getRating(String topicId, String username, String tenantDomain)throws ForumException;
}
