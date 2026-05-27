/*
 *  Copyright (c) 2026 WSO2 LLC. (https://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.impl.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default role mappings for REST API scopes introduced after the initial product release.
 * These are used by APIMConfigServiceImpl.addMissingScopes() to inject missing scopes into
 * existing tenants' configurations on-the-fly, without requiring a DB migration.
 * <p>
 * When adding a new scope in a patch or release:
 *   1. Add the scope and its default roles here.
 *   2. Add the same entry to tenant-conf.json (for new tenants).
 * <p>
 * Role assignment policy for granular scopes:
 *   - New granular scopes default to "admin" only. Customers can assign additional
 *     Internal/ roles via the Scope Assignments page in the Admin portal as needed.
 */
public class RESTAPIScopeDefaultRoles {

    private RESTAPIScopeDefaultRoles() {
    }

    public static final Map<String, String> MISSING_SCOPE_ROLE_MAPPINGS;

    static {
        Map<String, String> scopes = new HashMap<>();

        // Scopes introduced for subscription approval workflow
        scopes.put("apim:subscription_approval_view", "admin,Internal/publisher");
        scopes.put("apim:subscription_approval_manage", "admin,Internal/publisher");

        // Scopes introduced for organization support
        scopes.put("apim:publisher_organization_read", "admin,Internal/creator");
        scopes.put("apim:organization_read", "admin");
        scopes.put("apim:organization_manage", "admin");

        // Scopes introduced for admin portal throttling policy management
        scopes.put("apim:admin_tier_view", "admin");
        scopes.put("apim:admin_tier_manage", "admin");

        // Scopes introduced for key manager management
        scopes.put("apim:keymanagers_manage", "admin");

        // Scopes introduced for admin portal settings
        scopes.put("apim:api_category", "admin");
        scopes.put("apim:api_provider_change", "admin");
        scopes.put("apim:app_settings_change", "admin");

        // Scopes introduced for gateway policy management
        scopes.put("apim:gateway_policy_manage", "admin");
        scopes.put("apim:gateway_policy_view", "admin,Internal/creator,Internal/publisher,Internal/observer");

        // Scopes introduced for LLM provider management
        scopes.put("apim:llm_provider_manage", "admin");
        scopes.put("apim:llm_provider_read", "admin,Internal/publisher,Internal/creator");

        // Scopes introduced for governance
        scopes.put("apim:gov_rule_manage", "admin");
        scopes.put("apim:gov_rule_read", "admin,Internal/publisher,Internal/creator,Internal/observer");
        scopes.put("apim:gov_result_read", "admin,Internal/publisher,Internal/creator,Internal/observer");
        scopes.put("apim:gov_policy_manage", "admin");
        scopes.put("apim:gov_policy_read", "admin,Internal/publisher,Internal/creator,Internal/observer");

        // Scopes introduced for MCP Server management
        scopes.put("apim:mcp_server_create", "admin,Internal/creator");
        scopes.put("apim:mcp_server_manage", "admin");
        scopes.put("apim:mcp_server_view", "admin,Internal/publisher,Internal/creator,Internal/analytics,Internal/observer");
        scopes.put("apim:mcp_server_list_view", "admin,Internal/integration_dev");
        scopes.put("apim:mcp_server_import_export", "admin,Internal/devops");
        scopes.put("apim:mcp_server_publish", "admin,Internal/publisher");
        scopes.put("apim:mcp_server_delete", "admin,Internal/creator");
        scopes.put("apim:mcp_server_generate_key", "admin,Internal/creator,Internal/publisher");

        // Granular API scopes
        scopes.put("apim:api_metadata_view", "admin");
        scopes.put("apim:api_create_only", "admin");
        scopes.put("apim:api_update", "admin");
        scopes.put("apim:api_definition_update", "admin");
        scopes.put("apim:api_version_create", "admin");
        scopes.put("apim:api_revision_create", "admin");
        scopes.put("apim:api_revision_delete", "admin");
        scopes.put("apim:api_deploy", "admin");
        scopes.put("apim:api_deploy_view", "admin");
        scopes.put("apim:api_lifecycle_view", "admin");
        scopes.put("apim:api_lifecycle_manage", "admin");

        // Granular API Product scopes
        scopes.put("apim:api_product_metadata_view", "admin");
        scopes.put("apim:api_product_create", "admin");
        scopes.put("apim:api_product_update", "admin");
        scopes.put("apim:api_product_delete", "admin");
        scopes.put("apim:api_product_version_create", "admin");
        scopes.put("apim:api_product_revision_create", "admin");
        scopes.put("apim:api_product_revision_delete", "admin");
        scopes.put("apim:api_product_deploy", "admin");
        scopes.put("apim:api_product_deploy_view", "admin");
        scopes.put("apim:api_product_lifecycle_view", "admin");
        scopes.put("apim:api_product_lifecycle_manage", "admin");

        // Granular MCP Server scopes
        scopes.put("apim:mcp_server_metadata_view", "admin");
        scopes.put("apim:mcp_server_create_only", "admin");
        scopes.put("apim:mcp_server_update", "admin");
        scopes.put("apim:mcp_server_version_create", "admin");
        scopes.put("apim:mcp_server_revision_create", "admin");
        scopes.put("apim:mcp_server_revision_delete", "admin");
        scopes.put("apim:mcp_server_deploy", "admin");
        scopes.put("apim:mcp_server_deploy_view", "admin");
        scopes.put("apim:mcp_server_lifecycle_view", "admin");
        scopes.put("apim:mcp_server_lifecycle_manage", "admin");

        // Granular document scopes (shared across APIs, Products, MCP Servers)
        scopes.put("apim:document_view", "admin");
        scopes.put("apim:document_update", "admin");
        scopes.put("apim:document_delete", "admin");

        // Granular shared scope scopes
        scopes.put("apim:shared_scope_view", "admin");
        scopes.put("apim:shared_scope_create", "admin");
        scopes.put("apim:shared_scope_update", "admin");
        scopes.put("apim:shared_scope_delete", "admin");

        // Granular common operation policy scopes
        scopes.put("apim:common_operation_policy_create", "admin");
        scopes.put("apim:common_operation_policy_delete", "admin");

        // Granular gateway policy scopes
        scopes.put("apim:gateway_policy_create", "admin");
        scopes.put("apim:gateway_policy_update", "admin");
        scopes.put("apim:gateway_policy_delete", "admin");

        MISSING_SCOPE_ROLE_MAPPINGS = Collections.unmodifiableMap(scopes);
    }
}
