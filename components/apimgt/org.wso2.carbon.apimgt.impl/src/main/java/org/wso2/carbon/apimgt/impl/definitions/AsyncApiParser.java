package org.wso2.carbon.apimgt.impl.definitions;

import org.wso2.carbon.apimgt.api.UsedByMigrationClient;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated use org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser instead
 */
@Deprecated
@UsedByMigrationClient
public class AsyncApiParser extends org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser {
    @Override
    public Set<URITemplate> getURITemplates(String apiDefinition, boolean includePublish)
            throws APIManagementException {
        return Set.of();
    }

    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        return Set.of();
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent)
            throws APIManagementException {
        return null;
    }

    @Override
    public String generateAsyncAPIDefinition(API api) throws APIManagementException {
        return "";
    }

    @Override
    public String getAsyncApiDefinitionForStore(API api, String asyncAPIDefinition,
                                                Map<String, String> hostsWithSchemes) throws APIManagementException {
        return "";
    }

    @Override
    public String updateAsyncAPIDefinition(String oldDefinition, API apiToUpdate) {
        return "";
    }

    @Override
    public Map<String, String> buildWSUriMapping(String apiDefinition) {
        return Map.of();
    }
}
