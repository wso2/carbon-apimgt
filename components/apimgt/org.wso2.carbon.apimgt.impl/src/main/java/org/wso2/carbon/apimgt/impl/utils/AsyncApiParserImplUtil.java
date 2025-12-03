package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.spec.parser.definitions.AbstractAsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiParseOptions;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiParserFactory;

public class AsyncApiParserImplUtil {

    public static AsyncApiParseOptions getConfiguredDefaultParser() {

        AsyncApiParseOptions options =  new AsyncApiParseOptions();
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration();
        options.setDefaultAsyncApiParserVersion(Boolean.parseBoolean(config.getFirstProperty(
                APIConstants.API_PUBLISHER_PRESERVE_LEGACY_ASYNC_PARSER)));
        return options;
    }

    public static AbstractAsyncApiParser getAsyncApiParserWithOptions(String version) throws APIManagementException {

        AbstractAsyncApiParser asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(version, getConfiguredDefaultParser());
        return asyncApiParser;
    }
}
