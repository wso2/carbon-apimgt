package org.wso2.carbon.apimgt.impl.definitions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.UsedByMigrationClient;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.nio.charset.Charset;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

/**
 * @deprecated use org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil instead
 */
@Deprecated
@UsedByMigrationClient
public class AsyncApiParserUtil extends org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil {

    private static final Log log = LogFactory.getLog(AsyncApiParserUtil.class);

    /**
     * This method returns api definition json for given api
     *
     * @param apiIdentifier api identifier
     * @param registry user registry
     * @return api definition json as json string
     * @throws APIManagementException
     */
    @Deprecated
    public static String getAPIDefinition(Identifier apiIdentifier, Registry registry) throws APIManagementException {
        String resourcePath = "";

        if (apiIdentifier instanceof APIIdentifier) {
            resourcePath = APIUtil.getAsyncAPIDefinitionFilePath(apiIdentifier.getName(), apiIdentifier.getVersion(),
                    apiIdentifier.getProviderName());
        }
        JSONParser parser = new JSONParser();
        String apiDocContent = null;
        try {
            if (registry.resourceExists(resourcePath + APIConstants.API_ASYNCAPI_DEFINITION_RESOURCE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + APIConstants.API_ASYNCAPI_DEFINITION_RESOURCE_NAME);
                apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                parser.parse(apiDocContent);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Resource" + APIConstants.API_ASYNCAPI_DEFINITION_RESOURCE_NAME + " not found at "
                            + resourcePath);
                }
            }
        } catch (RegistryException e) {
            handleException(
                    "Error while retrieving AsyncAPI Definition for " + apiIdentifier.getName() + "-"
                            + apiIdentifier.getVersion(), e);
        } catch (ParseException e) {
            handleException(
                    "Error while parsing AsyncAPI Definition for " + apiIdentifier.getName() + "-"
                            + apiIdentifier.getVersion() + " in " + resourcePath, e);
        }
        return apiDocContent;
    }
}
