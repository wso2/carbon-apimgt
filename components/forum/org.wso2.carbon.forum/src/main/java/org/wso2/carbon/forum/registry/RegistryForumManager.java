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

package org.wso2.carbon.forum.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.forum.ForumException;
import org.wso2.carbon.forum.ForumManager;
import org.wso2.carbon.forum.ForumPermission;
import org.wso2.carbon.forum.ServiceReferenceHolder;
import org.wso2.carbon.forum.dto.ForumPermissionDTO;
import org.wso2.carbon.forum.dto.ForumReplyDTO;
import org.wso2.carbon.forum.dto.ForumSearchDTO;
import org.wso2.carbon.forum.dto.ForumTopicDTO;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RegistryForumManager implements ForumManager {

    private static final Log log = LogFactory.getLog(RegistryForumManager.class);

    private static final String TOPIC_RXT_KEY = "topic";

    private static final String REPLY_RXT_KEY = "reply";

    public static final String TOPICS_ROOT = "forumtopics";

    ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance().getInstance();

    @Override
    public String addTopic(ForumTopicDTO forumTopicDTO) throws ForumException {

        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(forumTopicDTO.getTopicOwner());

        Registry registry = null;
        int tenantId = 0;

        try {
            tenantId = serviceReferenceHolder.getRealmService().getTenantManager().getTenantId(forumTopicDTO.getTopicOwnerTenantDomain());
            registry = serviceReferenceHolder.getRegistryService().getGovernanceUserRegistry(tenantAwareUserName,tenantId);

        } catch (RegistryException e) {
            log.error("Could not get registry of user " + tenantAwareUserName + " of tenant (id) " + tenantId + " " + e.getMessage());
            throw new ForumException("Unable to get Registry of User", e);
        } catch (UserStoreException e) {
            log.error("Could not get tenant id from tenant domain " + e.getMessage());
            throw new ForumException("Could not get tenant id from tenant domain", e);
        }

        GenericArtifactManager artifactManager = getArtifactManager(registry,
                TOPIC_RXT_KEY);
        try {
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(forumTopicDTO.getSubject()));
            GenericArtifact artifact = createTopicArtifactContent(genericArtifact, forumTopicDTO);
            artifactManager.addGenericArtifact(artifact);

            Resource resource = registry.newResource();
            resource.setContent(forumTopicDTO.getDescription().getBytes());
            String resourcePath = TOPICS_ROOT +
                    RegistryConstants.PATH_SEPARATOR + artifact.getAttribute(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER) +
                    RegistryConstants.PATH_SEPARATOR + artifact.getId() +
                    RegistryConstants.PATH_SEPARATOR + "topic_content";
            registry.put(resourcePath, resource);
            return artifact.getId();
        } catch (GovernanceException e) {
            log.error("Error while creating Governance Artifact " + e.getMessage());
            throw new ForumException("Error while creating Governance Artifact ", e);
        } catch (RegistryException e) {
            log.error("Error trying to start/end Registry Transaction " + e.getMessage());
            throw new ForumException("Error trying to start/end Registry Transaction", e);
        }
    }

    @Override
    public void updateTopic(ForumTopicDTO forumTopicDTO, String username) throws ForumException {

        String tenantAwareRegistryOwner = MultitenantUtils.getTenantAwareUsername(username);

        Registry registry = null;
        int tenantId = 0;

        try {
            tenantId = serviceReferenceHolder.getRealmService().getTenantManager().getTenantId(forumTopicDTO.getTopicOwnerTenantDomain());
            registry = serviceReferenceHolder.getRegistryService().getGovernanceUserRegistry(tenantAwareRegistryOwner, tenantId);
        } catch (UserStoreException e) {
            log.error("Could not get tenant id from tenant domain " + e.getMessage());
            throw new ForumException("Could not get tenant id from tenant domain", e);
        } catch (RegistryException e) {
            log.error("Could not get registry of user " + tenantAwareRegistryOwner + " of tenant (id) " +
                    tenantId + " " + e.getMessage());
            throw new ForumException("Unable to get Registry of User", e);
        }

        GenericArtifactManager artifactManager = getArtifactManager(registry,
                TOPIC_RXT_KEY);
        try {
            GenericArtifact genericArtifact = artifactManager.getGenericArtifact(forumTopicDTO.getTopicId());

            // Only owner of the topic can update the topic.
            if(!isOwnerOfTopic(username, genericArtifact)){
                throw new ForumException(String.format("'%s' is not the owner of this topic.", username));
            }

            forumTopicDTO.setTopicResourceIdentifier(genericArtifact.getAttribute(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER));

            GenericArtifact artifact = createTopicArtifactContent(genericArtifact, forumTopicDTO);
            artifactManager.updateGenericArtifact(artifact);

            String resourcePath = TOPICS_ROOT +
                    RegistryConstants.PATH_SEPARATOR + forumTopicDTO.getTopicResourceIdentifier() +
                    RegistryConstants.PATH_SEPARATOR + artifact.getId() +
                    RegistryConstants.PATH_SEPARATOR + "topic_content";
            Resource resource = registry.get(resourcePath);
            resource.setContent(forumTopicDTO.getDescription().getBytes());
            registry.put(resourcePath, resource);

        } catch (GovernanceException e) {
            log.error("Error while creating Governance Artifact " + e.getMessage());
            throw new ForumException("Error while creating Governance Artifact ", e);
        } catch (RegistryException e) {
            log.error("Error trying to start/end Registry Transaction " + e.getMessage());
            throw new ForumException("Error trying to start/end Registry Transaction", e);
        }
    }

    @Override
    public void removeTopic(String topicId, String username, String tenantDomain) throws ForumException {

        //We set the username to null since we need the GovernanceSystemRegistry for removing artifacts.
        Registry registry = getRegistry(null, tenantDomain);

        GenericArtifactManager artifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);
        try {
            GenericArtifact genericArtifact = artifactManager.getGenericArtifact(topicId);

            if(!isOwnerOfTopic(username, genericArtifact)){
                throw new ForumException(String.format("'%s' is not the owner of this topic.", username));
            }

            String resourceIdentifier = genericArtifact.getAttribute(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER);
            String resourcePath = TOPICS_ROOT +
                                  RegistryConstants.PATH_SEPARATOR + resourceIdentifier +
                                  RegistryConstants.PATH_SEPARATOR + topicId;

            registry.delete(resourcePath);
        } catch (GovernanceException e) {
            log.error("Error while removing Governance Artifact " + e.getMessage());
            throw new ForumException("Error while removing Governance Artifact ", e);
        } catch (RegistryException e) {
            log.error("Error getting registry resource " + e.getMessage());
            throw new ForumException("Error getting registry resource ", e);
        }
    }

    @Override
    public ForumSearchDTO<ForumTopicDTO> fetchForumTopics(int start, int count, String tenantDomain, String username) throws ForumException {

        Registry registry = getRegistry(username, tenantDomain);

        GenericArtifactManager artifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);

        if(artifactManager == null){
            if(log.isDebugEnabled()){
                log.debug("Could not get artifact manager for topic.rxt, probably no topics found");
            }
            return null;
        }

        PaginationContext.init(start, count, "DESC", ForumConstants.OVERVIEW_TOPIC_TIMESTAMP, Integer.MAX_VALUE);

        try {

            Map<String, List<String>> listMap = new HashMap<String, List<String>>();
            listMap.put(ForumConstants.OVERVIEW_SUBJECT, new ArrayList<String>() {{
                add("*");
            }});

            //GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
            GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);

            if(genericArtifacts == null || genericArtifacts.length == 0){
                if(log.isDebugEnabled()){
                    log.debug("No Forum Topics Found");
                }
                return null;
            }

            ForumSearchDTO forumSearchDTO = new ForumSearchDTO();
            List<ForumTopicDTO> topics = new ArrayList<ForumTopicDTO>();
            ForumTopicDTO forumTopicDTO = null;
            for(GenericArtifact artifact : genericArtifacts){
                forumTopicDTO = createForumTopicDTOFromArtifact(artifact, registry);
                topics.add(forumTopicDTO);
            }
            forumSearchDTO.setPaginatedResults(topics);
            forumSearchDTO.setTotalResultCount(PaginationContext.getInstance().getLength());
            return forumSearchDTO;
        } catch (GovernanceException e) {
            log.error("Error finding forum topics " + e.getMessage());
            throw new ForumException("Error finding forum topics", e);
        } finally {
            PaginationContext.destroy();
        }
    }

    @Override
    public ForumTopicDTO fetchForumTopicWithReplies(String topicId, int start, int count, String username, String tenantDomain)
            throws ForumException{

        Registry registry = getRegistry(username, tenantDomain);

        GenericArtifactManager topicArtifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);
        GenericArtifactManager replyArtifactManager = getArtifactManager(registry, REPLY_RXT_KEY);

        if(topicArtifactManager == null){
            if(log.isDebugEnabled()){
                log.debug("Could not get artifact manager for topic.rxt, probably no topics found");
            }
            return null;
        }

        final String replyTopicId = topicId;

        Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        listMap.put(ForumConstants.OVERVIEW_REPLY_TOPIC_ID, new ArrayList<String>() {{
            add(replyTopicId);
        }});

        ForumTopicDTO topicDTO = null;
        PaginationContext.init(start, count, "DESC", ForumConstants.OVERVIEW_REPLY_TIMESTAMP, Integer.MAX_VALUE);

        try {
            GenericArtifact topicArtifact = topicArtifactManager.getGenericArtifact(topicId);

            if(topicArtifact == null){
                log.info("Could not find topic with id " + topicId);
                return null;
            }

            topicDTO = createForumTopicDTOFromArtifact(topicArtifact, registry);

            // Set ratings of the topic.
            // NOTE : Taking this operation out from 'createForumTopicDTOFromArtifact' for performance's sake
            topicDTO.setUserRating(registry.getRating(topicArtifact.getPath(), username));
            topicDTO.setAverageRating(registry.getAverageRating(topicArtifact.getPath()));

            final String searchValue = topicId;

            GenericArtifact[] replyArtifacts = replyArtifactManager.findGenericArtifacts(listMap);

            if (replyArtifacts == null || replyArtifacts.length == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("No Replies Found for topic " + topicDTO.getSubject());
                }
                return topicDTO;
            }

            List<ForumReplyDTO> replies = new ArrayList<ForumReplyDTO>();
            for(GenericArtifact replyArtifact : replyArtifacts){
                ForumReplyDTO replyDTO = createReplyDtoFromArtifact(replyArtifact, registry);
                replies.add(replyDTO);
            }
            topicDTO.setReplies(replies);
            return topicDTO;
        } catch (RegistryException e) {
            log.error("Error while getting artifacts for topic " + topicId + " " + e.getMessage());
            throw new ForumException("Error while getting artifacts for topic " + topicId);
        } finally {
            PaginationContext.destroy();
        }
    }

    @Override
    public void addReply(ForumReplyDTO forumReplyDTO) throws ForumException {

        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(forumReplyDTO.getCreatedBy());

        Registry registry = null;
        int tenantId = 0;

        try {
            tenantId = serviceReferenceHolder.getRealmService().getTenantManager().getTenantId(
                                            forumReplyDTO.getCreatorTenantDomain());
            registry = serviceReferenceHolder.getRegistryService().getGovernanceUserRegistry(tenantAwareUserName,tenantId);
        } catch (UserStoreException e) {
            log.error("Could not get tenant id from tenant domain " + e.getMessage());
            throw new ForumException("Could not get tenant id from tenant domain", e);
        } catch (RegistryException e) {
            log.error("Could not get registry of user " + tenantAwareUserName + " of tenant (id) " +
                    tenantId + " " + e.getMessage());
            throw new ForumException("Unable to get Registry of User", e);
        }

        GenericArtifactManager replyArtifactManager = getArtifactManager(registry, REPLY_RXT_KEY);
        GenericArtifactManager topicArtifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);

        try {
            GenericArtifact genericArtifact =
                    replyArtifactManager.newGovernanceArtifact(new QName(forumReplyDTO.getCreatedBy()));
            GenericArtifact artifact = createReplyArtifactContent(genericArtifact, forumReplyDTO);
            replyArtifactManager.addGenericArtifact(artifact);

            Resource resource = registry.newResource();
            resource.setContent(forumReplyDTO.getReply().getBytes());
            String resourcePath = TOPICS_ROOT +
                    RegistryConstants.PATH_SEPARATOR + forumReplyDTO.getTopicResourceIdentifier() +
                    RegistryConstants.PATH_SEPARATOR + forumReplyDTO.getTopicId() +
                    RegistryConstants.PATH_SEPARATOR + "rep_content_" + artifact.getId();
            registry.put(resourcePath, resource);

            GenericArtifact topicArtifact = topicArtifactManager.getGenericArtifact(forumReplyDTO.getTopicId());

            if(topicArtifact != null){
                String replyCount = topicArtifact.getAttribute(ForumConstants.OVERVIEW_REPLY_COUNT);
                if(replyCount == null || replyCount.length() == 0){
                    replyCount = "1";
                }
                topicArtifact.setAttribute(ForumConstants.OVERVIEW_REPLY_COUNT, String.valueOf(Long.parseLong(replyCount) + 1));

                //Update the reply count for the topic
                topicArtifactManager.updateGenericArtifact(topicArtifact);
            }
            else{
                log.warn("Could not find Topic with ID " + forumReplyDTO.getTopicId() + ". Cannot update reply counts.");
            }


        } catch (GovernanceException e) {
            log.error("Error while adding reply to registry " + e.getMessage());
            throw new ForumException("Error while adding reply to registry ", e);
        } catch (RegistryException e) {
            log.error("Error trying to start/end Registry Transaction " + e.getMessage());
            throw new ForumException("Error trying to start/end Registry Transaction", e);
        }
    }

    @Override
    public void updateReply(ForumReplyDTO forumReplyDTO, String username) throws ForumException {

        String tenantAwareRegistryOwner = MultitenantUtils.getTenantAwareUsername(username);

        Registry registry = null;
        int tenantId = 0;

        try {
            tenantId = serviceReferenceHolder.getRealmService().getTenantManager().getTenantId(
                    forumReplyDTO.getCreatorTenantDomain());
            registry = serviceReferenceHolder.getRegistryService().getGovernanceUserRegistry(tenantAwareRegistryOwner, tenantId);
        } catch (UserStoreException e) {
            log.error("Could not get tenant id from tenant domain " + e.getMessage());
            throw new ForumException("Could not get tenant id from tenant domain", e);
        } catch (RegistryException e) {
            log.error("Could not get registry of user " + tenantAwareRegistryOwner + " of tenant (id) " +
                    tenantId + " " + e.getMessage());
            throw new ForumException("Unable to get Registry of User", e);
        }

        GenericArtifactManager artifactManager = getArtifactManager(registry, REPLY_RXT_KEY);
        try {
            GenericArtifact genericArtifact = artifactManager.getGenericArtifact(forumReplyDTO.getReplyId());

            if(!isOwnerOfReply(username, genericArtifact)){
                throw new ForumException(String.format("'%s' is not the owner of this reply.", username));
            }

            GenericArtifact artifact = createReplyArtifactContent(genericArtifact, forumReplyDTO);
            artifactManager.updateGenericArtifact(artifact);

            String resourcePath = TOPICS_ROOT +
                    RegistryConstants.PATH_SEPARATOR + forumReplyDTO.getTopicResourceIdentifier() +
                    RegistryConstants.PATH_SEPARATOR + forumReplyDTO.getTopicId() +
                    RegistryConstants.PATH_SEPARATOR + "rep_content_" + artifact.getId();
            Resource resource = registry.get(resourcePath);
            resource.setContent(forumReplyDTO.getReply().getBytes());
            registry.put(resourcePath, resource);

        } catch (GovernanceException e) {
            log.error("Error while creating Governance Artifact " + e.getMessage());
            throw new ForumException("Error while creating Governance Artifact ", e);
        } catch (RegistryException e) {
            log.error("Error while updating reply content " + e.getMessage());
            throw new ForumException("Error while updating reply content ", e);
        }
    }

    @Override
    public void removeReply(String replyId, String username, String tenantDomain) throws ForumException {

        String tenantAwareRegistryOwner = MultitenantUtils.getTenantAwareUsername(username);

        Registry registry = null;
        int tenantId = 0;

        try {
            tenantId = serviceReferenceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            registry = serviceReferenceHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
        }catch (UserStoreException e) {
            log.error("Could not get tenant id from tenant domain " + e.getMessage());
            throw new ForumException("Could not get tenant id from tenant domain", e);
        }catch (RegistryException e) {
            log.error("Could not get registry of user " + tenantAwareRegistryOwner + " of tenant (id) " +
                    tenantId + " " + e.getMessage());
            throw new ForumException("Unable to get Registry of User", e);
        }

        GenericArtifactManager artifactManager = getArtifactManager(registry, REPLY_RXT_KEY);
        GenericArtifactManager topicArtifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);

        try {
            GenericArtifact replyArtifact = artifactManager.getGenericArtifact(replyId);

            if(!isOwnerOfReply(username, replyArtifact)){
                throw new ForumException(String.format("'%s' is not the owner of this reply.", username));
            }

            String topicId = null;

            if(replyArtifact != null){
                topicId = replyArtifact.getAttribute(ForumConstants.OVERVIEW_REPLY_TOPIC_ID);
                artifactManager.removeGenericArtifact(replyId);

                String resourcePath = TOPICS_ROOT +
                        RegistryConstants.PATH_SEPARATOR + replyArtifact.getAttribute(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER) +
                        RegistryConstants.PATH_SEPARATOR + topicId +
                        RegistryConstants.PATH_SEPARATOR + "rep_content_" + replyId;
                registry.delete(resourcePath);
            }
            else{
                log.error("Could not find reply with id " + replyId);
                throw new ForumException("Could not find reply with id " + replyId);
            }


            GenericArtifact topicArtifact = topicArtifactManager.getGenericArtifact(topicId);

            String replyCount = topicArtifact.getAttribute(ForumConstants.OVERVIEW_REPLY_COUNT);

            topicArtifact.setAttribute(ForumConstants.OVERVIEW_REPLY_COUNT, String.valueOf(Long.parseLong(replyCount) - 1));

            //Update the reply count for the topic
            topicArtifactManager.updateGenericArtifact(topicArtifact);
        } catch (GovernanceException e) {
            log.error("Error while removing Governance Artifact " + e.getMessage());
            throw new ForumException("Error while removing Governance Artifact ", e);
        } catch (RegistryException e) {
            log.error("Error getting registry resource " + e.getMessage());
            throw new ForumException("Error getting registry resource ", e);
        }
    }

    @Override
    public void applyPermissions(String resourceIdentifier, String tenantDomain,
                                 Set<ForumPermissionDTO> allowedRoles, Set<ForumPermissionDTO> deniedRoles) throws ForumException{

        Registry registry = null;
        GenericArtifactManager artifactManager;
        AuthorizationManager authorizationManager;

        //If super tenant
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            try {
                registry = serviceReferenceHolder.getRegistryService().getGovernanceUserRegistry();

                authorizationManager = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getAuthorizationManager();
            } catch (RegistryException e) {
                log.error("Could not get registry of super tenant ");
                throw new ForumException("Unable to get Registry of Super Tenant", e);
            } catch (UserStoreException e) {
                log.error("Could not get Authorization Manager of Super Tenant" + e.getMessage());
                throw new ForumException("Could not get Authorization Manager of Super Tenant", e);
            }
        }
        else{
            int tenantId = 0;
            try {
                tenantId = serviceReferenceHolder.getRealmService().
                        getTenantManager().getTenantId(tenantDomain);

                registry = serviceReferenceHolder.getRegistryService().getGovernanceUserRegistry(
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);

                authorizationManager = new RegistryAuthorizationManager
                        (serviceReferenceHolder.getRegistryService().getConfigSystemRegistry().getUserRealm());

            } catch (RegistryException e) {
                log.error("Could not get registry of tenant " + tenantDomain);
                throw new ForumException("Unable to get Registry of Tenant " + tenantDomain, e);
            } catch (UserStoreException e) {
                log.error("Could not get Tenant ID of Tenant " + tenantDomain + e.getMessage());
                throw new ForumException("Could not get Tenant ID of Tenant " + tenantDomain, e);
            }
        }

        artifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);

        try {
            String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                            RegistryConstants.PATH_SEPARATOR + TOPICS_ROOT +
                            RegistryConstants.PATH_SEPARATOR + resourceIdentifier
            );

            // Apply 'allow' rules.
            if(allowedRoles != null && !allowedRoles.isEmpty()){
                for(ForumPermissionDTO permissionDTO : allowedRoles){

                    for(ForumPermission permission : permissionDTO.getPermissions()){
                        String regPermission = ForumRegistryPermission.valueOf(permission.toString()).getRegistryPermission();
                        authorizationManager.authorizeRole(permissionDTO.getRole(), resourcePath, regPermission);
                    }
                }
            }

            // Apply 'deny' rules.
            if(deniedRoles != null && !deniedRoles.isEmpty()){
                for(ForumPermissionDTO permissionDTO : deniedRoles){

                    for(ForumPermission permission : permissionDTO.getPermissions()){
                        String regPermission = ForumRegistryPermission.valueOf(permission.toString()).getRegistryPermission();
                        authorizationManager.denyRole(permissionDTO.getRole(), resourcePath, regPermission);
                    }
                }
            }

        }catch (UserStoreException e) {
            log.error("Error while authorizing role for resource " + e.getMessage());
            throw new ForumException("Error while authorizing role for resource ", e);
        }
    }

    @Override
    public ForumSearchDTO<ForumTopicDTO> searchTopicsBySubject(int start, int count, String searchString, String user, String tenantDomain) throws ForumException{

        //final String regex = "(?i)[\\w.|-]*" + searchString.trim() + "[\\w.|-]*";
        final String regex = "*" + searchString.trim() + "*";
        //final Pattern pattern = Pattern.compile(regex);

        Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        listMap.put(ForumConstants.OVERVIEW_SUBJECT, new ArrayList<String>() {{
            add(regex);
        }});

        Registry registry = getRegistry(user, tenantDomain);

//        try {
//            GovernanceUtils.loadGovernanceArtifacts((UserRegistry)registry);
//        } catch (RegistryException e) {
//            e.printStackTrace();
//        }

        PaginationContext.init(start, count, "DESC", ForumConstants.OVERVIEW_TOPIC_TIMESTAMP, Integer.MAX_VALUE);

        GenericArtifactManager artifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);

        if(artifactManager == null){
            if(log.isDebugEnabled()){
                log.debug("Could not get artifact manager for topic.rxt, probably no topics found");
            }
            return null;
        }

        try {
           /* GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(new GenericArtifactFilter() {
                @Override
                public boolean matches(GenericArtifact artifact) throws GovernanceException {
                    Matcher matcher = pattern.matcher(artifact.getAttribute(ForumConstants.OVERVIEW_SUBJECT));
                    return matcher.find();
                }
            });*/

            GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
            if(genericArtifacts == null || genericArtifacts.length == 0){
                if(log.isDebugEnabled()){
                    log.debug("No Forum Topics Found");
                }
                return null;
            }

            List<ForumTopicDTO> topics = new ArrayList<ForumTopicDTO>();
            ForumTopicDTO forumTopicDTO = null;
            for(GenericArtifact artifact : genericArtifacts){
                forumTopicDTO = createForumTopicDTOFromArtifact(artifact, registry);
                topics.add(forumTopicDTO);
            }

            ForumSearchDTO<ForumTopicDTO> forumSearchDTO = new ForumSearchDTO<ForumTopicDTO>();
            forumSearchDTO.setPaginatedResults(topics);
            forumSearchDTO.setTotalResultCount(PaginationContext.getInstance().getLength());

            return forumSearchDTO;
        } catch (GovernanceException e) {
            log.error("Error finding forum topics " + e.getMessage());
            throw new ForumException("Error finding forum topics", e);
        } finally {
            PaginationContext.destroy();
        }
    }
    
    @Override
    public ForumSearchDTO<ForumTopicDTO> searchTopicsBySubjectForResourceId(int start, int count, String searchString,
                                                                            final String resourceIdentifier,
                                                                            String user, String tenantDomain)
                                                                                                throws ForumException {

        final String regex = "*" + searchString.trim() + "*";

        Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        listMap.put(ForumConstants.OVERVIEW_SUBJECT, new ArrayList<String>() {
            {
                add(regex);
            }
        });
        listMap.put(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER, new ArrayList<String>() {
            {
                add(resourceIdentifier.replace("@", "-AT-"));
            }
        });

        Registry registry = getRegistry(user, tenantDomain);

        PaginationContext.init(start, count, "DESC", ForumConstants.OVERVIEW_TOPIC_TIMESTAMP, Integer.MAX_VALUE);

        GenericArtifactManager artifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);

        if (artifactManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Could not get artifact manager for topic.rxt, probably no topics found");
            }
            return null;
        }

        try {
            
            GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
            if (genericArtifacts == null || genericArtifacts.length == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("No Forum Topics Found");
                }
                return null;
            }

            List<ForumTopicDTO> topics = new ArrayList<ForumTopicDTO>();
            ForumTopicDTO forumTopicDTO = null;
            for (GenericArtifact artifact : genericArtifacts) {
                forumTopicDTO = createForumTopicDTOFromArtifact(artifact, registry);
                topics.add(forumTopicDTO);
            }

            ForumSearchDTO<ForumTopicDTO> forumSearchDTO = new ForumSearchDTO<ForumTopicDTO>();
            forumSearchDTO.setPaginatedResults(topics);
            forumSearchDTO.setTotalResultCount(PaginationContext.getInstance().getLength());

            return forumSearchDTO;
        } catch (GovernanceException e) {
            log.error("Error finding forum topics " + e.getMessage());
            throw new ForumException("Error finding forum topics", e);
        } finally {
            PaginationContext.destroy();
        }
    }


    public ForumSearchDTO<ForumTopicDTO> getTopicsByResourceId(int start, int count, final String resourceIdentifier,
                                                               String user, String tenantDomain) throws ForumException{

        Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        listMap.put(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER, new ArrayList<String>() {{
            add(resourceIdentifier.replace("@", "-AT-").replace(":","%3A"));
        }});

        Registry registry = getRegistry(user, tenantDomain);

        PaginationContext.init(start, count, "DESC", ForumConstants.OVERVIEW_TOPIC_TIMESTAMP, Integer.MAX_VALUE);

        GenericArtifactManager artifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);

        if(artifactManager == null){
            if(log.isDebugEnabled()){
                log.debug("Could not get artifact manager for topic.rxt, probably no topics found");
            }
            return null;
        }

        try {
            GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
            if(genericArtifacts == null || genericArtifacts.length == 0){
                if(log.isDebugEnabled()){
                    log.debug("No Forum Topics Found for identifier " + resourceIdentifier);
                }
                return null;
            }

            List<ForumTopicDTO> topics = new ArrayList<ForumTopicDTO>();
            ForumTopicDTO forumTopicDTO = null;
            for(GenericArtifact artifact : genericArtifacts){
                forumTopicDTO = createForumTopicDTOFromArtifact(artifact, registry);
                topics.add(forumTopicDTO);
            }

            ForumSearchDTO<ForumTopicDTO> forumSearchDTO = new ForumSearchDTO<ForumTopicDTO>();
            forumSearchDTO.setPaginatedResults(topics);
            forumSearchDTO.setTotalResultCount(PaginationContext.getInstance().getLength());

            return forumSearchDTO;
        } catch (GovernanceException e) {
            log.error("Error finding forum topics " + e.getMessage());
            throw new ForumException("Error finding forum topics", e);
        } finally {
            PaginationContext.destroy();
        }
    }

    /**
     * Rates the given topic using registry rating,
     * @param topicId ID of the topic which should be rated.
     * @param rating User rate for the topic.
     * @param username Username
     * @param tenantDomain Tenant domain.
     * @return  Average rating.
     * @throws ForumException When the topic cannot be rated.
     */
    @Override
    public float rateTopic(String topicId, int rating, String username, String tenantDomain)throws ForumException {
        Registry registry;

        try {
            registry = getRegistry(username, tenantDomain);
            GenericArtifactManager artifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);
            GenericArtifact genericArtifact = artifactManager.getGenericArtifact(topicId);
            registry.rateResource(genericArtifact.getPath(), rating);
            return registry.getAverageRating(genericArtifact.getPath());
        } catch (RegistryException e) {
            throw new ForumException("Unable to get Registry of User", e);
        }
    }

    /**
     * Return the registry resources ratings for the given topic resource.
     * @param topicId Id of the topic which the ratings should be returned of.
     * @param username Username
     * @param tenantDomain Tenant domain.
     * @return User rating if the user is signed in, and average rating.
     * @throws ForumException
     */
    @Override
    public Map<String, Object> getRating(String topicId, String username, String tenantDomain) throws ForumException {

        Map<String, Object> rating = new HashMap<String, Object>();

        try {
            Registry registry = getRegistry(username, tenantDomain);
            GenericArtifactManager artifactManager = getArtifactManager(registry, TOPIC_RXT_KEY);
            GenericArtifact genericArtifact = artifactManager.getGenericArtifact(topicId);

            // Get the user rating.
            if(username != null){
                rating.put("userRating", registry.getRating(genericArtifact.getPath(), username));
            }

            // Get average rating.
            rating.put("averageRating", registry.getAverageRating(genericArtifact.getPath()));

            return rating;

        } catch (RegistryException e) {
            throw new ForumException(String.format("Cannot get rating for the topic id : '%s'", topicId), e);
        }

    }


    private static GenericArtifactManager getArtifactManager(Registry registry, String key)
            throws ForumException {
        GenericArtifactManager artifactManager = null;

        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            if(GovernanceUtils.findGovernanceArtifactConfiguration(key, registry)!=null){
                artifactManager = new GenericArtifactManager(registry, key);
            }
        } catch (RegistryException e) {
            String msg = "Failed to initialize GenericArtifactManager";
            log.error(msg, e);
            throw new ForumException(msg, e);
        }
        return artifactManager;
    }

    private static GenericArtifact createTopicArtifactContent(GenericArtifact artifact, ForumTopicDTO forumTopicDTO)
                                                                                                throws ForumException {

        try {
            artifact.setAttribute(ForumConstants.OVERVIEW_TOPIC_ID, artifact.getId());
            artifact.setAttribute(ForumConstants.OVERVIEW_SUBJECT, forumTopicDTO.getSubject());
            artifact.setAttribute(ForumConstants.OVERVIEW_TOPIC_OWNER_TENANT_DOMAIN,
                                  forumTopicDTO.getTopicOwnerTenantDomain());
            artifact.setAttribute(ForumConstants.OVERVIEW_TOPIC_OWNER, forumTopicDTO.getTopicOwner());

            String location = forumTopicDTO.getTopicResourceIdentifier();

            if(location == null){
                log.error("Resource Identifier not set. Please provide a value for the Resource Identifier");
                throw new ForumException("Resource Identifier not set. Please provide a value for the Resource Identifier");
            }
            location = location.replaceAll("@", "-AT-");
            artifact.setAttribute(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER, location);
            DateFormat df = new SimpleDateFormat(ForumConstants.FORUM_DATE_FORMAT);
            artifact.setAttribute(ForumConstants.OVERVIEW_CREATED_DATE, df.format(forumTopicDTO.getCreatedDate()));

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(forumTopicDTO.getTimestamp());
            df = new SimpleDateFormat(ForumConstants.FORUM_DATE_TIME_FORMAT);
            artifact.setAttribute(ForumConstants.OVERVIEW_TOPIC_TIMESTAMP, df.format(calendar.getTime()));

            return artifact;
        } catch (GovernanceException e) {
            log.error("Could not create Generic Artifact from DTO " + e.getMessage());
            throw new ForumException("Could not create Generic Artifact from DTO", e);
        }
    }

    private static ForumTopicDTO createForumTopicDTOFromArtifact(GenericArtifact artifact, Registry registry)
            throws ForumException {

        ForumTopicDTO forumTopicDTO = new ForumTopicDTO();

        try {
            forumTopicDTO.setTopicId(artifact.getAttribute(ForumConstants.OVERVIEW_TOPIC_ID));
            forumTopicDTO.setSubject(artifact.getAttribute(ForumConstants.OVERVIEW_SUBJECT));
            forumTopicDTO.setTopicOwnerTenantDomain(artifact.getAttribute(ForumConstants.OVERVIEW_TOPIC_OWNER_TENANT_DOMAIN));
            forumTopicDTO.setTopicOwner(artifact.getAttribute(ForumConstants.OVERVIEW_TOPIC_OWNER));

            String location = artifact.getAttribute(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER);
            location = location.replaceAll("-AT-", "@");
            forumTopicDTO.setTopicResourceIdentifier(location);
            try {
                forumTopicDTO.setCreatedDate(new SimpleDateFormat(ForumConstants.FORUM_DATE_FORMAT).
                                             parse(artifact.getAttribute(ForumConstants.OVERVIEW_CREATED_DATE)));
            } catch (ParseException e) {
                log.error("Could not parse String to date " + e.getMessage());
                throw new ForumException("Could not parse String to date ", e);
            }

            String replyCount = artifact.getAttribute(ForumConstants.OVERVIEW_REPLY_COUNT);
            if(replyCount == null){
                replyCount = "0";
            }
            forumTopicDTO.setReplyCount(Long.parseLong(replyCount));

            String resourcePath = TOPICS_ROOT +
                    RegistryConstants.PATH_SEPARATOR + artifact.getAttribute(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER) +
                    RegistryConstants.PATH_SEPARATOR + artifact.getId() +
                    RegistryConstants.PATH_SEPARATOR + "topic_content";
            Resource resource = registry.get(resourcePath);
            if(resource != null){
                byte[] content = (byte[])resource.getContent();
                forumTopicDTO.setDescription(new String(content));
            }
            else{
                log.warn("Could not load topic description");
            }

            return forumTopicDTO;
        } catch (GovernanceException e) {
            log.error("Could not create Generic Artifact from DTO " + e.getMessage());
            throw new ForumException("Could not create Generic Artifact from DTO", e);
        } catch (RegistryException e) {
            log.error("Could not fetch topic content from registry resource " + e.getMessage());
            throw new ForumException("Could not fetch topic content from registry resource ", e);
        }
    }

    private static GenericArtifact createReplyArtifactContent(GenericArtifact artifact, ForumReplyDTO forumReplyDTO)
            throws ForumException {

        try {
            artifact.setAttribute(ForumConstants.OVERVIEW_REPLY_ID, artifact.getId());
            artifact.setAttribute(ForumConstants.OVERVIEW_REPLY_TOPIC_ID, forumReplyDTO.getTopicId());
            artifact.setAttribute(ForumConstants.OVERVIEW_CREATOR_TENANT_DOMAIN, forumReplyDTO.getCreatorTenantDomain());
            artifact.setAttribute(ForumConstants.OVERVIEW_CREATED_BY, forumReplyDTO.getCreatedBy());
            artifact.setAttribute(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER, forumReplyDTO.getTopicResourceIdentifier());
            DateFormat df = new SimpleDateFormat(ForumConstants.FORUM_DATE_FORMAT);
            artifact.setAttribute(ForumConstants.OVERVIEW_CREATED_DATE, df.format(forumReplyDTO.getCreatedDate()));

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(forumReplyDTO.getTimestamp());
            df = new SimpleDateFormat(ForumConstants.FORUM_DATE_TIME_FORMAT);
            artifact.setAttribute(ForumConstants.OVERVIEW_REPLY_TIMESTAMP, df.format(calendar.getTime()));
            return artifact;
        } catch (GovernanceException e) {
            log.error("Could not create Forum Reply Generic Artifact from DTO " + e.getMessage());
            throw new ForumException("Could not create Forum Reply Generic Artifact from DTO", e);
        }
    }

    private static ForumReplyDTO createReplyDtoFromArtifact(GenericArtifact artifact, Registry registry) throws ForumException {

        ForumReplyDTO replyDTO = new ForumReplyDTO();

        try {
            replyDTO.setReplyId(artifact.getAttribute(ForumConstants.OVERVIEW_REPLY_ID));
            replyDTO.setTopicId(artifact.getAttribute(ForumConstants.OVERVIEW_REPLY_TOPIC_ID));
            replyDTO.setCreatorTenantDomain(artifact.getAttribute(ForumConstants.OVERVIEW_CREATOR_TENANT_DOMAIN));
            replyDTO.setCreatedBy(artifact.getAttribute(ForumConstants.OVERVIEW_CREATED_BY));
            replyDTO.setTopicResourceIdentifier(artifact.getAttribute(ForumConstants.OVERVIEW_RESOURCE_IDENTIFIER));
            DateFormat df = new SimpleDateFormat(ForumConstants.FORUM_DATE_FORMAT);
            try {
                replyDTO.setCreatedDate(df.parse(artifact.getAttribute(ForumConstants.OVERVIEW_CREATED_DATE)));
            } catch (ParseException e) {
                log.error("Could not parse String to date " + e.getMessage());
                throw new ForumException("Could not parse String to date ", e);
            }

            df = new SimpleDateFormat(ForumConstants.FORUM_DATE_TIME_FORMAT);
            Date date = df.parse(artifact.getAttribute(ForumConstants.OVERVIEW_REPLY_TIMESTAMP));
            replyDTO.setTimestamp(date.getTime());

            String resourcePath = TOPICS_ROOT +
                    RegistryConstants.PATH_SEPARATOR + replyDTO.getTopicResourceIdentifier() +
                    RegistryConstants.PATH_SEPARATOR + replyDTO.getTopicId() +
                    RegistryConstants.PATH_SEPARATOR + "rep_content_" + replyDTO.getReplyId();
            Resource resource = registry.get(resourcePath);
            if(resource != null){
                byte[] content = (byte[])resource.getContent();
                replyDTO.setReply(new String(content));
            }
            else{
                log.warn("Could not load reply content");
            }

            return replyDTO;
        } catch (GovernanceException e) {
            log.error("Could not create Forum Reply Generic Artifact from DTO " + e.getMessage());
            throw new ForumException("Could not create Forum Reply Generic Artifact from DTO", e);
        } catch (ParseException e) {
            log.error("Could not parse string value to date " + e.getMessage());
            throw new ForumException("Could not parse string value to date ", e);
        } catch (RegistryException e) {
            log.error("Could not fetch reply content from registry resource " + e.getMessage());
            throw new ForumException("Could not fetch reply content from registry resource ", e);
        }

    }

    /**
     * Get the registry belonging to the user
     * @param username - user
     * @param tenantDomain - tenant domain of user
     * @return - GovernanceSystemRegistry if username is null, GovernanceUserRegistry otherwise.
     * @throws ForumException
     */
    private Registry getRegistry(String username, String tenantDomain) throws ForumException{
        Registry registry = null;
        int tenantId = 0;

        try {
            tenantId = serviceReferenceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            if(username != null){
                username = MultitenantUtils.getTenantAwareUsername(username);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
                return serviceReferenceHolder.getRegistryService().getGovernanceUserRegistry(username, tenantId);
            }
            else{
                username = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
                return serviceReferenceHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            }

        } catch (UserStoreException e) {
            log.error("Could not get tenant id from tenant domain " + tenantDomain + e.getMessage());
            throw new ForumException("Could not get tenant id from tenant domain " + tenantDomain, e);
        } catch (RegistryException e) {
            log.error("Could not get registry of user " + username + " of tenant (id) " +
                    tenantId + " " + e.getMessage());
            throw new ForumException("Unable to get Registry of User", e);
        }
    }

    private boolean isOwnerOfTopic(String username, GenericArtifact artifact){

        try {
            String owner = artifact.getAttribute(ForumConstants.OVERVIEW_TOPIC_OWNER);
            if(username == null){
                return false;
            }else{
                return username.equals(owner);
            }
        } catch (GovernanceException e) {
            log.error("Could not get the 'overview_owner' attribute of the artifact.", e);
            return false;
        }

    }

    private boolean isOwnerOfReply(String username, GenericArtifact artifact){

        try {
            String owner = artifact.getAttribute(ForumConstants.OVERVIEW_CREATED_BY);
            if(username == null){
                return false;
            }else{
                return username.equals(owner);
            }
        } catch (GovernanceException e) {
            log.error("Could not get the 'overview_createdBy' attribute of the artifact.", e);
            return false;
        }

    }



}
