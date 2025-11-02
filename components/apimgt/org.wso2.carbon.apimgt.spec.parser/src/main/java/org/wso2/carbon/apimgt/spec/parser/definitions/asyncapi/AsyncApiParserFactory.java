package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.spec.parser.definitions.APISpecParserConstants;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models.*;

public class AsyncApiParserFactory {

    private static final Log log = LogFactory.getLog(AsyncApiParserUtil.class);

    public static AsyncApiParser getAsyncApiParser(String version) throws UnsupportedOperationException{
        if (version == null) {
            throw new IllegalArgumentException("AsyncAPI version cannot be null");
        } else if (isAsyncApiV2(version)) {
            log.debug("[AsyncAPI][AsyncApiParserFactory] AsyncAPI definition version is V2.x.x");
            return new AsyncApiV2Parser();
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V30)) {
            log.debug("[AsyncAPI][AsyncApiParserFactory] AsyncAPI definition version is V3.0.0");
            return new AsyncApiV3Parser();
        } else {
            throw new UnsupportedOperationException("Unsupported AsyncAPI version: " + version);
        }
    }

    private static boolean isAsyncApiV2(String version) {
        return version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V20)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V21)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V22)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V23)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V24)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V25)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V26);
    }
}
