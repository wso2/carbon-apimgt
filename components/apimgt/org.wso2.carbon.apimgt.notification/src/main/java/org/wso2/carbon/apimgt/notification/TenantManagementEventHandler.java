/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.notification;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.clients.TenantManagementClient;
import org.wso2.carbon.apimgt.impl.handlers.EventHandler;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.notification.event.TenantManagementEvent;
import org.wso2.carbon.apimgt.notification.event.TenantManagementEvent.EventDetail;
import org.wso2.carbon.apimgt.notification.event.TenantManagementEvent.Owner;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Event handler to handle tenant management related events
 */
public class TenantManagementEventHandler implements EventHandler {

    private static final Log log = LogFactory.getLog(TenantManagementEventHandler.class);
    private TenantManagementClient client;

    private static final String AUTH_HEADER_PREFIX = "Authorization";
    private static final String BASIC_SCHEME_PREFIX = "basic ";

    @Override
    public boolean handleEvent(String event, Map<String, List<String>> headers) throws APIManagementException {
        try {
            if (!isAuthorized(headers.get(AUTH_HEADER_PREFIX))) {
                throw new APIManagementException("User is not authorized to perform this operation.");
            }
            if (client == null) {
                client = new TenantManagementClient();
            }
            TenantManagementEvent tenantMgtEvent = new Gson().fromJson(event, TenantManagementEvent.class);
            EventDetail eventDetail;

            if (tenantMgtEvent != null) {
                Map<String, EventDetail> eventsMap = tenantMgtEvent.getEvents();

                if (eventsMap != null && !eventsMap.isEmpty()) {
                    Optional<EventDetail> firstEventDetail = eventsMap.values().stream().findFirst();

                    if (firstEventDetail.isPresent()) {
                        eventDetail = firstEventDetail.get();
                        if (eventDetail != null) {
                            if (eventDetail.getTenant() == null || eventDetail.getTenant().getDomain() == null) {
                                throw new APIManagementException("Invalid tenant management event data");
                            }

                            String tenantDomain = eventDetail.getTenant().getDomain();

                            String action = eventDetail.getAction();
                            if (log.isDebugEnabled()) {
                                log.debug("Processing tenant management event of type: " + action);
                            }
                            if (action == null) {
                                throw new APIManagementException("action type cannot be null");
                            }

                            if (APIConstants.TenantManagementEvent.TYPE_ADD_TENANT.equals(action)) {
                                addTenant(eventDetail);
                            } else if (APIConstants.TenantManagementEvent.TYPE_UPDATE_TENANT.equals(action)) {
                                updateTenant(eventDetail);
                            } else if (APIConstants.TenantManagementEvent.TYPE_ACTIVATE_TENANT.equals(action)) {
                                activateTenant(tenantDomain);
                            } else if (APIConstants.TenantManagementEvent.TYPE_DEACTIVATE_TENANT.equals(action)) {
                                deactivateTenant(tenantDomain);
                            } else {
                                throw new APIManagementException("Invalid action in tenant management event " + action);
                            }
                        } else {
                            throw new APIManagementException(
                                    "Could not process event because EventDetail could not be extracted.");
                        }

                    } else {
                        throw new APIManagementException("Events element does not contain necessary data");
                    }

                } else {
                    throw new APIManagementException(
                            "The 'events' field in the payload was either missing, null, or empty.");
                }
            } else {
                throw new APIManagementException(
                        "Failed to deserialize the event payload. The JSON string might be null or malformed.");
            }

        } catch (APIManagementException e) {
            throw new APIManagementException("Error while executing tenant management service", e);
        }
        return true;
    }

    private void deactivateTenant(String tenantDomain) throws APIManagementException {
        
        if (log.isDebugEnabled()) {
            log.debug("Deactivate tenant " + tenantDomain);
        }
        if (client != null) {
            client.deactivateTenant(tenantDomain);
        }

    }

