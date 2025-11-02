package org.wso2.carbon.apimgt.solace.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser;

import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil.getExtensionFromAsyncApiDoc;


/**
 * This Parser class will validate the Solace Async API Specifications.
 */
@Component(
        name = "solace.async.definition.parser.component",
        immediate = true,
        service = APIDefinition.class
)
public class SolaceApiParser extends AsyncApiParser {

    /**
     * This method will extract the vendor provider or the API specification form the extensions list
     *
     * @param definition String
     * @return String
     */
    @Override
    public String getVendorFromExtension(String definition)  {

        Map<String, JsonNode> extensions = getExtensionFromAsyncApiDoc(definition);
//        Map<String, JsonNode> extensions = getExtensionFromAsyncApiDoc(getAsyncApiVersion(definition), definition);
        if (extensions != null && extensions.containsKey("x-origin")) {
            JsonNode origin = extensions.get("x-origin");
            ObjectMapper objectMapper = new ObjectMapper();
            Map originMap = objectMapper.convertValue(origin, Map.class);
            if (originMap.containsKey("vendor")) {
                if (SolaceConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(originMap.get("vendor").toString())) {
                    return SolaceConstants.SOLACE_ENVIRONMENT;
                }
            }
        }
        return null;
    }

    @Override
    public Set<URITemplate> getURITemplates(String apiDefinition, boolean includePublish)
            throws APIManagementException {
        return null;
    }

    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        return null;
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent)
            throws APIManagementException {
        return null;
    }

    @Override
    public String generateAsyncAPIDefinition(API api) throws APIManagementException {
        return null;
    }

    @Override
    public String getAsyncApiDefinitionForStore(API api, String asyncAPIDefinition, Map<String, String>
            hostsWithSchemes) throws APIManagementException {
        return null;
    }

    @Override
    public String updateAsyncAPIDefinition(String oldDefinition, API apiToUpdate) {
        return null;
    }

    @Override
    public Map<String, String> buildWSUriMapping(String apiDefinition) {
        return null;
    }

    @Override
    public String getType() {
        return SolaceConstants.SOLACE_ENVIRONMENT;
    }

}
