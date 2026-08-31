/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.subscription.Subscription;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.SubscriptionsApiService;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

public class SubscriptionsApiServiceImpl implements SubscriptionsApiService {

    @Override
    public Response subscriptionsGet(String xWSO2Tenant, Integer apiId, Integer appId, String apiUUID,
                                     String applicationUUID, MessageContext messageContext) throws
            APIManagementException {

        Response result;

        List<Subscription> subscriptionList = new ArrayList<>();
        String authenticatedOrganization = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (StringUtils.isEmpty(authenticatedOrganization)) {
            return Response.ok().entity(
                    SubscriptionValidationDataUtil.fromSubscriptionToSubscriptionListDTO(subscriptionList)).build();
        }

        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        String organization = validateOrganization(RestApiUtil.getOrganization(messageContext),
                authenticatedOrganization);
        if (StringUtils.isNotEmpty(applicationUUID) && StringUtils.isNotEmpty(apiUUID)) {
            Subscription subscription = getSubscription(subscriptionValidationDAO, apiUUID, applicationUUID,
                    authenticatedOrganization);
            if (subscription != null) {
                subscriptionList.add(subscription);
            }
            result = Response.ok().entity(
                    SubscriptionValidationDataUtil.fromSubscriptionToSubscriptionListDTO(subscriptionList)).build();
        } else if (apiId != null && appId != null) {
            Subscription subscription = getSubscription(subscriptionValidationDAO, apiId, appId,
                    authenticatedOrganization);
            if (subscription != null) {
                subscriptionList.add(subscription);
            }
            result = Response.ok().entity(
                    SubscriptionValidationDataUtil.fromSubscriptionToSubscriptionListDTO(subscriptionList)).build();
        } else if (StringUtils.isNotEmpty(organization) &&
                !organization.equalsIgnoreCase(APIConstants.ORG_ALL_QUERY_PARAM)) {
            result = Response.ok().entity(SubscriptionValidationDataUtil.fromSubscriptionToSubscriptionListDTO(
                    subscriptionValidationDAO.getAllSubscriptionsByOrganization(organization))).build();
        } else if (StringUtils.isNotEmpty(organization) && organization.equalsIgnoreCase(APIConstants.ORG_ALL_QUERY_PARAM) &&
                xWSO2Tenant.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            result = Response.ok().entity(SubscriptionValidationDataUtil.fromSubscriptionToSubscriptionListDTO(
                    subscriptionValidationDAO.getAllSubscriptions())).build();
        } else if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            result = Response.ok().entity(SubscriptionValidationDataUtil.fromSubscriptionToSubscriptionListDTO(
                    subscriptionValidationDAO.getAllSubscriptions(xWSO2Tenant))).build();
        } else {
            result = Response.ok().entity(SubscriptionValidationDataUtil.fromSubscriptionToSubscriptionListDTO(
                    subscriptionValidationDAO.getAllSubscriptions())).build();
        }

        return result;
    }

    static Subscription getSubscription(SubscriptionValidationDAO subscriptionValidationDAO, String apiUUID,
                                        String applicationUUID, String authenticatedOrganization) {

        if (StringUtils.isEmpty(authenticatedOrganization)) {
            return null;
        }
        Subscription subscription = subscriptionValidationDAO.getSubscription(apiUUID, applicationUUID);
        return isSubscriptionAccessibleForOrganization(subscription, authenticatedOrganization) ? subscription : null;
    }

    static Subscription getSubscription(SubscriptionValidationDAO subscriptionValidationDAO, int apiId, int appId,
                                        String authenticatedOrganization) {

        if (StringUtils.isEmpty(authenticatedOrganization)) {
            return null;
        }
        Subscription subscription = subscriptionValidationDAO.getSubscription(apiId, appId);
        return isSubscriptionAccessibleForOrganization(subscription, authenticatedOrganization) ? subscription : null;
    }

    private static boolean isSubscriptionAccessibleForOrganization(Subscription subscription, String organization) {

        return subscription != null &&
                (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(organization) ||
                        organization.equalsIgnoreCase(subscription.getAppOrganization()));
    }

    static String validateOrganization(String requestedOrganization, String authenticatedOrganization) {

        if (StringUtils.isEmpty(authenticatedOrganization)) {
            return null;
        }
        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(authenticatedOrganization)
                ? requestedOrganization : authenticatedOrganization;
    }
}