    private void activateTenant(String tenantDomain) throws APIManagementException {
        
        if (log.isDebugEnabled()) {
            log.debug("Activate tenant " + tenantDomain);
        }
        if (client != null) {
            client.activateTenant(tenantDomain);
        }

    }

    private void updateTenant(EventDetail updateTenantEvent) throws APIManagementException {

        if (updateTenantEvent != null && updateTenantEvent.getTenant() != null
                && updateTenantEvent.getTenant().getOwners() != null
                && !updateTenantEvent.getTenant().getOwners().isEmpty()) {

            Owner userInfo = updateTenantEvent.getTenant().getOwners().get(0);
            String firstName = userInfo.getFirstname();
            String lastName = userInfo.getLastname();
            String adminPassword = userInfo.getPassword();
            String tenantDomain = updateTenantEvent.getTenant().getDomain();
            String email = userInfo.getEmail();

            if (log.isDebugEnabled()) {
                log.debug("Update tenant " + tenantDomain);
            }

            if (client != null) {
                client.updateTenant(firstName, lastName, adminPassword, email, tenantDomain, true);
            }
        }
    }

    private void addTenant(EventDetail addTenantEvent) throws APIManagementException {

        if (addTenantEvent != null && addTenantEvent.getTenant() != null
                && addTenantEvent.getTenant().getOwners() != null
                && !addTenantEvent.getTenant().getOwners().isEmpty()) {

            Owner userInfo = addTenantEvent.getTenant().getOwners().get(0);
            String firstName = userInfo.getFirstname();
            String lastName = userInfo.getLastname();
            String adminUserName = userInfo.getUsername();
            String adminPassword = userInfo.getPassword();
            String tenantDomain = addTenantEvent.getTenant().getDomain();
            String email = userInfo.getEmail();

            if (log.isDebugEnabled()) {
                log.debug("Add tenant " + tenantDomain);
            }

            if (client != null) {
                client.addTenant(firstName, lastName, adminUserName, adminPassword, email, tenantDomain, true);
            }
        }
    }

    @Override
    public String getType() {
        return APIConstants.TenantManagementEvent.TENANT_MANAGEMENT_TYPE;
    }

    private boolean isAuthorized(List<String> headers) {

        // Return null immediately if the list is null or empty to avoid errors.
        if (headers == null || headers.isEmpty()) {
            return false;
        }

        // Loop through each header in the provided list.
        for (String header : headers) {
            if (header == null) {
                continue; // Skip null headers
            }

            String lowercasedHeader = header.toLowerCase(Locale.ROOT);

            // Check if the header content is an "Basic ".
            if (lowercasedHeader.startsWith(BASIC_SCHEME_PREFIX)) {

                // Extract the Base64 encoded credentials string.
                String base64Credentials = header.substring(BASIC_SCHEME_PREFIX.length()).trim();

                try {
                    // Decode the Base64 string to bytes.
                    byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
                    String decodedCredentials = new String(decodedBytes, StandardCharsets.UTF_8);

                    // The decoded string should be in the format "username:password".
                    // We use split with a limit of 2 to handle passwords that might contain a colon.
                    final String[] credentialsArray = decodedCredentials.split(":", 2);

                    // Validate that we got both a username and a password.
                    if (credentialsArray.length == 2) {
                        String username = credentialsArray[0];
                        if (APIUtil.hasPermission(username, APIConstants.Permissions.TENANT_MANAGE) ||
                                APIUtil.hasPermission(username, APIConstants.Permissions.TENANT_MANAGE_MODIFY)) {
                            return true;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // This catches errors if the string is not valid Base64.
                    log.error("Error decoding Base64 credentials: " + e.getMessage());
                    return false;
                } catch (APIManagementException e) {
                    log.error("Error while checking permission: " + e.getMessage());
                    return false;
                }

                // If an "Authorization" header was found but it wasn't "Basic",
                return false;
            }
        }
        return false;
    }
}
