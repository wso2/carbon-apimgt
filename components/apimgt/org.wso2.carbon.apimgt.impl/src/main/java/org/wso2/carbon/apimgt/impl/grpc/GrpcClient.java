/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.apimgt.impl.grpc;

import choreo.apis.Types;
import choreo.apis.Types.Group;
import choreo.apis.UserServiceGrpc;
import choreo.apis.UserServiceGrpc.UserServiceBlockingStub;
import choreo.apis.UserServiceOuterClass;
import choreo.apis.UserServiceOuterClass.ListUserGroupsRequest;
import choreo.apis.UserServiceOuterClass.ListUserGroupsResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

import static choreo.apis.UserServiceGrpc.newBlockingStub;
import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class GrpcClient {

    private static final Log log = LogFactory.getLog(GrpcClient.class);
    private static volatile UserServiceBlockingStub userServiceBlockingStub = null;
    private static final Metadata.Key<String> USER_IDP_METADATA_KEY =
            Metadata.Key.of("user-idp-id", ASCII_STRING_MARSHALLER);

    private GrpcClient() {
    }

    public static void init() throws GrpcClientException {
        synchronized (GrpcClient.class) {
            if (userServiceBlockingStub == null) {

            ManagedChannel managedChannel = ManagedChannelBuilder
                    .forTarget(GrpcConfig.getInstance().getAppServiceUrl())
                    .usePlaintext().build();
            userServiceBlockingStub = newBlockingStub(managedChannel);
            log.info("Grpc client initialted successfully");
            }
        }
    }

    public static List<Group> findUserGroupsByIdpId(String userUuid)
            throws StatusRuntimeException, GrpcClientException {
        if (userServiceBlockingStub == null) {
            init();
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Getting user groups for IDP ID: %s ", userUuid));
        }
        ListUserGroupsRequest findUsersByOrganizationRequest = ListUserGroupsRequest.newBuilder()
                .setUserIdpId(userUuid)
                .build();
        ListUserGroupsResponse listUserGroupsResponse;
        List<Group> groupList;
        long startTime = System.currentTimeMillis();
        listUserGroupsResponse = userServiceBlockingStub.listUserGroups(findUsersByOrganizationRequest);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Requesting user groups completed for IDP ID: %s , request time : %d", userUuid, System.currentTimeMillis() - startTime));
        }
        groupList = listUserGroupsResponse.getGroupsList();

        return groupList;
    }

    public static List<Types.Role> findUserRolesByOrgAndIdpId(String org, String idpId) throws GrpcClientException {
        if (userServiceBlockingStub == null) {
            init();
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Getting user roles for user IDP ID: %s of org : %s ", idpId, org));
        }
        long startTime = System.currentTimeMillis();
        UserServiceOuterClass.GetUserRolesRequest request = UserServiceOuterClass.GetUserRolesRequest.newBuilder()
                .setOrganizationName(org)
                .setUserIdpId(idpId)
                .build();
        if (log.isDebugEnabled()) {
            log.debug(String.format("Request to get user roles completed. Org: %s, IdpUserId: %s, request time : %d", org, idpId,
                    System.currentTimeMillis() - startTime));
        }

        Metadata metadata = new Metadata();
        metadata.put(USER_IDP_METADATA_KEY, idpId);

        UserServiceGrpc.UserServiceBlockingStub stubWithInterceptor = MetadataUtils.attachHeaders(userServiceBlockingStub, metadata);
        UserServiceOuterClass.GetUserRolesResponse response = stubWithInterceptor.getUserRoles(request);
        if (log.isDebugEnabled()) {
            long requestTime = System.currentTimeMillis() - startTime;
            log.debug(String.format("User info request completed. Org: %s, IdpUserId: %s, Request time: %d",
                    org, idpId, requestTime));
        }
        return response.getRolesList();
    }

    public static List<Types.Role> findEnterpriseUserRolesForGroups(String org, String idpId, List<String> groupList) throws GrpcClientException {
        if (userServiceBlockingStub == null) {
            init();
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Getting user roles for  user IDP ID: %s of org : %s for groups: %s", idpId, org, groupList));
        }
        long startTime = System.currentTimeMillis();
        UserServiceOuterClass.GetEnterpriseUserRolesRequest request = UserServiceOuterClass.GetEnterpriseUserRolesRequest.newBuilder()
                .setOrganizationName(org)
                .setUserIdpId(idpId)
                .addAllGroupList(groupList)
                .build();
        if (log.isDebugEnabled()) {
            log.debug(String.format("Request to get user roles completed. Org: %s, Groups: %s, request time : %d", org, groupList,
                    System.currentTimeMillis() - startTime));
        }

        Metadata metadata = new Metadata();
        metadata.put(USER_IDP_METADATA_KEY, idpId);

        UserServiceGrpc.UserServiceBlockingStub stubWithInterceptor = MetadataUtils.attachHeaders(userServiceBlockingStub, metadata);
        startTime = System.currentTimeMillis();
        UserServiceOuterClass.GetUserRolesResponse response = stubWithInterceptor.getEnterpriseUserRoles(request);
        if (log.isDebugEnabled()) {
            long requestTime = System.currentTimeMillis() - startTime;
            log.debug(String.format("User info request completed. Org: %s, Groups: %s, Request time: %d",
                    org, groupList, requestTime));
        }
        return response.getRolesList();
    }

    public static Types.Organization findOrganizationByHandle(String orgHandle, String idpId) throws GrpcClientException {
        if (userServiceBlockingStub == null) {
            init();
        }
        long startTime = System.currentTimeMillis();
        UserServiceOuterClass.GetOrganizationRequest request = UserServiceOuterClass.GetOrganizationRequest.newBuilder()
                .setOrganizationName(orgHandle)
                .build();

        Metadata metadata = new Metadata();
        metadata.put(USER_IDP_METADATA_KEY, idpId);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Sending request to get organization. orgHandle: %s, idpId: %s", orgHandle, idpId));
        }
        UserServiceGrpc.UserServiceBlockingStub stub = MetadataUtils.attachHeaders(userServiceBlockingStub, metadata);
        UserServiceOuterClass.GetOrganizationResponse response = stub.getOrganization(request);
        if (log.isDebugEnabled()) {
            long requestTime = System.currentTimeMillis() - startTime;
            log.debug(String.format("Get organization request completed. orgHandle: %s, Request time: %d, orgUuid: %s, idpId: %s",
                    orgHandle, requestTime, response.getOrganization().getUuid(), idpId));
        }
        return response.getOrganization();
    }

    public static List<Types.Organization> findUserOrganizationsByIdpId(String idpId) throws GrpcClientException {
        if (userServiceBlockingStub == null) {
            init();
        }
        long startTime = System.currentTimeMillis();
        UserServiceOuterClass.FindUserOrganizationsRequest request = UserServiceOuterClass.FindUserOrganizationsRequest.newBuilder()
                .setUser(Types.User.newBuilder().setIdpId(idpId).build())
                .build();

        Metadata metadata = new Metadata();
        metadata.put(USER_IDP_METADATA_KEY, idpId);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Sending request to get user organizations. IdpUserId: %s", idpId));
        }
        UserServiceGrpc.UserServiceBlockingStub stub = MetadataUtils.attachHeaders(userServiceBlockingStub, metadata);
        UserServiceOuterClass.FindUserOrganizationsResponse response = stub.findUserOrganizations(request);
        if (log.isDebugEnabled()) {
            long requestTime = System.currentTimeMillis() - startTime;
            log.debug(String.format("Get user organizations request completed. IdpUserId: %s, Request time: %d, OrgCount: %d",
                    idpId, requestTime, response.getOrganizationsList().size()));
        }
        return response.getOrganizationsList();
    }
}

